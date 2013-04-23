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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NumericPredictor extends Predictor<Double>
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( NumericPredictor.class );

  public double coefficient;
  public long exponent = 1;

  public NumericPredictor( String name, double coefficient )
    {
    super( name );
    this.coefficient = coefficient;
    }

  public NumericPredictor( String name, double coefficient, long exponent )
    {
    super( name );
    this.coefficient = coefficient;
    this.exponent = exponent;
    }

  @Override
  public double calcTerm( Double value )
    {
    double result = Math.pow( value, exponent ) * coefficient;

    LOG.debug( String.format( "calc: %s, %e, %d, %e, %e", name, value, exponent, coefficient, result ) );

    return result;
    }

  /** @return String */
  @Override
  public String toString()
    {
    return String.format( "NumericPredictor: %s, %d, %e", name, exponent, coefficient );
    }
  }
