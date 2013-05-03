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

import cascading.pattern.model.clustering.compare.CompareFunction;
import cascading.pattern.model.clustering.measure.ComparisonMeasure;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;

/**
 *
 */
public class ClusterEvaluator
  {
  private final Cluster cluster;
  private final ComparisonMeasure comparisonMeasure;
  private final CompareFunction[] compareFunctions;

  public ClusterEvaluator( Fields argumentFields, Cluster cluster, ComparisonMeasure comparisonMeasure, CompareFunction compareFunction )
    {
    this.cluster = cluster;
    this.comparisonMeasure = comparisonMeasure;

    this.compareFunctions = createCompareFunctions( argumentFields, compareFunction );
    }

  private CompareFunction[] createCompareFunctions( Fields fields, CompareFunction defaultFunction, CompareFunction... functions )
    {
    CompareFunction[] results = new CompareFunction[ fields.size() ];

    Arrays.fill( results, defaultFunction );

    if( functions.length == 0 )
      return results;

    if( functions.length != fields.size() )
      throw new IllegalStateException( "fields and number of functions are not equal" );

    for( int i = 0; i < functions.length; i++ )
      {
      if( functions[ i ] != null )
        results[ i ] = functions[ i ];
      }

    return results;
    }

  double evaluate( TupleEntry tupleEntry )
    {
    return comparisonMeasure.calculate( compareFunctions, tupleEntry.getTuple(), cluster.getPoints() );
    }

  public String getTargetCategory()
    {
    return cluster.getTargetCategory();
    }
  }
