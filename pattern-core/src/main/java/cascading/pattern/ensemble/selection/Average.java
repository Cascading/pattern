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

package cascading.pattern.ensemble.selection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Average extends PredictionSelector
  {
  private static final Logger LOG = LoggerFactory.getLogger( Average.class );

  @Override
  public boolean isParallel()
    {
    return true;
    }

  @Override
  double predict( double[] results )
    {
    double sum = 0;

    for( double result : results )
      sum += result;

    double avg = sum / results.length;

    LOG.debug( "prediction: {}", avg );

    return avg;
    }
  }
