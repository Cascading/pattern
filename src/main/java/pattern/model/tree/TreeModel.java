/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.model.tree;

import java.io.Serializable;
import javax.xml.xpath.XPathConstants;

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

  public Schema schema;
  public Context context;

  public Tree tree;

  /**
   * @param pmml PMML model
   * @param context tree context
   * @throws PatternException
   */
  public TreeModel( PMML pmml ) throws PatternException
    {
    this.schema = pmml.getSchema();
    this.context = new Context();

    schema.parseMiningSchema( pmml.getNodeList( "/PMML/TreeModel/MiningSchema/MiningField" ) );
    tree = new Tree( "default" );

    String node_expr = "./TreeModel/Node[1]";
    NodeList root_node = pmml.getNodeList( node_expr );

    buildTree( pmml, (Element) root_node.item( 0 ), tree );
    }

  /**
   * Constructor for a TreeModel as part of an ensemble, such as in
   * Random Forest.
   *
   * @param pmml PMML model
   * @param parent parent node in the XML
   * @param context tree context
   * @throws PatternException
   */
  public TreeModel( PMML pmml, Node parent, Context context ) throws PatternException
    {
    this.schema = pmml.getSchema();
    this.context = context;

    String id = ( (Element) parent ).getAttribute( "id" );
    tree = new Tree( id );

    String node_expr = "./TreeModel/Node[1]";
    NodeList root_node = (NodeList) pmml.getReader().read( parent, node_expr, XPathConstants.NODESET );

    buildTree( pmml, (Element) root_node.item( 0 ), tree );
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
   * @param values tuple values
   * @return String
   * @throws PatternException
   */
  @Override
  public String classifyTuple( Tuple values ) throws PatternException
    {
    return "null";
    }

  /**
   * Generate a serializable graph representation for a tree.
   *
   * @param pmml PMML model
   * @param root root node in the XML
   * @param tree serializable tree structure
   * @throws PatternException
   */
  public void buildTree( PMML pmml, Element root, Tree tree ) throws PatternException
    {
    Vertex vertex = makeVertex( root, tree.getGraph() );
    tree.setRoot( vertex );

    buildNode( pmml, root, vertex, tree.getGraph() );
    }

  /**
   * @param pmml PMML model
   * @param node predicate node in the XML
   * @param vertex tree vertex
   * @param graph serializable graph
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
          Integer predicate_id = context.makePredicate( schema, pmml.getReader(), (Element) child );

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

    buf.append( "---------" );
    buf.append( "\n" );
    buf.append( schema );
    buf.append( "\n" );
    buf.append( "---------" );
    buf.append( "\n" );
    buf.append( tree );
    buf.append( "\n" );

    return buf.toString();
    }
  }
