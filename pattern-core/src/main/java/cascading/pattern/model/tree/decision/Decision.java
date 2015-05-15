/*
 * Copyright (c) 2007-2015 Concurrent, Inc. All Rights Reserved.
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

package cascading.pattern.model.tree.decision;

import java.util.List;

import cascading.pattern.model.tree.Node;
import cascading.pattern.model.tree.Tree;
import cascading.tuple.TupleEntry;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.KShortestPaths;

/**
 *
 */
public abstract class Decision
  {
  protected final String name;

  public Decision( Tree tree, Node node )
    {
    this.name = createName( tree, node );
    }

  public String getName()
    {
    return name;
    }

  protected String createName( Tree tree, Node node )
    {
    if( tree.getRoot() == node )
      return node.getID();

    List<GraphPath<Node, Integer>> paths = new KShortestPaths<Node, Integer>( tree.getGraph(), tree.getRoot(), 1 ).getPaths( node );

    List<Node> predecessors = Graphs.getPathVertexList( paths.get( 0 ) );

    predecessors.remove( node );

    String name = "";

    for( Node predecessor : predecessors )
      name += predecessor.getID() + ".";

    name += node.getID();

    return name;
    }

  protected abstract FinalDecision decide( TupleEntry tupleEntry );

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "Decision{" );
    toString( sb );
    sb.append( '}' );
    return sb.toString();
    }

  protected StringBuilder toString( StringBuilder sb )
    {
    return sb.append( "name='" ).append( name ).append( '\'' );
    }
  }
