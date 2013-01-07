/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import pattern.datafield.DataField;
import pattern.datafield.DataFieldFactory;


public class Schema extends LinkedHashMap<String, DataField> implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( Schema.class );

  /** Field label_field - metadata of the label to be produced by the classifier */
  public DataField label_field;

  /**
   * Parse the data dictionary from PMML.
   *
   * @param pmml PMML model
   * @param node_list list of DataField nodes in the DataDictionary.
   * @throws PatternException
   */
  public void parseDictionary( PMML pmml, NodeList node_list ) throws PatternException
    {
    for( int i = 0; i < node_list.getLength(); i++ )
      {
      Node node = node_list.item( i );

      if( node.getNodeType() == Node.ELEMENT_NODE )
        {
        String name = ( (Element) node ).getAttribute( "name" );
        String op_type = ( (Element) node ).getAttribute( "optype" );
        String data_type = ( (Element) node ).getAttribute( "dataType" );

        if( !containsKey( name ) )
          {
          DataField df = DataFieldFactory.getDataField( pmml.getReader(), node, name, op_type, data_type );
          put( name, df );
          LOG.debug( "PMML add DataField: " + df );
          }
        }
      }
    }

  /**
   * Determine the active tuple fields for the input schema.
   *
   * @param node_list list of DataField nodes in the DataDictionary.
   * @throws PatternException
   */
  public void parseMiningSchema( NodeList node_list ) throws PatternException
    {
    for( int i = 0; i < node_list.getLength(); i++ )
      {
      Node node = node_list.item( i );

      if( node.getNodeType() == Node.ELEMENT_NODE )
        {
        String name = ( (Element) node ).getAttribute( "name" );
        String usage_type = ( (Element) node ).getAttribute( "usageType" );

        LOG.info( String.format( "DataField: %s:%s", name, usage_type ) );

        if( containsKey( name ) )
          {
          if( "predicted".equals( usage_type ) )
            label_field = remove( name );
          else if( !"active".equals( usage_type ) )
            remove( name );
          }
        else
          {
          String message = String.format( "unknown DataField referenced in PMML [ %s ]", name );
          LOG.error( message );
          throw new PatternException( message );
          }
        }
      }
    }

  /**
   * Returns a Fields data structure naming the input tuple fields.
   *
   * @return Fields
   */
  public Fields getInputFields()
    {
    Fields fields = new Fields();

    for( String name : keySet() )
      fields = fields.append( new Fields( name ) );

    return fields;
    }

  /**
   * Returns the expected name for each field in the Tuple, to be
   * used as Janino parameters.
   * @return String[]
   */
  public String[] getParamNames()
    {
    return keySet().toArray( new String[ 0 ] );
    }

  /**
   * Returns the expected class for each field in the Tuple, to be
   * used as Janino parameters.
   * @return Class[]
   */
  public Class[] getParamTypes()
    {
    Class[] param_types = new Class[ size() ];
    Iterator<DataField> iter = values().iterator();

    for( int i = 0; i < size(); i++ )
     {
     DataField df = iter.next();
     param_types[ i ] = df.getClassType();
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
    Iterator<DataField> iter = values().iterator();

    for( int i = 0; i < size(); i++ )
      {
      DataField df = iter.next();
      param_values[ i ] = df.getValue( values, i );
      }
    }

  /**
   * Returns a Map of names/values for each field in the Tuple.
   * @param values
   * @return Map<String, Object>
   * @throws PatternException
   */
  public Map<String, Object> getParamMap( Tuple values ) throws PatternException
    {
    HashMap<String, Object> param_map = new HashMap<String, Object>();
    Iterator<DataField> iter = values().iterator();

    for( int i = 0; i < size(); i++ )
      {
      DataField df = iter.next();
      param_map.put( df.name, df.getValue( values, i ) );
      }

    return param_map;
    }
  }
