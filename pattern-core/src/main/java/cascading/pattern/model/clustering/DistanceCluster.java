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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class DistanceCluster extends Cluster
  {
  protected List<Double> points = new ArrayList<Double>(); // todo: may Double[], nulls as missing values

  public DistanceCluster( String name, Double... points )
    {
    this( name, Arrays.asList( points ) );
    }

  public DistanceCluster( String name, List<Double> points )
    {
    super( name );
    this.points = points;
    }

  /**
   * Calculate the distance from this cluster for the given tuple.
   *
   * @param paramValues array of tuple values
   * @return double
   */
  public abstract double calcDistance( Double[] paramValues );

  /** @return String */
  @Override
  public String toString()
    {
    return String.format( "%s: %s %s", getClass().getSimpleName(), name, points.toString() );
    }
  }
