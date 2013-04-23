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

package cascading.pattern.pmml.regression;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cascading.pattern.model.regression.predictor.Predictor;
import org.dmg.pmml.CategoricalPredictor;
import org.dmg.pmml.NumericPredictor;
import org.dmg.pmml.RegressionTable;

/**
 *
 */
public class RegressionUtil
  {
  public static List<Predictor> createPredictors( RegressionTable regressionTable )
    {
    List<Predictor> predictors = new ArrayList<Predictor>();

    Map<String, cascading.pattern.model.regression.predictor.CategoricalPredictor> categories = new HashMap<String, cascading.pattern.model.regression.predictor.CategoricalPredictor>();

    for( CategoricalPredictor predictor : regressionTable.getCategoricalPredictors() )
      {
      String name = predictor.getName().getValue();
      String value = predictor.getValue();
      double coefficient = predictor.getCoefficient();

      if( !categories.containsKey( name ) )
        categories.put( name, new cascading.pattern.model.regression.predictor.CategoricalPredictor( name ) );

      categories.get( name ).addCategory( value, coefficient );
      }

    predictors.addAll( categories.values() );

    for( NumericPredictor predictor : regressionTable.getNumericPredictors() )
      {
      long exponent = predictor.getExponent().longValue(); // maybe losing data here

      if( !predictor.getExponent().equals( BigInteger.valueOf( exponent ) ) )
        throw new UnsupportedOperationException( "BigInt values not supported" );

      double coefficient = predictor.getCoefficient();

      predictors.add( new cascading.pattern.model.regression.predictor.NumericPredictor( predictor.getName().getValue(), coefficient, exponent ) );
      }
    return predictors;
    }
  }
