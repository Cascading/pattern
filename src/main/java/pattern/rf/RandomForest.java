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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pattern.Classifier;
import pattern.PatternException;
import pattern.XPathReader;


public class RandomForest extends Classifier implements Serializable
  {
  public List<String> predicates = new ArrayList<String>();
  public List<Tree> forest = new ArrayList<Tree>();

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

  /** @return  */
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
        Vertex vertex = makeVertex( root, 0, tree.getGraph() );
        tree.setRoot( vertex );
        buildNode( root, vertex, 0, tree.getGraph() );
        }
      }
    }

  /**
   * @param depth
   * @return
   */
  private static String spacer( int depth )
    {
    String pad = "";

    for( int i = 0; i < depth; i++ )
      pad += " ";

    return pad;
    }

  /**
   * @param node
   * @param depth
   * @param graph
   * @return
   */
  protected Vertex makeVertex( Element node, Integer depth, DirectedGraph<Vertex, Edge> graph )
    {
    String pad = spacer( depth );
    String id = ( node ).getAttribute( "id" );
    Vertex vertex = new Vertex( id );
    graph.addVertex( vertex );

    return vertex;
    }

  /**
   * @param node
   * @param vertex
   * @param depth
   * @param graph
   * @throws PatternException
   */
  protected void buildNode( Element node, Vertex vertex, Integer depth, DirectedGraph<Vertex, Edge> graph ) throws PatternException
    {
    String pad = spacer( depth );
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
          Vertex child_vertex = makeVertex( (Element) child, depth + 1, graph );
          Edge edge = graph.addEdge( vertex, child_vertex );

          buildNode( (Element) child, child_vertex, depth + 1, graph );
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

  /**
   * @param fields
   * @return
   * @throws PatternException
   */
  public String classifyTuple( String[] fields ) throws PatternException
    {
    Boolean[] pred_eval = evalPredicates( fields );
    Map<String, Integer> votes = new HashMap<String, Integer>();
    String label = tallyVotes( pred_eval, votes );

    return label;
    }

  /**
   * @param fields
   * @return
   * @throws PatternException
   */
  public Boolean[] evalPredicates( String[] fields ) throws PatternException
    {
    // map from input tuple to an array of predicate values for the forest

    Boolean[] pred_eval = new Boolean[ predicates.size() ];
    int predicate_id = 0;

    for( String predicate : predicates )
      {
      try
        {
        Object[] param_values = new Object[ fields.length ];
        String[] param_names = new String[ fields.length ];
        Class[] param_types = new Class[ fields.length ];
        int i = 0;

        for( String name : schema.keySet() )
          {
          param_values[ i ] = new Double( fields[ i ] );
          param_names[ i ] = name;
          param_types[ i ] = double.class;
          i++;
          }

        ExpressionEvaluator ee = new ExpressionEvaluator( predicate, boolean.class, param_names, param_types, new Class[ 0 ], null );
        Object res = ee.evaluate( param_values );
        pred_eval[ predicate_id ] = new Boolean( res.toString() );
        }
      catch( CompileException e )
        {
        e.printStackTrace();
        throw new PatternException( "predicate did not compile", e );
        }
      catch( InvocationTargetException e )
        {
        e.printStackTrace();
        throw new PatternException( "predicate did not compile", e );
        }
      catch( NumberFormatException e )
        {
        e.printStackTrace();
        throw new PatternException( "tuple format is bad", e );
        }
      catch( NullPointerException e )
        {
        e.printStackTrace();
        throw new PatternException( "predicates failed", e );
        }

      predicate_id += 1;
      }

    return pred_eval;
    }

  /**
   * @param pred_eval
   * @param votes
   * @return
   */
  public String tallyVotes( Boolean[] pred_eval, Map<String, Integer> votes )
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
  }
