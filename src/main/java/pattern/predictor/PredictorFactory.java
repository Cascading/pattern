/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.predictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import pattern.datafield.CategoricalDataField;
import pattern.PatternException;
import pattern.Schema;


public class PredictorFactory
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( PredictorFactory.class );

  /**
   * Create the appropriate Predictor object based on parsing the predictor terms (IV) of a regression model.
   *
   * @param schema model schema
   * @param node predictor node in the XML
   * @return Predictor
   * @throws PatternException
   */
  public static Predictor getPredictor( Schema schema, Element node ) throws PatternException
    {
    Predictor pred = null;
    String name = node.getAttribute( "name" );

    if( "NumericPredictor".equals( node.getNodeName() ) )
      {
      //<NumericPredictor name="petal_width" exponent="1" coefficient="-0.302988044352268"/>
      String exponent_text = node.getAttribute( "exponent" );
      Double exponent = new Double( 1.0 );

      if( exponent_text != null )
        exponent = Double.valueOf( exponent_text );

      Double coefficient = Double.valueOf( node.getAttribute( "coefficient" ) );
      pred = new NumericPredictor( name, coefficient, exponent );
      }
    else if( "CategoricalPredictor".equals( node.getNodeName() ) )
      {
      //<CategoricalPredictor name="species" value="setosa" coefficient="0"/>
      CategoricalDataField df = (CategoricalDataField) schema.get( name );
      Integer value = df.categories.indexOf( node.getAttribute( "value" ) );
      Double coefficient = Double.valueOf( node.getAttribute( "coefficient" ) );
      pred = new CategoricalPredictor( name, coefficient, value );
      }
    else
      {
      String message = String.format( "unsupported Predictor type %s", node.getNodeName() );
      LOG.error( message );
      throw new PatternException( message );
      }

    return pred;
    }
  }
