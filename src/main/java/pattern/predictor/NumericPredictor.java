/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.predictor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pattern.datafield.DataField;


public class NumericPredictor extends Predictor
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( NumericPredictor.class );

  public Double exponent;

  /**
   * @param name name of the DataField used by this term
   * @param coefficient coefficient for the term
   * @param exponent exponent for the term
   */
  public NumericPredictor( String name, Double coefficient, Double exponent )
    {
    this.name = name;
    this.coefficient = coefficient;
    this.exponent = exponent;
    }

  /**
   * Calculate the value for the term based on this Predictor.
   *
   * @param param_map tuples names/values
   * @return double
   */
  @Override
  public double calcTerm( Map<String, Object> param_map )
    {
    double value = (Double) param_map.get( name );
    double result =  Math.pow( value, exponent ) * coefficient;

    LOG.debug( String.format( "calc: %s, %e, %e, %e, %e", name, value, exponent, coefficient, result ) );

    return result;
    }

  /** @return String  */
  @Override
  public String toString()
    {
    return String.format( "NumericPredictor: %s, %e, %e", name, exponent, coefficient );
    }
  }
