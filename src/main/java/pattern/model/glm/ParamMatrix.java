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
 * This Class represents ParamMatrix
 */
public class ParamMatrix extends LinkedHashMap<String,ArrayList<PCell>> implements Serializable
  {
  private static final Logger LOG = LoggerFactory.getLogger( PPMatrix.class );

  /**
   * Parse the data dictionary from PMML.
   *
   * @param node_list list of DataField nodes in the DataDictionary.
   * @throws pattern.PatternException
   */
  public void parsePCell( NodeList node_list ) throws PatternException
    {
    for( int i = 0; i < node_list.getLength(); i++ )
      {
      Node node = node_list.item( i );

      if( node.getNodeType() == Node.ELEMENT_NODE )
        {
        String name = ( (Element) node ).getAttribute( "parameterName" );
        String predictorName = ( (Element) node ).getAttribute( "beta" );
        String df = ( (Element) node ).getAttribute( "df" );
        LOG.info(name);

        if( !containsKey( name ) )
          {
          ArrayList<PCell> arrPCell;
          arrPCell = new ArrayList<PCell>();
          PCell pCell = new PCell();
          pCell.setParameterName( name );
          pCell.setBeta( predictorName );
          pCell.setDf( df );
          arrPCell.add( pCell );
          put( name, arrPCell );
          LOG.debug( "PMML add DataField: " + arrPCell.toString() );
          }
        else
          {
          PCell pCell = new PCell();
          pCell.setParameterName( name );
          pCell.setBeta( predictorName );
          pCell.setDf( df );
          ArrayList<PCell> arrPCell = get( name );
          arrPCell.add( pCell );
          put( name, arrPCell );
          LOG.debug( "PMML add DataField: " + arrPCell.toString() );
          }
        }
      }
    }
  }
