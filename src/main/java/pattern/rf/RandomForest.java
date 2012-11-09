/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.rf;

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
import pattern.Classifier;
import pattern.PatternException;
import pattern.XPathReader;

import cascading.tuple.Tuple;


public class RandomForest extends Classifier implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( RandomForest.class );

  public List<String> predicates = new ArrayList<String>();
  public List<Tree> forest = new ArrayList<Tree>();

  protected Object[] param_values;
  protected Boolean[] pred_eval;
  protected ExpressionEvaluator[] ee_list;
  protected Map<String, Integer> votes;

  /**
   * @param reader
   * @throws PatternException
   */
  public RandomForest( XPathReader reader ) throws PatternException
    {
    this.reader = reader;
    buildSchema();
    buildForest();
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
   * @return
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
   * Map from an input tuple to an array of predicate values for the forest.
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
   * @return
   */
  protected String tallyVotes( Map<String, Integer> votes )
    {
    String label = null;
    Integer winning_vote = 0;

    // tally the vote for each tree in the forest

    for( Tree tree : forest )
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
   * Generate a serializable graph representation for each tree.
   *
   * @throws PatternException
   */
  protected void buildForest() throws PatternException
    {
    String expr = "/PMML/MiningModel/Segmentation/Segment";
    NodeList node_list = (NodeList) reader.read( expr, XPathConstants.NODESET );

    for( int i = 0; i < node_list.getLength(); i++ )
      {
      Node node = node_list.item( i );

      if( node.getNodeType() == Node.ELEMENT_NODE )
        {
        String id = ( (Element) node ).getAttribute( "id" );
        String node_expr = "./TreeModel/Node[1]";
        NodeList root_node = (NodeList) reader.read( node, node_expr, XPathConstants.NODESET );

        Tree tree = new Tree( id );
        forest.add( tree );

        Element root = (Element) root_node.item( 0 );
        Vertex vertex = makeVertex( root, tree.getGraph() );
        tree.setRoot( vertex );
        buildNode( root, vertex, tree.getGraph() );
        }
      }
    }

  /**
   * @param node
   * @param graph
   * @return
   */
  protected Vertex makeVertex( Element node, DirectedGraph<Vertex, Edge> graph )
    {
    String id = ( node ).getAttribute( "id" );
    Vertex vertex = new Vertex( id );
    graph.addVertex( vertex );

    return vertex;
    }

  /**
   * @param node
   * @param vertex
   * @param graph
   * @throws PatternException
   */
  protected void buildNode( Element node, Vertex vertex, DirectedGraph<Vertex, Edge> graph ) throws PatternException
    {
    NodeList child_nodes = node.getChildNodes();

    for( int i = 0; i < child_nodes.getLength(); i++ )
      {
      Node child = child_nodes.item( i );

      if( child.getNodeType() == Node.ELEMENT_NODE )
        {
        if( child.getNodeName().equals( "SimplePredicate" ) )
          {
          Integer predicate_id = makePredicate( (Element) child );

          if( node.hasAttribute( "score" ) )
            {
            String score = ( node ).getAttribute( "score" );
            vertex.setScore( score );
            }

          for( Edge e : graph.edgesOf( vertex ) )
            e.setPredicateId( predicate_id );
          }
        else if( child.getNodeName().equals( "Node" ) )
          {
          Vertex child_vertex = makeVertex( (Element) child, graph );
          Edge edge = graph.addEdge( vertex, child_vertex );

          buildNode( (Element) child, child_vertex, graph );
          }
        }
      }
    }

  /**
   * @param node
   * @return
   * @throws PatternException
   */
  protected Integer makePredicate( Element node ) throws PatternException
    {
    String field = node.getAttribute( "field" );
    String operator = node.getAttribute( "operator" );
    String value = node.getAttribute( "value" );

    String eval = null;

    if( operator.equals( "greaterThan" ) )
      eval = field + " > " + value;
    else if( operator.equals( "lessOrEqual" ) )
      eval = field + " <= " + value;
    else
      throw new PatternException( "unknown operator: " + operator );

    if( !predicates.contains( eval ) )
      predicates.add( eval );

    Integer predicate_id = predicates.indexOf( eval );

    return predicate_id;
    }

  /** @return  */
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
    buf.append( forest );
    buf.append( "\n" );
    buf.append( "---------" );
    buf.append( "\n" );

    for( Tree tree : forest )
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
