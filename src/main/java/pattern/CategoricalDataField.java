/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.util.ArrayList;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cascading.tuple.Tuple;


public class CategoricalDataField extends DataField
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( CategoricalDataField.class );

  public ArrayList<String> categories = new ArrayList<String>();

  /**
   * @param name
   * @param op_type
   * @param data_type
   */
  public CategoricalDataField( String name, String op_type, String data_type )
    {
    this.name = name;
    this.op_type = op_type;
    this.data_type = data_type;
    }

  /**
   * @param reader
   * @param node
   */
  @Override
  public void parse( XPathReader reader, Node node )
    {
    String node_expr = "./Value";
    NodeList node_list = (NodeList) reader.read( node, node_expr, XPathConstants.NODESET );

    for( int i = 0; i < node_list.getLength(); i++ )
      {
      Node child = node_list.item( i );

      if( child.getNodeType() == Node.ELEMENT_NODE )
        {
        String value = ( (Element) child ).getAttribute( "value" );
	LOG.debug( String.format( "PMML categorical value: %s", value ) );
        categories.add( value );
        }
      }
    }

  /** @return Class */
  public Class getClassType()
    {
    return String.class;
    }

  /**
   * @param values
   * @param i
   * @return Object
   */
  public Object getValue( Tuple values, int i )
    {
    return values.getString( i );
    }
  }
