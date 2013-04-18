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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Exemplar implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( Exemplar.class );

  public String name;
  public List<Double> points = new ArrayList<Double>();

  public Exemplar( String name, List<Double> points )
    {
    this.name = name;
    this.points = points;
    }

  /**
   * Calculate the distance from this cluster for the given tuple.
   *
   * @param paramValues array of tuple values
   * @return double
   */
  public double calcDistance( Double[] paramValues )
    {
    double sumOfSquares = 0.0;

    for( int i = 0; i < paramValues.length; i++ )
      sumOfSquares += Math.pow( paramValues[ i ] - points.get( i ), 2.0 );

    return Math.sqrt( sumOfSquares );
    }

  /** @return String */
  @Override
  public String toString()
    {
    return String.format( "Exemplar: %s %s", name, points.toString() );
    }
  }
