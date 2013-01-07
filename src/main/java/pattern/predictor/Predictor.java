/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.predictor;

import java.io.Serializable;
import java.util.Map;


public abstract class Predictor implements Serializable
  {
  public String name;
  public Double coefficient;

  /**
   * Calculate the value for the term based on this Predictor.
   *
   * @param param_map tuples names/values
   * @return double
   */
  public abstract double calcTerm( Map<String, Object> param_map );
  }
