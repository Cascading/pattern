/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class Classifier implements Serializable
  {
  protected transient XPathReader reader;
  public Map<String, DataField> schema = new LinkedHashMap<String, DataField>();

  /**
   * @param fields
   * @return
   * @throws PatternException
   */
  public abstract String classifyTuple( String[] fields ) throws PatternException;

  /** Build the data dictionary */
  protected void buildSchema()
    {
    String expr = "/PMML/DataDictionary/DataField";
    NodeList node_list = (NodeList) reader.read( expr, XPathConstants.NODESET );

    for( int i = 0; i < node_list.getLength(); i++ )
      {
      Node node = node_list.item( i );

      if( node.getNodeType() == Node.ELEMENT_NODE )
        {
        String name = ( (Element) node ).getAttribute( "name" );
        String data_type = ( (Element) node ).getAttribute( "dataType" );

        if( !schema.containsKey( name ) )
          schema.put( name, new DataField( name, data_type ) );
        }
      }

    // determine the active tuple fields for the input schema

    expr = "/PMML/MiningModel/MiningSchema/MiningField";
    node_list = (NodeList) reader.read( expr, XPathConstants.NODESET );

    for( int i = 0; i < node_list.getLength(); i++ )
      {
      Node node = node_list.item( i );

      if( node.getNodeType() == Node.ELEMENT_NODE )
        {
        String name = ( (Element) node ).getAttribute( "name" );
        String usage_type = ( (Element) node ).getAttribute( "usageType" );

        if( schema.containsKey( name ) && !"active".equals( usage_type ) )
          schema.remove( name );
        }
      }
    }
  }
