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

package cascading.pattern.pmml;

import cascading.pattern.model.generalregression.Parameter;
import cascading.pattern.model.generalregression.RegressionTable;
import cascading.pattern.model.generalregression.normalization.Normalization;
import cascading.pattern.model.generalregression.normalization.SoftMaxNormalization;
import cascading.pattern.model.generalregression.predictor.CovariantPredictor;
import cascading.pattern.model.generalregression.predictor.FactorPredictor;
import org.dmg.pmml.CategoricalPredictor;
import org.dmg.pmml.NumericPredictor;
import org.dmg.pmml.RegressionModel;

/**
 *
 */
class RegressionUtil
  {
  public static RegressionTable createTable( org.dmg.pmml.RegressionTable regressionTable )
    {
    RegressionTable generalRegressionTable = new RegressionTable();

    String targetCategory = regressionTable.getTargetCategory();

    if( targetCategory != null )
      generalRegressionTable.setTargetCategory( targetCategory );

    generalRegressionTable.addParameter( new Parameter( "intercept", regressionTable.getIntercept() ) );

    int count = 0;

    for( CategoricalPredictor predictor : regressionTable.getCategoricalPredictors() )
      {
      String name = predictor.getName().getValue();
      String value = predictor.getValue();
      double coefficient = predictor.getCoefficient();

      generalRegressionTable.addParameter( new Parameter( "f" + count++, coefficient, new FactorPredictor( name, value ) ) );
      }

    for( NumericPredictor predictor : regressionTable.getNumericPredictors() )
      {
      String name = predictor.getName().getValue();
      int exponent = predictor.getExponent();

      double coefficient = predictor.getCoefficient();

      generalRegressionTable.addParameter( new Parameter( "f" + count++, coefficient, new CovariantPredictor( name, exponent ) ) );
      }

    return generalRegressionTable;
    }

  static Normalization getNormalizationMethod( RegressionModel model )
    {
    switch( model.getNormalizationMethod() )
      {
      case NONE:
        return Normalization.NONE;
      case SIMPLEMAX:
        break;
      case SOFTMAX:
        return new SoftMaxNormalization();
      case LOGIT:
        break;
      case PROBIT:
        break;
      case CLOGLOG:
        break;
      case EXP:
        break;
      case LOGLOG:
        break;
      case CAUCHIT:
        break;
      }

    throw new UnsupportedOperationException( "unsupported normalization method: " + model.getNormalizationMethod() );
    }
  }
