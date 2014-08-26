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

import java.util.HashMap;
import java.util.Map;

import cascading.pattern.model.clustering.compare.AbsoluteDifferenceCompareFunction;
import cascading.pattern.model.clustering.compare.CompareFunction;
import cascading.pattern.model.clustering.compare.DeltaCompareFunction;
import cascading.pattern.model.clustering.compare.EqualCompareFunction;
import cascading.pattern.model.clustering.compare.GaussianSimilarityCompareFunction;

import org.dmg.pmml.ClusteringField;
import org.dmg.pmml.ClusteringModel;
import org.dmg.pmml.CompareFunctionType;

/**
 *
 */
class ClusteringUtil
  {
  static CompareFunction getDefaultComparisonFunction( ClusteringModel model )
    {
    CompareFunctionType compareFunctionType = model.getComparisonMeasure().getCompareFunction();

    return getComparisonFunction( null, compareFunctionType, null );
    }

  public static Map<String, CompareFunction> getComparisonFunctions( ClusteringModel model, CompareFunction defaultCompareFunction )
    {
    Map<String, CompareFunction> result = new HashMap<String, CompareFunction>();
    
    for (ClusteringField field : model.getClusteringFields())
      {
      result.put( field.getField().getValue(), getComparisonFunction( defaultCompareFunction, field.getCompareFunction(), field.getSimilarityScale() ) );
      }
    
    return result;
    }

  private static CompareFunction getComparisonFunction( CompareFunction defaultCompareFunction, CompareFunctionType compareFunctionType, Double similarityScale )
    {
    if (compareFunctionType == null)
      {
      if (defaultCompareFunction instanceof GaussianSimilarityCompareFunction)
        // similarity scale is required for each field that uses GaussianSimilarity
        // even if GaussianSimilarity is the default (there is no such thing as a default
        // similarity scale)
        return new GaussianSimilarityCompareFunction( similarityScale );
      else
        return defaultCompareFunction;
      }

    switch( compareFunctionType )
      {
      case ABS_DIFF:
        return new AbsoluteDifferenceCompareFunction();
      case DELTA:
        return new DeltaCompareFunction();
      case EQUAL:
        return new EqualCompareFunction();
      case GAUSS_SIM:
        return new GaussianSimilarityCompareFunction( similarityScale );
      case TABLE:
        break;
      }

    throw new UnsupportedOperationException( "unsupported compare function: " + compareFunctionType );
    }
  }
