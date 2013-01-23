/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.model.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPathConstants;

import cascading.tuple.Fields;
import org.jgrapht.DirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cascading.tuple.Tuple;
import pattern.PatternException;
import pattern.PMML;
import pattern.Schema;
import pattern.model.Model;


public class TreeModel extends Model implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( TreeModel.class );

  public Context context = null;
  public Tree tree;

  /**
   * Constructor for a TreeModel as a standalone classifier (PMML
   * versions 1-3).
   *
   * @param pmml PMML model
   * @throws PatternException
   */
  public TreeModel( PMML pmml ) throws PatternException
    {
    schema = pmml.getSchema();
    context = new Context();

    schema.parseMiningSchema( pmml.getNodeList( "/PMML/TreeModel/MiningSchema/MiningField" ) );
    tree = new Tree( "default" );

    String node_expr = "./TreeModel/Node[1]";
    NodeList root_node = pmml.getNodeList( node_expr );

    buildTree( pmml, context, (Element) root_node.item( 0 ), tree );
    }

  /**
   * Constructor for a TreeModel as part of an ensemble (PMML verion
   * 4+), such as in Random Forest.
   *
   * @param pmml PMML model
   * @param context tree context
   * @param parent parent node in the XML
   * @throws PatternException
   */
  public TreeModel( PMML pmml, Context context, Node parent ) throws PatternException
    {
    String id = ( (Element) parent ).getAttribute( "id" );
    tree = new Tree( id );

    String node_expr = "./TreeModel/Node[1]";
    NodeList root_node = (NodeList) pmml.getReader().read( parent, node_expr, XPathConstants.NODESET );

    buildTree( pmml, context, (Element) root_node.item( 0 ), tree );
    }

  /**
   * Prepare to classify with this model. Called immediately before
   * the enclosing Operation instance is put into play processing
   * Tuples.
   */
  @Override
  public void prepare()
    {
    context.prepare( schema );
    }

  /**
   * Classify an input tuple, returning the predicted label.
   *
   *
   * @param values tuple values
   * @param fields
   * @return String
   * @throws PatternException
   */
  @Override
  public String classifyTuple(Tuple values, Fields fields) throws PatternException
    {
    // TODO
    return "null";
    }

  /**
   * Generate a serializable graph representation for a tree.
   *
   * @param pmml PMML model
   * @param shared_context tree context
   * @param root root node in the XML
   * @param tree serializable tree structure
   * @throws PatternException
   */
  public void buildTree( PMML pmml, Context shared_context, Element root, Tree tree ) throws PatternException
    {
    Vertex vertex = makeVertex( root, tree.getGraph() );
    tree.setRoot( vertex );

    buildNode( pmml, shared_context, root, vertex, tree.getGraph() );
    }

  /**
   * @param pmml PMML model
   * @param shared_context tree context
   * @param node predicate node in the XML
   * @param vertex tree vertex
   * @param graph serializable graph
   * @throws PatternException
   */
  protected void buildNode( PMML pmml, Context shared_context, Element node, Vertex vertex, DirectedGraph<Vertex, Edge> graph ) throws PatternException
    {
    // build a list of parameters from which the predicate will be evaluated 

    Schema schema = pmml.getSchema();
    String[] param_names = schema.getParamNames();
    List<String> params = new ArrayList<String>();

    for( int i = 0; i < param_names.length; i++ )
      params.add( param_names[ i ] );

    // walk the node list to construct serializable predicates

    NodeList child_nodes = node.getChildNodes();

    for( int i = 0; i < child_nodes.getLength(); i++ )
      {
      Node child = child_nodes.item( i );

      if( child.getNodeType() == Node.ELEMENT_NODE )
        {
        if( "SimplePredicate".equals( child.getNodeName() ) || "SimpleSetPredicate".equals( child.getNodeName() ) )
          {
          Integer predicate_id = shared_context.makePredicate( schema, pmml.getReader(), (Element) child, params );

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

          buildNode( pmml, shared_context, (Element) child, child_vertex, graph );
          }
        }
      }
    }

  /**
   * @param node predicate node in the XML
   * @param graph serializable graph
   * @return Vertex
   */
  protected Vertex makeVertex( Element node, DirectedGraph<Vertex, Edge> graph )
    {
    String id = ( node ).getAttribute( "id" );
    Vertex vertex = new Vertex( id );
    graph.addVertex( vertex );

    return vertex;
    }

  /** @return String  */
  @Override
  public String toString()
    {
    StringBuilder buf = new StringBuilder();

    if( schema != null )
      {
      buf.append( schema );
      buf.append( "\n" );
      buf.append( "---------" );
      buf.append( "\n" );
      }

    if( context != null )
      {
      buf.append( context );
      buf.append( "\n" );
      buf.append( "---------" );
      buf.append( "\n" );
      }

    buf.append( tree );
    buf.append( tree.getRoot() );

    for( Edge edge : tree.getGraph().edgeSet() )
      buf.append( edge );

    buf.append( "\n" );

    return buf.toString();
    }
  }
