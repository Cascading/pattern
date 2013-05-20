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

import cascading.pattern.model.clustering.compare.AbsoluteDifferenceCompareFunction;
import org.dmg.pmml.ClusteringModel;
import org.dmg.pmml.CompareFunctionType;

/**
 *
 */
class ClusteringUtil
  {
  static AbsoluteDifferenceCompareFunction setComparisonFunction( ClusteringModel model )
    {
    CompareFunctionType compareFunction = model.getComparisonMeasure().getCompareFunction();

    switch( compareFunction )
      {
      case ABS_DIFF:
        return new AbsoluteDifferenceCompareFunction();
      case GAUSS_SIM:
        break;
      case DELTA:
        break;
      case EQUAL:
        break;
      case TABLE:
        break;
      }

    throw new UnsupportedOperationException( "unknown comparison function type: " + compareFunction );
    }
  }
