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

package cascading.pattern.model.clustering.measure;

import java.io.Serializable;

import cascading.pattern.model.clustering.compare.CompareFunction;
import cascading.tuple.Tuple;

/**
 *
 */
public abstract class ComparisonMeasure implements Serializable
  {
  /**
   * Calculate the distance from this cluster for the given tuple.
   *
   * @param compareFunctions
   * @param values           array of tuple values
   * @return double
   */
  public abstract double calculate( CompareFunction[] compareFunctions, Tuple values, double[] points );
  }
