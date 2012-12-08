/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.predictor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pattern.datafield.DataField;


public class CategoricalPredictor extends Predictor
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( CategoricalPredictor.class );

  public Integer value;

  /**
   * @param name name of the DataField used by this term
   * @param coefficient coefficient for the term
   * @param value value for the category
   */
  public CategoricalPredictor( String name, Double coefficient, Integer value )
    {
    this.name = name;
    this.coefficient = coefficient;
    this.value = value;
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
    double result = 0.0;
    int cat = (Integer) param_map.get( name );

    if( value == cat )
      result = coefficient;

    LOG.debug( String.format( "calc: %s, %d, %d, %e", name, value, cat, result ) );

    return result;
    }

  /** @return String */
  @Override
  public String toString()
    {
    return String.format( "CategoricalPredictor: %s, %d, %e", name, value, coefficient );
    }
  }
