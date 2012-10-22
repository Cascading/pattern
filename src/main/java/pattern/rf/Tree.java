/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
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

package pattern.rf;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.DirectedGraph;
import java.io.Serializable;

 
public class Tree implements Serializable
{
  public String tree_name;
  public Vertex root;
  public DirectedGraph<Vertex, Edge> graph = new DefaultDirectedGraph<Vertex, Edge>(Edge.class);


  public Tree ( String id ) throws Exception {
      tree_name = "tree_" + id;

      /* */
      System.out.println( tree_name );
      /* */
  }


  public void setRoot ( Vertex root ) {
      this.root = root;
  }


  public Vertex getRoot () {
      return root;
  }


  public DirectedGraph<Vertex, Edge> getGraph () {
      return graph;
  }


  public String toString () {
      return tree_name + ": " + graph;
  }


  public String traverse ( Boolean[] pred ) {
      return traverseVertex( root, pred );
  }


  protected String traverseVertex ( Vertex vertex, Boolean[] pred ) {
      String score = vertex.getScore();

      if ( score != null ) {
	  /** /
	  System.out.println( "  then " + score );
	  /* */

	  return score;
      }

      for ( Edge edge : graph.outgoingEdgesOf( vertex ) ) {
	  /** /
	  System.out.println( edge );
	  System.out.println( " if pred[ " + edge.getPredicateId() + " ]:" + pred[ edge.getPredicateId() ] );
	  /* */

	  if ( pred[ edge.getPredicateId() ] ) {
	      score = traverseVertex( graph.getEdgeTarget( edge ), pred );

	      if ( score != null ) {
		  return score;
	      }
	  }
      }

      return null;
  }
}
