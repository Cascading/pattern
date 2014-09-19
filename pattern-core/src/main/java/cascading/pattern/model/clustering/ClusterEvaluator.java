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

package cascading.pattern.model.clustering;

import java.util.Arrays;
import java.util.Map;

import cascading.pattern.model.clustering.compare.CompareFunction;
import cascading.pattern.model.clustering.measure.ComparisonMeasure;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;

/**
 *
 */
class ClusterEvaluator
  {
  private final Cluster cluster;
  private final ComparisonMeasure comparisonMeasure;
  private final CompareFunction[] compareFunctions;
  private final double[] points;

  public ClusterEvaluator( Fields argumentFields, Cluster cluster, ComparisonMeasure comparisonMeasure, CompareFunction defaultCompareFunction, Map<String, CompareFunction> compareFunctions  )
    {
    this.cluster = cluster;
    this.comparisonMeasure = comparisonMeasure;
    this.compareFunctions = createCompareFunctions( argumentFields, defaultCompareFunction, compareFunctions );
    this.points = cluster.getPoints();
    }

  private CompareFunction[] createCompareFunctions( Fields fields, CompareFunction defaultFunction, Map<String, CompareFunction> compareFunctions )
    {
    CompareFunction[] results = new CompareFunction[ fields.size() ];

    Arrays.fill( results, defaultFunction );

    for( int i = 0; i < fields.size(); i++ )
      {
      CompareFunction function = compareFunctions.get( fields.get( i ) );
      if (function != null)
        {
        results[i] = function;
        }
      }

    return results;
    }

  double evaluate( TupleEntry tupleEntry )
    {
    return comparisonMeasure.calculate( compareFunctions, tupleEntry.getTuple(), points );
    }

  public String getTargetCategory()
    {
    return cluster.getTargetCategory();
    }
  }
