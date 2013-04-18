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

package cascading.pattern.model.regression;

import java.util.ArrayList;
import java.util.List;

import cascading.pattern.model.MiningSchemaParam;
import cascading.pattern.model.Param;
import cascading.pattern.model.regression.predictor.Predictor;


public class RegressionParam extends Param
  {
  public Double intercept = 0.0;
  public List<Predictor> predictors = new ArrayList<Predictor>();

  public RegressionParam( MiningSchemaParam schemaParam, double intercept, List<Predictor> predictors )
    {
    super( schemaParam );
    this.intercept = intercept;
    this.predictors = predictors;
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "RegressionParam{" );
    sb.append( "intercept=" ).append( intercept );
    sb.append( ", predictors=" ).append( predictors );
    sb.append( '}' );
    return sb.toString();
    }
  }
