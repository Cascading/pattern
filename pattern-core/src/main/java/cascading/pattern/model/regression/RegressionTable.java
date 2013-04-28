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

package cascading.pattern.model.regression;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cascading.pattern.model.regression.predictor.Predictor;
import cascading.tuple.Fields;

/**
 *
 */
public class RegressionTable implements Serializable
  {
  String targetCategory;
  double intercept = 0.0;
  Map<String, Predictor> predictors = new HashMap<String, Predictor>();

  public RegressionTable()
    {
    }

  public RegressionTable( double intercept )
    {
    this.intercept = intercept;
    }

  public RegressionTable( String targetCategory, double intercept )
    {
    this.targetCategory = targetCategory;
    this.intercept = intercept;
    }

  public RegressionTable( String targetCategory, double intercept, List<Predictor> predictors )
    {
    this.targetCategory = targetCategory;
    this.intercept = intercept;
    setPredictors( predictors );
    }

  public RegressionTable( double intercept, List<Predictor> predictors )
    {
    this.intercept = intercept;
    setPredictors( predictors );
    }

  public void setIntercept( double intercept )
    {
    this.intercept = intercept;
    }

  public void addPredictor( Predictor predictor )
    {
    this.predictors.put( predictor.getFieldName(), predictor );
    }

  public Map<String, Predictor> getPredictors()
    {
    return Collections.unmodifiableMap( predictors );
    }

  public void setPredictors( List<Predictor> predictors )
    {
    this.predictors.clear();

    for( Predictor predictor : predictors )
      addPredictor( predictor );
    }

  public ExpressionEvaluator bind( Fields argumentFields )
    {
    Predictor[] orderedPredictors;
    orderedPredictors = new Predictor[ predictors.size() ];

    if( predictors.size() != 0 )
      {
      int i = 0;

      for( Comparable comparable : argumentFields )
        orderedPredictors[ i++ ] = predictors.get( comparable.toString() );
      }

    return new ExpressionEvaluator( targetCategory, intercept, orderedPredictors );
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "RegressionTable{" );
    sb.append( "intercept=" ).append( intercept );
    sb.append( ", predictors=" ).append( predictors );
    sb.append( '}' );
    return sb.toString();
    }
  }
