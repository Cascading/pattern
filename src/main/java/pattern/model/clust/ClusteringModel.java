/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.model.clust;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cascading.tuple.Tuple;
import pattern.PatternException;
import pattern.PMML;
import pattern.Schema;
import pattern.model.Model;


public class ClusteringModel extends Model implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( ClusteringModel.class );

  public List<Exemplar> exemplars = new ArrayList<Exemplar>();

  /**
   * Constructor for a ClusteringModel as a standalone classifier (PMML
   * versions 1-3).
   *
   * @param pmml PMML model
   * @throws PatternException
   */
  public ClusteringModel( PMML pmml ) throws PatternException
    {
    schema = pmml.getSchema();
    schema.parseMiningSchema( pmml.getNodeList( "/PMML/ClusteringModel/MiningSchema/MiningField" ) );

    String node_expr = "/PMML/ClusteringModel/Cluster";
    NodeList node_list = pmml.getNodeList( node_expr );

    for( int i = 0; i < node_list.getLength(); i++ )
      {
      Node node = node_list.item( i );

      if( node.getNodeType() == Node.ELEMENT_NODE )
        {
        Element node_elem = (Element) node;

        if( "Cluster".equals( node_elem.getNodeName() ) )
          exemplars.add( new Exemplar( node_elem ) );
        }
      }
    }

  /**
   * Prepare to classify with this model. Called immediately before
   * the enclosing Operation instance is put into play processing
   * Tuples.
   */
  @Override
  public void prepare()
    {
    // not needed
    }

  /**
   * Classify an input tuple, returning the predicted label.
   *
   * @param values tuple values
   * @return String
   * @throws PatternException
   */
  @Override
  public String classifyTuple( Tuple values ) throws PatternException
    {
    Map<String, Object> param_map = schema.getParamMap( values );
    String[] param_names = schema.getParamNames();
    Double[] param_values = new Double[ param_names.length ];

    for( int i = 0; i < param_names.length; i++ )
      param_values[ i ] = (Double) param_map.get( param_names[ i ] );

    Exemplar best_clust = null;
    double best_dist = 0.0;

    for( Exemplar clust : exemplars )
      {
      double distance = clust.calcDistance( param_values );

      if( ( best_clust == null ) || ( distance < best_dist ) )
        {
        best_clust = clust;
        best_dist = distance;
        }
      }

    return best_clust.name;
    }

  /** @return String  */
  @Override
  public String toString()
    {
    StringBuilder buf = new StringBuilder();

    if( schema != null )
      {
      buf.append( schema );
      buf.append( "\n" );
      buf.append( "---------" );
      buf.append( "\n" );
      buf.append( exemplars );
      buf.append( "---------" );
      buf.append( "\n" );
      }

    return buf.toString();
    }
  }
