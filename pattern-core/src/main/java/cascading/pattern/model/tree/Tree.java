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
import java.util.HashMap;
import java.util.Map;

import cascading.pattern.model.tree.decision.DecisionTree;
import cascading.pattern.model.tree.predicate.Predicate;
import cascading.tuple.Fields;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Tree implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( Tree.class );

  private transient Map<String, Node> nodes = new HashMap<String, Node>();
  public SimpleDirectedGraph<Node, Integer> graph = new SimpleDirectedGraph<Node, Integer>( Integer.class );
  public Node root;

  private int count = 0;

  public Tree( String id )
    {
    root = new Node( id );
    nodes.put( root.getID(), root );
    graph.addVertex( root );
    }

  public Node getRoot()
    {
    return root;
    }

  public DirectedGraph<Node, Integer> getGraph()
    {
    return graph;
    }

  public void addPredicate( String from, String to, Predicate predicate )
    {
    addPredicate( from, to, predicate, null );
    }

  public void addPredicate( String from, String to, Predicate predicate, String score )
    {
    if( nodes.containsKey( to ) )
      throw new IllegalArgumentException( "duplicate node name: " + to );

    Node toNode = new Node( to, predicate, score );

    nodes.put( to, toNode );
    graph.addVertex( toNode );
    graph.addEdge( nodes.get( from ), toNode, count++ );
    }

  public DecisionTree createDecisionTree( Fields argumentFields )
    {
    return new DecisionTree( argumentFields, this, this.getRoot() );
    }
  }
