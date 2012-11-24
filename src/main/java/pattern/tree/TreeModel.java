/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.tree;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathConstants;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;
import org.jgrapht.DirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cascading.tuple.Tuple;
import pattern.Classifier;
import pattern.PatternException;
import pattern.PMML;
import pattern.datafield.DataField;


public class TreeModel extends Classifier implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( TreeModel.class );

  public List<String> predicates = new ArrayList<String>();
  protected List<Tree> segments = new ArrayList<Tree>();

  protected Object[] param_values;
  protected Boolean[] pred_eval;
  protected ExpressionEvaluator[] ee_list;

  protected Map<String, Integer> votes;

  /**
   * @param pmml PMML model
   * @throws PatternException
   */
  public TreeModel( PMML pmml ) throws PatternException
    {
    schema = pmml.getSchema();

    // test for different tree structure (decision tree vs. forest)
    NodeList node_list = pmml.getNodeList( "//TreeModel[1]/.." );
    Node parent = node_list.item( 0 );

    if( "Segment".equals( parent.getNodeName() ) )
      {
      schema.parseMiningSchema( pmml.getNodeList( "/PMML/MiningModel/MiningSchema/MiningField" ) );

      String expr = "/PMML/MiningModel/Segmentation/Segment";
      buildForest( pmml, pmml.getNodeList( expr ) );
      }
    else
      {
      schema.parseMiningSchema( pmml.getNodeList( "/PMML/TreeModel/MiningSchema/MiningField" ) );

      Tree tree = new Tree( "default" );
      segments.add( tree );

      NodeList root_node = pmml.getNodeList( "//TreeModel[1]" );

      buildTree( pmml, (Element) root_node.item( 0 ), tree );
      }
    }

  /**
   * Prepare to classify with this model. Called immediately before
   * the enclosing Operation instance is put into play processing
   * Tuples.
   */
  @Override
  public void prepare()
    {
    // handle the loop-invariant preparations here,
    // in lieu of incurring overhead for each tuple

    String[] param_names = schema.getParamNames();
    Class[] param_types = schema.getParamTypes();

    pred_eval = new Boolean[ predicates.size() ];
    ee_list = new ExpressionEvaluator[ predicates.size() ];

    for( int i = 0; i < predicates.size(); i++ )
      try
        {
          LOG.debug( "eval: " + predicates.get( i ) );
          ee_list[ i ] = new ExpressionEvaluator( predicates.get( i ), boolean.class, param_names, param_types, new Class[ 0 ], null );
        }
      catch( NullPointerException exception )
        {
        String message = String.format( "predicate [ %s ] failed", predicates.get( i ) );
        LOG.error( message, exception );
        throw new PatternException( message, exception );
        }
      catch( CompileException exception )
        {
        String message = String.format( "predicate [ %s ] did not compile", predicates.get( i ) );
        LOG.error( message, exception );
        throw new PatternException( message, exception );
        }

    param_values = new Object[ schema.size() ];
    votes = new HashMap<String, Integer>();
    }

  /**
   * Classify an input tuple, returning the predicted label.
   *
   * @param values
   * @return String
   * @throws PatternException
   */
  @Override
  public String classifyTuple( Tuple values ) throws PatternException
    {
    evalPredicates( values );
    votes.clear();

    return tallyVotes( votes );
    }

  /**
   * Map from an input tuple to an array of predicate values for the segments.
   *
   * @param values
   * @throws PatternException
   */
  protected void evalPredicates( Tuple values ) throws PatternException
    {
    schema.setParamValues( values, param_values );

    for( int i = 0; i < predicates.size(); i++ )
      try
        {
        pred_eval[ i ] = new Boolean( ee_list[ i ].evaluate( param_values ).toString() );
        }
      catch( InvocationTargetException exception )
        {
        String message = String.format( "predicate [ %s ] did not evaluate", predicates.get( i ) );
        LOG.error( message, exception );
        throw new PatternException( message, exception );
        }
    }

  /**
   * Tally the vote for each tree in the forest, to determine the winning label.
   *
   * @param votes
   * @return String
   */
  protected String tallyVotes( Map<String, Integer> votes )
    {
    String label = null;
    Integer winning_vote = 0;

    // tally the vote for each tree in the forest

    for( Tree tree : segments )
      {
      label = tree.traverse( pred_eval );

      if( !votes.containsKey( label ) )
        winning_vote = 1;
      else
        winning_vote = votes.get( label ) + 1;

      votes.put( label, winning_vote );
      }

    // determine the winning label

    for( String key : votes.keySet() )
      {
      if( votes.get( key ) > winning_vote )
        {
        label = key;
        winning_vote = votes.get( key );
        }
      }

    return label;
    }

  /**
   * Generate a serializable graph representation for a list of trees.
   *
   * @param pmml PMML model
   * @param node_list
   * @throws PatternException
   */
  protected void buildForest( PMML pmml, NodeList node_list ) throws PatternException
    {
    for( int i = 0; i < node_list.getLength(); i++ )
      {
      Node node = node_list.item( i );

      if( node.getNodeType() == Node.ELEMENT_NODE )
        {
        String id = ( (Element) node ).getAttribute( "id" );
        Tree tree = new Tree( id );
        segments.add( tree );

        String node_expr = "./TreeModel/Node[1]";
        NodeList root_node = (NodeList) pmml.getReader().read( node, node_expr, XPathConstants.NODESET );

        buildTree( pmml, (Element) root_node.item( 0 ), tree );
        }
      }
    }

  /**
   * Generate a serializable graph representation for a tree.
   *
   * @param pmml PMML model
   * @param root root XML node
   * @param tree serializable tree structure
   * @throws PatternException
   */
  protected void buildTree( PMML pmml, Element root, Tree tree ) throws PatternException
    {
    Vertex vertex = makeVertex( root, tree.getGraph() );
    tree.setRoot( vertex );

    buildNode( pmml, root, vertex, tree.getGraph() );
    }

  /**
   * @param node
   * @param graph
   * @return Vertex
   */
  protected Vertex makeVertex( Element node, DirectedGraph<Vertex, Edge> graph )
    {
    String id = ( node ).getAttribute( "id" );
    Vertex vertex = new Vertex( id );
    graph.addVertex( vertex );

    return vertex;
    }

  /**
   * @param pmml PMML model
   * @param node
   * @param vertex
   * @param graph
   * @throws PatternException
   */
  protected void buildNode( PMML pmml, Element node, Vertex vertex, DirectedGraph<Vertex, Edge> graph ) throws PatternException
    {
    NodeList child_nodes = node.getChildNodes();

    for( int i = 0; i < child_nodes.getLength(); i++ )
      {
      Node child = child_nodes.item( i );

      if( child.getNodeType() == Node.ELEMENT_NODE )
        {
        if( "SimplePredicate".equals( child.getNodeName() ) || "SimpleSetPredicate".equals( child.getNodeName() ) )
          {
          Integer predicate_id = makePredicate( pmml, (Element) child );

          if( node.hasAttribute( "score" ) )
            {
            String score = ( node ).getAttribute( "score" );
            vertex.setScore( score );
            }

          for( Edge e : graph.edgesOf( vertex ) )
            e.setPredicateId( predicate_id );
          }
        else if( "Node".equals( child.getNodeName() ) )
          {
          Vertex child_vertex = makeVertex( (Element) child, graph );
          Edge edge = graph.addEdge( vertex, child_vertex );

          buildNode( pmml, (Element) child, child_vertex, graph );
          }
        }
      }
    }

  /**
   * @param pmml PMML model
   * @param node
   * @return Integer
   * @throws PatternException
   */
  protected Integer makePredicate( PMML pmml, Element node ) throws PatternException
    {
    String field = node.getAttribute( "field" );
    String eval = schema.get( field ).getEval( pmml.getReader(), node );

    if( !predicates.contains( eval ) )
      predicates.add( eval );

    Integer predicate_id = predicates.indexOf( eval );

    return predicate_id;
    }

  /** @return String  */
  @Override
  public String toString()
    {
    StringBuilder buf = new StringBuilder();

    buf.append( "---------" );
    buf.append( "\n" );
    buf.append( schema );
    buf.append( "\n" );
    buf.append( "---------" );
    buf.append( "\n" );
    buf.append( segments );
    buf.append( "\n" );
    buf.append( "---------" );
    buf.append( "\n" );

    for( Tree tree : segments )
      {
      buf.append( tree );
      buf.append( tree.getRoot() );

      for( Edge edge : tree.getGraph().edgeSet() )
        buf.append( edge );

      buf.append( "\n" );
      }

    buf.append( "---------" );
    buf.append( "\n" );

    for( String predicate : predicates )
      {
      buf.append( "expr[ " + predicates.indexOf( predicate ) + " ]: " + predicate );
      buf.append( "\n" );
      }

    return buf.toString();
    }
  }
