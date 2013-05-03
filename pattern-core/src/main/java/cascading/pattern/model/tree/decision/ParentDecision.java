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

package cascading.pattern.model.tree.decision;

import java.util.List;

import cascading.pattern.model.tree.Node;
import cascading.pattern.model.tree.Tree;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;
import org.jgrapht.Graphs;

/**
 *
 */
abstract class ParentDecision extends Decision
  {
  protected final Decision[] children;

  public ParentDecision( String[] categories, Fields expectedFields, Tree tree, Node node )
    {
    super( tree, node );

    this.children = createChildren( categories, expectedFields, tree, node );
    }

  protected Decision[] createChildren( String[] categories, Fields expectedFields, Tree tree, Node node )
    {
    List<Node> successors = Graphs.successorListOf( tree.getGraph(), node );

    if( successors.size() == 0 )
      return new Decision[]{new FinalDecision( categories, tree, node )};

    Decision[] children = new Decision[ successors.size() ];

    for( int i = 0; i < successors.size(); i++ )
      {
      Node childNode = successors.get( i );

      children[ i ] = new PredicatedDecision( categories, expectedFields, tree, childNode );
      }

    return children;
    }

  protected FinalDecision decide( TupleEntry tupleEntry )
    {
    for( Decision child : children )
      {
      FinalDecision score = child.decide( tupleEntry );

      if( score != null )
        return score;
      }

    return null;
    }
  }
