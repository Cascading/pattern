/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.datafield;

import java.io.Serializable;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PortableBitSet extends BitSet implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( PortableBitSet.class );

  protected long max_size = 0L;

  public PortableBitSet( int nbits )
    {
    super( nbits );
    max_size = nbits;
    }

  public PortableBitSet( String bit_str )
    {
    super( bit_str.length() );
    max_size = bit_str.length();

    for( int i = 0; i < max_size; i++ )
      if( bit_str.charAt( i ) == '1' )
        set( i );
    }

  /**
   * @param bits
   * @return boolean
   */
  public static boolean isIn ( String bit_str, int i )
    {
    boolean result = false;

    if( ( i >= 0 ) && ( i < bit_str.length() ) )
      {
      PortableBitSet bits = new PortableBitSet( bit_str );
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
