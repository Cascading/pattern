/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
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

package pattern;

import java.io.Serializable;
import java.util.LinkedHashMap;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

 
public abstract class Classifier implements Serializable
{
  protected transient XPathReader reader;
  public LinkedHashMap<String, DataField> schema = new LinkedHashMap<String, DataField>();


  public abstract String classifyTuple( String[] fields );


  protected void buildSchema () {
      // build the data dictionary

      String expr = "/PMML/DataDictionary/DataField";
      NodeList node_list = (NodeList) reader.read( expr, XPathConstants.NODESET );

      for ( int i = 0; i < node_list.getLength(); i++ ) {
	  Node node = node_list.item( i );

	  if ( node.getNodeType() == Node.ELEMENT_NODE ) {
	      String name = ( (Element) node ).getAttribute( "name" );
	      String data_type = ( (Element) node ).getAttribute( "dataType" );

	      if ( !schema.containsKey( name ) ) {
		  schema.put( name, new DataField( name, data_type ) );
	      }
	  }
      }

      // determine the active tuple fields for the input schema

      expr = "/PMML/MiningModel/MiningSchema/MiningField";
      node_list = (NodeList) reader.read( expr, XPathConstants.NODESET );

      for ( int i = 0; i < node_list.getLength(); i++ ) {
	  Node node = node_list.item( i );

	  if ( node.getNodeType() == Node.ELEMENT_NODE ) {
	      String name = ( (Element) node ).getAttribute( "name" );
	      String usage_type = ( (Element) node ).getAttribute( "usageType" );

	      if ( schema.containsKey( name ) && !"active".equals( usage_type ) ) {
		  schema.remove( name );
	      }
	  }
      }
  }
}
