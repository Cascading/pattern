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

import cascading.pattern.model.tree.predicate.Predicate;
import cascading.pattern.model.tree.predicate.SimplePredicate;
import cascading.pattern.model.tree.predicate.compound.CompoundPredicate;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 *
 */
public class PredicateEvaluator
  {
  private final Invoker invoker;

  protected interface Invoker
    {
    Boolean invoke( TupleEntry tupleEntry );
    }

  protected class PredicateInvoker implements Invoker
    {
    private final SimplePredicate predicate;
    private final int pos;

    public PredicateInvoker( SimplePredicate predicate, int pos )
      {
      this.predicate = predicate;
      this.pos = pos;
      }

    @Override
    public Boolean invoke( TupleEntry tupleEntry )
      {
      return predicate.evaluate( tupleEntry.getObject( pos ) );
      }
    }

  protected class NoArgPredicateInvoker implements Invoker
    {
    private final SimplePredicate predicate;

    public NoArgPredicateInvoker( SimplePredicate predicate )
      {
      this.predicate = predicate;
      }

    @Override
    public Boolean invoke( TupleEntry tupleEntry )
      {
      return predicate.evaluate( null );
      }
    }

  protected class CompoundPredicateInvoker implements Invoker
    {
    private final CompoundPredicate predicate;
    private final ImmutableList<Invoker> invokers;

    public CompoundPredicateInvoker( CompoundPredicate predicate, Invoker[] invokers )
      {
      this.predicate = predicate;
      this.invokers = ImmutableList.<Invoker>builder().add( invokers ).build();
      }

    @Override
    public Boolean invoke( final TupleEntry tupleEntry )
      {
      Iterable<Boolean> iterable = Lists.transform( invokers, new Function<Invoker, Boolean>()
      {
      @Override
      public Boolean apply( Invoker invoker )
        {
        return invoker.invoke( tupleEntry );
        }
      } );

      return predicate.evaluate( iterable.iterator() );
      }
    }

  public PredicateEvaluator( Fields expectedFields, Predicate predicate )
    {
    this.invoker = createInvokers( expectedFields, predicate );
    }

  private Invoker createInvokers( Fields expectedFields, Predicate predicate )
    {
    if( ( predicate instanceof SimplePredicate ) )
      {
      SimplePredicate simplePredicate = (SimplePredicate) predicate;
      String argumentField = simplePredicate.getArgumentField();

      if( argumentField == null )
        return new NoArgPredicateInvoker( simplePredicate );
      else
        return new PredicateInvoker( simplePredicate, expectedFields.getPos( argumentField ) );
      }

    CompoundPredicate compoundPredicate = (CompoundPredicate) predicate;
    Predicate[] children = compoundPredicate.getChildren();

    Invoker[] invokers = new Invoker[ children.length ];

    for( int i = 0; i < children.length; i++ )
      invokers[ i ] = createInvokers( expectedFields, children[ i ] );

    return new CompoundPredicateInvoker( compoundPredicate, invokers );
    }

  public boolean evaluate( TupleEntry tupleEntry )
    {
    return invoker.invoke( tupleEntry );
    }
  }
