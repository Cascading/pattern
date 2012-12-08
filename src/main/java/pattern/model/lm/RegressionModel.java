/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.model.lm;

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
import pattern.predictor.Predictor;
import pattern.predictor.PredictorFactory;
import pattern.PatternException;
import pattern.PMML;
import pattern.Schema;
import pattern.model.Model;


public class RegressionModel extends Model implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( RegressionModel.class );

  public Double intercept = 0.0;
  public List<Predictor> predictors = new ArrayList<Predictor>();

  /**
   * Constructor for a RegressionModel as a standalone classifier (PMML
   * versions 1-3).
   *
   * @param pmml PMML model
   * @throws PatternException
   */
  public RegressionModel( PMML pmml ) throws PatternException
    {
    schema = pmml.getSchema();
    schema.parseMiningSchema( pmml.getNodeList( "/PMML/RegressionModel/MiningSchema/MiningField" ) );

    String node_expr = "/PMML/RegressionModel/RegressionTable[1]";
    Element model_node = (Element) pmml.getNodeList( node_expr ).item( 0 );

    intercept = Double.valueOf( model_node.getAttribute( "intercept" ) );
    LOG.info( "Intercept: " + intercept );

    NodeList child_nodes = model_node.getChildNodes();

    for( int i = 0; i < child_nodes.getLength(); i++ )
      {
      Node child = child_nodes.item( i );

      if( child.getNodeType() == Node.ELEMENT_NODE )
        {
        Predictor pred = PredictorFactory.getPredictor( schema, (Element) child );
        predictors.add( pred );
        LOG.info( pred.toString() );
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
    double result = intercept;

    for( Predictor pred : predictors )
      {
      double term = pred.calcTerm( param_map );
      result += term;
      }

    LOG.debug( "result: " + result );

    return Double.toString( result );
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
      buf.append( predictors );
      buf.append( "---------" );
      buf.append( "\n" );
      }

    return buf.toString();
    }
  }
