/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
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

package cascading.pattern.model.regression.predictor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CategoricalPredictor extends Predictor
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( CategoricalPredictor.class );

  public int index;

  /**
   * @param name        name of the DataField used by this term
   * @param index       value for the category
   * @param coefficient coefficient for the term
   */
  public CategoricalPredictor( String name, Integer index, Double coefficient )
    {
    this.name = name;
    this.index = index;
    this.coefficient = coefficient;
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
    int categoryIndex = (Integer) param_map.get( name );

    if( index == categoryIndex )
      result = coefficient;

    if( LOG.isDebugEnabled() )
      LOG.debug( String.format( "calc: %s, %d, %d, %e", name, index, categoryIndex, result ) );

    return result;
    }

  /** @return String */
  @Override
  public String toString()
    {
    return String.format( "CategoricalPredictor: %s, %d, %e", name, index, coefficient );
    }
  }
