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

package cascading.pattern.datafield;

import java.io.Serializable;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PortableBitSet extends BitSet implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( PortableBitSet.class );

  protected long max_size = 0L;

  public PortableBitSet( int max_size )
    {
    super( max_size );
    this.max_size = max_size;
    }

  public PortableBitSet( String bitStr )
    {
    super( bitStr.length() );
    max_size = bitStr.length();

    for( int i = 0; i < max_size; i++ )
      if( bitStr.charAt( i ) == '1' )
        set( i );
    }

  /**
   */
  public static boolean isIn( String bitStr, int i )
    {
    boolean result = false;

    if( ( i >= 0 ) && ( i < bitStr.length() ) )
      {
      PortableBitSet bits = new PortableBitSet( bitStr );
      result = bits.get( i );
      }

    return result;
    }

  /** @return  */
  @Override
  public String toString()
    {
    StringBuilder sb = new StringBuilder();

    for( int i = 0; i < max_size; i++ )
      sb.append( get( i ) ? "1" : "0" );

    return sb.toString();
    }
  }
