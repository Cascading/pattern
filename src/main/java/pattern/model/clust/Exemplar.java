/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.model.clust;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pattern.PatternException;


public class Exemplar implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( Exemplar.class );

  public List<Double> points = new ArrayList<Double>();
  public String name;

  /**
   * @param cluster_node node representing a cluster center
   * @throws PatternException
   */
  public Exemplar( Element cluster_node ) throws PatternException
    {
    //  <Cluster name="1">
    //   <Array n="4" type="real">5.006 3.428 1.462 0.246</Array>
    name = cluster_node.getAttribute( "name" );

    NodeList child_nodes = cluster_node.getChildNodes();

    for( int j = 0; j < child_nodes.getLength(); j++ )
      {
      Node child_node = child_nodes.item( j );

      if( child_node.getNodeType() == Node.ELEMENT_NODE )
        {
        Element child_elem = (Element) child_node;

        if( "Array".equals( child_elem.getNodeName() ) )
          {
          int n = Integer.valueOf( child_elem.getAttribute( "n" ) );
          String type = child_elem.getAttribute( "type" );
          String text = child_elem.getTextContent();

          for ( String val: text.split( "\\s+" ) )
            points.add( Double.valueOf( val ) );

          if( points.size() != n )
            {
            String message = String.format( "expected %d data points in PMML for cluster %s [ %s ]", n, name, text );
            LOG.error( message );
            throw new PatternException( message );
            }
          }
        }
      }
    }

  /**
   * Calculate the distance from this cluster for the given tuple.
   *
   * @param param_values array of tuple values
   * @return double
   */
  public double calcDistance( Double[] param_values )
    {
    double sum_sq = 0.0;

    for( int i = 0; i < param_values.length; i++ )
      sum_sq += Math.pow( param_values[ i ] - points.get( i ), 2.0 );

    return Math.sqrt( sum_sq );
    }

  /** @return String  */
  @Override
  public String toString()
    {
    return String.format( "Exemplar: %s %s", name, points.toString() );
    }
  }
