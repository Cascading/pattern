/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.datafield;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import pattern.PatternException;
import pattern.XPathReader;


public class DataFieldFactory
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( DataFieldFactory.class );

  /**
   * Create the appropriate DataField object based on parsing the data dictionary in a PMML file.
   *
   * @param reader
   * @param node
   * @param name
   * @param op_type
   * @param data_type
   * @return DataField
   * @throws PatternException
   */
  public static DataField getDataField( XPathReader reader, Node node, String name, String op_type, String data_type ) throws PatternException
    {
    DataField df = null;

    if( "continuous".equals( op_type ) && "double".equals( data_type ) )
      df = new ContinuousDataField( name, op_type, data_type );
    else if( "categorical".equals( op_type ) && "string".equals( data_type ) )
      {
      df = new CategoricalDataField( name, op_type, data_type );
      df.parse( reader, node );
      }
    else
      {
      String message = String.format( "unsupported DataField type %s / %s: ", op_type, data_type );
      LOG.error( message );
      throw new PatternException( message );
      }

    return df;
    }
  }
