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

package cascading.pattern;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cascading.PlatformTestCase;
import cascading.pattern.util.Logging;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class PatternPlatformTestCase extends PlatformTestCase
  {
  private static final Logger LOG = LoggerFactory.getLogger( PatternPlatformTestCase.class );

  public static void enableLogging( String log, String level )
    {
    Logging.setLogLevel( PatternPlatformTestCase.class, log, level );
    }

  public static void assertEquals( List<Tuple> lhs, List<Tuple> rhs, double delta )
    {
    assertEquals( lhs, rhs, delta, null, null );
    }

  public static void assertEquals( List<Tuple> lhs, List<Tuple> rhs, double delta, final Fields skipFields, final Tuple[] skip )
    {
    assertEquals( "not same number of results", lhs.size(), rhs.size() );

    Collections.sort( lhs );
    Collections.sort( rhs );

    Iterator<Tuple> lhsIterator = lhs.iterator();
    Iterator<Tuple> rhsIterator = rhs.iterator();

    if( skip != null )
      {
      lhsIterator = Iterators.filter( lhsIterator, new Predicate<Tuple>()
      {
      @Override
      public boolean apply( Tuple tuple )
        {
        for( Tuple skipTuple : skip )
          {
          Tuple value = tuple.get( skipFields.getPos() );

          if( value.equals( skipTuple ) )
            {
            LOG.debug( "skipping: lhs: {}", tuple );
            return false;
            }
          }
        return true;
        }
      } );

      rhsIterator = Iterators.filter( rhsIterator, new Predicate<Tuple>()
      {
      @Override
      public boolean apply( Tuple tuple )
        {
        for( Tuple skipTuple : skip )
          {
          Tuple value = tuple.get( skipFields.getPos() );

          if( value.equals( skipTuple ) )
            {
            LOG.debug( "skipping: rhs: {}", tuple );

            return false;
            }
          }
        return true;
        }
      } );
      }

    while( lhsIterator.hasNext() )
      {
      Tuple lhsTuple = lhsIterator.next();
      Tuple rhsTuple = rhsIterator.next();

      assertEquals( "not same size", lhsTuple.size(), rhsTuple.size() );

      try
        {
        for( int i = 0; i < lhsTuple.size(); i++ )
          {
          Object lhsObject = lhsTuple.getObject( i );
          Object rhsObject = rhsTuple.getObject( i );

          if( lhsObject instanceof Double )
            assertEquals( (Double) lhsObject, (Double) rhsObject, delta );
          else if( lhsObject instanceof Float )
            assertEquals( (Float) lhsObject, (Float) rhsObject, delta );
          else
            assertEquals( lhsObject, rhsObject );
          }
        }
      catch( AssertionFailedError exception )
        {
        LOG.error( "actual error", exception );
        throw new ComparisonFailure( "tuples not equal", lhsTuple.toString(), rhsTuple.toString() );
        }
      }
    }
  }
