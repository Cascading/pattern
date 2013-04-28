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

import cascading.pattern.model.regression.predictor.Predictor;
import cascading.tuple.TupleEntry;

/**
 *
 */
public class ExpressionEvaluator
  {
  private final String targetCategory;
  private final double intercept;
  private final Predictor[] orderedPredictors;

  public ExpressionEvaluator( String targetCategory, double intercept, Predictor[] orderedPredictors )
    {
    this.targetCategory = targetCategory;
    this.intercept = intercept;
    this.orderedPredictors = orderedPredictors;
    }

  public String getTargetCategory()
    {
    return targetCategory;
    }

  public double calculate( TupleEntry tupleEntry )
    {
    double result = intercept;

    for( int i = 0; i < orderedPredictors.length; i++ )
      result += orderedPredictors[ i ].calculate( tupleEntry.getObject( i ) );

    return result;
    }
  }
