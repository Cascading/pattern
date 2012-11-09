/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.io.Serializable;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cascading.tuple.Tuple;


public class Schema extends LinkedHashMap<String, DataField> implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( Schema.class );

  /**
   * Returns the expected name for each field in the Tuple, to be
   * used as Janino parameters.
   * @return
   */
  public String[] getParamNames()
    {
    return keySet().toArray( new String[ 0 ] );
    }

  /**
   * Returns the expected class for each field in the Tuple, to be
   * used as Janino parameters.
   * @return
   */
  public Class[] getParamTypes()
    {
    Class[] param_types = new Class[ size() ];

    for( int i = 0; i < size(); i++ )
     {
      param_types[ i ] = double.class;
      // System.out.println( param_types[ i ] );
      // param_types[ i ] = schema.get( i ).getClassType();
      // System.out.println( param_types[ i ] );
     }

    return param_types;
    }

  /**
   * Convert values for the fields in the Tuple, in a form that Janino expects.
   *
   * @param values
   * @param param_values
   * @throws PatternException
   */
  public void setParamValues( Tuple values, Object[] param_values ) throws PatternException
    {
    for( int i = 0; i < size(); i++ )
      try
        {
        param_values[ i ] = values.getDouble( i );
        }
      catch( NumberFormatException exception )
        {
        LOG.error( "tuple format is bad", exception );
        throw new PatternException( "tuple format is bad", exception );
        }
    }
  }
