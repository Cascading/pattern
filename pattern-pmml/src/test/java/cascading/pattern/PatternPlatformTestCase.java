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

package cascading.pattern;

import java.util.Iterator;
import java.util.List;

import cascading.PlatformTestCase;
import cascading.tuple.Tuple;
import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import org.apache.log4j.Level;

/**
 *
 */
public class PatternPlatformTestCase extends PlatformTestCase
  {
  public static void enableLogging( String log, String level )
    {
    org.apache.log4j.Logger.getLogger( log ).setLevel( Level.toLevel( level.toUpperCase() ) );
    }

  public static void assertEquals( List<Tuple> lhs, List<Tuple> rhs, double delta )
    {
    assertEquals( "not same number of results", lhs.size(), rhs.size() );

    Iterator<Tuple> lhsIterator = lhs.iterator();
    Iterator<Tuple> rhsIterator = rhs.iterator();

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
        throw new ComparisonFailure( "tuples not equal", lhsTuple.toString(), rhsTuple.toString() );
        }
      }
    }
  }
