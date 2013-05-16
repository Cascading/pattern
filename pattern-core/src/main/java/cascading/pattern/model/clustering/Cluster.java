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

import java.io.Serializable;
import java.util.List;

import com.google.common.primitives.Doubles;

/**
 * Class Cluster represents a point in space denoted by the given collection of {@code points} which
 * in turn represents a particular {@code targetCategory}.
 */
public class Cluster implements Serializable
  {
  protected int ordinal; // set when added to spec
  protected String targetCategory;
  protected double[] points;

  public Cluster( String targetCategory, double... points )
    {
    this( targetCategory );
    this.points = new double[ points.length ];

    System.arraycopy( points, 0, this.points, 0, points.length );
    }

  public Cluster( String targetCategory, List<Double> points )
    {
    this( targetCategory );
    this.points = Doubles.toArray( points );
    }

  private Cluster( String targetCategory )
    {
    this.targetCategory = targetCategory;
    }

  protected void setOrdinal( int ordinal )
    {
    this.ordinal = ordinal;
    }

  public String getTargetCategory()
    {
    if( targetCategory == null )
      return Integer.toString( ordinal );

    return targetCategory;
    }

  public double[] getPoints()
    {
    double[] dest = new double[ points.length ];

    System.arraycopy( points, 0, dest, 0, points.length );

    return dest;
    }

  public int getPointsSize()
    {
    return points.length;
    }
  }
