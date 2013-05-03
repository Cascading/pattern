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

import cascading.pattern.model.tree.Node;
import cascading.pattern.model.tree.Tree;
import cascading.pattern.model.tree.predicate.Predicate;
import cascading.pattern.model.tree.predicate.SimplePredicate;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;
import cascading.tuple.util.TupleViews;

/**
 *
 */
class PredicatedDecision extends ParentDecision
  {
  private final Predicate predicate;
  private TupleEntry narrowEntry;

  public PredicatedDecision( String[] categories, Fields expectedFields, Tree tree, Node node )
    {
    super( categories, expectedFields, tree, node );

    this.predicate = node.getPredicate();

    // allows simple predicates to pull the first pos without a field name lookup
    if( this.predicate instanceof SimplePredicate )
      {
      Fields fields = new Fields( ( (SimplePredicate) this.predicate ).getField() );
      int[] pos = expectedFields.getPos( fields );
      narrowEntry = new TupleEntry( fields, TupleViews.createNarrow( pos ) );
      }
    }

  @Override
  protected FinalDecision decide( TupleEntry tupleEntry )
    {
    TupleEntry entry = narrowEntry != null ? resetNarrow( tupleEntry ) : tupleEntry;

    boolean result = predicate.evaluate( entry );

    if( !result )
      return null;

    return super.decide( tupleEntry );
    }

  private TupleEntry resetNarrow( TupleEntry tupleEntry )
    {
    TupleViews.reset( narrowEntry.getTuple(), tupleEntry.getTuple() );

    return narrowEntry;
    }
  }
