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
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;
import org.jgrapht.Graphs;

/**
 *
 */
abstract class ParentDecision extends Decision
  {
  protected final Decision[] successors;

  public ParentDecision( String[] categories, Fields expectedFields, Tree tree, Node node )
    {
    super( tree, node );

    this.successors = createSuccessors( categories, expectedFields, tree, node );
    }

  protected Decision[] createSuccessors( String[] categories, Fields expectedFields, Tree tree, Node node )
    {
    List<Node> successorNodes = Graphs.successorListOf( tree.getGraph(), node );

    if( successorNodes.size() == 0 )
      return new Decision[]{new FinalDecision( categories, tree, node )};

    Decision[] successors = new Decision[ successorNodes.size() ];

    for( int i = 0; i < successorNodes.size(); i++ )
      {
      Node successorNode = successorNodes.get( i );

      successors[ i ] = new PredicatedDecision( categories, expectedFields, tree, successorNode );
      }

    return successors;
    }

  protected FinalDecision decide( TupleEntry tupleEntry )
    {
    for( Decision child : successors )
      {
      FinalDecision decision = child.decide( tupleEntry );

      if( decision != null )
        return decision;
      }

    return null;
    }
  }
