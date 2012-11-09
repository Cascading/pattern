/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cascading.tuple.Tuple;


public class ContinuousDataField extends DataField
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( ContinuousDataField.class );

  /**
   * @param name
   * @param op_type
   * @param data_type
   */
  public ContinuousDataField( String name, String op_type, String data_type )
    {
    this.name = name;
    this.op_type = op_type;
    this.data_type = data_type;
    }

  /** @return Class  */
  public Class getClassType()
    {
    return double.class;
    }

  /**
   * @param values
   * @param i
   * @return Object
   * @throws PatternException
   */
  public Object getValue( Tuple values, int i ) throws PatternException
    {
    try
      {
      return values.getDouble( i );
      }
    catch( NumberFormatException exception )
      {
      LOG.error( "tuple format is bad", exception );
      throw new PatternException( "tuple format is bad", exception );
      }
    }
  }
