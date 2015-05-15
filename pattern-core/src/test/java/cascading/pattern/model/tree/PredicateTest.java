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

package cascading.pattern.model.tree;

import cascading.CascadingTestCase;
import cascading.pattern.model.tree.decision.PredicateEvaluator;
import cascading.pattern.model.tree.predicate.EqualsToPredicate;
import cascading.pattern.model.tree.predicate.IsMissingPredicate;
import cascading.pattern.model.tree.predicate.IsNotMissingPredicate;
import cascading.pattern.model.tree.predicate.Predicate;
import cascading.pattern.model.tree.predicate.TruePredicate;
import cascading.pattern.model.tree.predicate.compound.AndPredicate;
import cascading.pattern.model.tree.predicate.compound.OrPredicate;
import cascading.pattern.model.tree.predicate.compound.SurrogatePredicate;
import cascading.pattern.util.Logging;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import org.junit.Test;

/**
 *
 */
public class PredicateTest extends CascadingTestCase
  {
  private Fields expectedFields;
  private TupleEntry tupleEntry;

  public PredicateTest()
    {
    }

  @Override
  public void setUp() throws Exception
    {
    Logging.setLogLevel( this.getClass(), "cascading.pattern", "debug" );

    super.setUp();

    expectedFields = Fields.NONE
      .append( new Fields( "var0", double.class ) )
      .append( new Fields( "var1", String.class ) )
      .append( new Fields( "var2", double.class ) );

    Tuple tuple = new Tuple( 0.d, "value", null );

    tupleEntry = new TupleEntry( expectedFields, tuple );
    }

  @Test
  public void testAnd()
    {
    Predicate andPredicate = new AndPredicate( new IsNotMissingPredicate( "var0" ), new EqualsToPredicate( "var1", "value" ) );

    assertTrue( new PredicateEvaluator( expectedFields, andPredicate ).evaluate( tupleEntry ) );
    }

  @Test
  public void testOr()
    {
    Predicate orPredicate = new AndPredicate( new IsNotMissingPredicate( "var2" ), new EqualsToPredicate( "var1", "value" ) );

    assertTrue( !new PredicateEvaluator( expectedFields, orPredicate ).evaluate( tupleEntry ) );
    }

  @Test
  public void testComplex()
    {
    Predicate complexPredicate = new AndPredicate(
      new IsNotMissingPredicate( "var0" ),
      new OrPredicate( new EqualsToPredicate( "var1", "value" ), new IsMissingPredicate( "var2" ) ) );

    assertTrue( new PredicateEvaluator( expectedFields, complexPredicate ).evaluate( tupleEntry ) );
    }

  @Test
  public void testSurrogate()
    {
    Predicate surrogatePredicate = new SurrogatePredicate(
      new EqualsToPredicate( "var2", 0.0d ),
      new EqualsToPredicate( "var2", 0.0d ),
      new TruePredicate() );

    assertTrue( new PredicateEvaluator( expectedFields, surrogatePredicate ).evaluate( tupleEntry ) );
    }
  }
