/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 *
 * @author girish.kathalagiri
 */

package pattern.model.glm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pattern.PatternException;


/**
 * This Class represents PPMatrix
 */
public class PPMatrix extends LinkedHashMap<String, ArrayList<PPCell>> implements Serializable
  {
  private static final Logger LOG = LoggerFactory.getLogger( PPMatrix.class );

  /**
   * Parse the data dictionary from PMML.
   *
   * @param node_list list of DataField nodes in the DataDictionary.
   * @throws pattern.PatternException
   */
  public void parsePPCell( NodeList node_list ) throws PatternException
    {
    for( int i = 0; i < node_list.getLength(); i++ )
      {
      Node node = node_list.item( i );

      if( node.getNodeType() == Node.ELEMENT_NODE )
        {
        String name = ( (Element) node ).getAttribute( "parameterName" );
        String predictorName = ( (Element) node ).getAttribute( "predictorName" );
        String value = ( (Element) node ).getAttribute( "value" );

        if( !containsKey( name ) )
          {
          ArrayList<PPCell> arrPPCell = new ArrayList<PPCell>();
          PPCell ppCell = new PPCell();
          ppCell.setParameterName( name );
          ppCell.setPredictorName( predictorName );
          ppCell.setValue( value );
          arrPPCell.add( ppCell );
          put( name, arrPPCell );
          LOG.debug( "PMML add DataField: " + arrPPCell.toString() );
          }
        else
          {
          PPCell ppCell = new PPCell();
          ppCell.setParameterName( name );
          ppCell.setPredictorName( predictorName );
          ppCell.setValue( value );
          ArrayList<PPCell> arrPPCell = get( name );
          arrPPCell.add( ppCell );
          put( name, arrPPCell );
          LOG.debug( "PMML add DataField: " + arrPPCell.toString() );
          }
        }
      }
    }
  }
