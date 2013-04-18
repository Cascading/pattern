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

package cascading.pattern.pmml.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cascading.pattern.PatternException;
import cascading.pattern.model.MiningSchemaParam;
import cascading.pattern.model.tree.Edge;
import cascading.pattern.model.tree.Tree;
import cascading.pattern.model.tree.TreeContext;
import cascading.pattern.model.tree.Vertex;
import org.dmg.pmml.Node;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;
import org.dmg.pmml.TreeModel;
import org.jgrapht.DirectedGraph;

/**
 *
 */
public class TreeUtil
  {
  public static Tree createTree( TreeModel model, MiningSchemaParam schemaParam, TreeContext treeContext )
    {
    return createTree( "default", model, schemaParam, treeContext );
    }

  public static Tree createTree( String id, TreeModel model, MiningSchemaParam schemaParam, TreeContext treeContext )
    {
    Tree tree = new Tree( id );

    Node node = model.getNode();

    buildTree( schemaParam, treeContext, node, tree );

    return tree;
    }

  public static void buildTree( MiningSchemaParam schemaParam, TreeContext treeContext, Node node, Tree tree ) throws PatternException
    {
    Vertex vertex = makeVertex( tree.getGraph(), node.getId() );

    tree.setRoot( vertex );

    buildNode( schemaParam, treeContext, node, vertex, tree.getGraph() );
    }

  private static void buildNode( MiningSchemaParam schemaParam, TreeContext treeContext, Node node, Vertex vertex, DirectedGraph<Vertex, Edge> graph )
    {
    // build a list of parameters from which the predicate will be evaluated
    String[] paramNames = schemaParam.getParamNames();
    List<String> params = new ArrayList<String>();

    Collections.addAll( params, paramNames );

    // walk the node list to construct serializable predicates
    Predicate predicate = node.getPredicate();

    if( predicate instanceof SimplePredicate || predicate instanceof SimpleSetPredicate )
      {
      Integer predicateId = Predicates.makePredicate( treeContext, schemaParam, predicate, params );

      vertex.setScore( node.getScore() );

      for( Edge edge : graph.edgesOf( vertex ) )
        edge.setPredicateId( predicateId );
      }

    for( int i = 0; i < node.getNodes().size(); i++ )
      {
      Node child = node.getNodes().get( i );

      Vertex childVertex = makeVertex( graph, child.getId() );

      graph.addEdge( vertex, childVertex );

      buildNode( schemaParam, treeContext, child, childVertex, graph );
      }
    }

  private static Vertex makeVertex( DirectedGraph<Vertex, Edge> graph, String id )
    {
    Vertex vertex = new Vertex( id );
    graph.addVertex( vertex );
    return vertex;
    }
  }
