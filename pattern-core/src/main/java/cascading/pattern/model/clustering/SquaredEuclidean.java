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

import java.util.List;

import cascading.tuple.Tuple;

/**
 *
 */
public class SquaredEuclidean extends DistanceCluster
  {
  public SquaredEuclidean( String name, Double... points )
    {
    super( name, points );
    }

  public SquaredEuclidean( String name, List<Double> points )
    {
    super( name, points );
    }

  /**
   * Calculate the distance from this cluster for the given tuple.
   *
   *
   * @param values array of tuple values
   * @return double
   */
  public double calcDistance( Tuple values )
    {
    double sumOfSquares = 0.0;

    for( int i = 0; i < points.length; i++ )
      sumOfSquares += Math.pow( (Double) values.getObject( i ) - points[ i ], 2.0 );

    return sumOfSquares;
    }
  }
