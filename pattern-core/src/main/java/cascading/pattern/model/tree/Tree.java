/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cascading.pattern.model.tree;

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
          return score;
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
