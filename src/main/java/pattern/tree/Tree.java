/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.tree;

import java.io.Serializable;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Tree implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( Tree.class );

  public String tree_name;
  public Vertex root;
  public DirectedGraph<Vertex, Edge> graph = new DefaultDirectedGraph<Vertex, Edge>( Edge.class );

  /** @param id  */
  public Tree( String id )
    {
    tree_name = "tree_" + id;

    if( LOG.isDebugEnabled() )
      LOG.debug( tree_name );
    }

  /** @param root  */
  public void setRoot( Vertex root )
    {
    this.root = root;
    }

  /** @return  */
  public Vertex getRoot()
    {
    return root;
    }

  /** @return  */
  public DirectedGraph<Vertex, Edge> getGraph()
    {
    return graph;
    }

  /**
   * @param pred_eval
   * @return
   */
  public String traverse( Boolean[] pred_eval )
    {
    return traverseVertex( root, pred_eval );
    }

  /**
   * @param vertex
   * @param pred_eval
   * @return
   */
  protected String traverseVertex( Vertex vertex, Boolean[] pred_eval )
    {
    String score = vertex.getScore();

    if( score != null )
      {
      if( LOG.isDebugEnabled() )
        LOG.debug( "  then " + score );

      return score;
      }

    for( Edge edge : graph.outgoingEdgesOf( vertex ) )
      {
      if( LOG.isDebugEnabled() )
      {
        LOG.debug( edge.toString() );
        LOG.debug( " if pred_eval[ " + edge.getPredicateId() + " ]:" + pred_eval[ edge.getPredicateId() ] );
      }

      if( pred_eval[ edge.getPredicateId() ] )
        {
        score = traverseVertex( graph.getEdgeTarget( edge ), pred_eval );

        if( score != null )
          {
          return score;
          }
        }
      }

    return null;
    }

  /** @return  */
  @Override
  public String toString()
    {
    return tree_name + ": " + graph;
    }
  }
