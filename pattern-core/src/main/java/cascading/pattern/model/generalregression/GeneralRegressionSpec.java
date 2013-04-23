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

package cascading.pattern.model.generalregression;

import java.util.Set;

import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.Spec;


public class GeneralRegressionSpec extends Spec
  {
  PPMatrix ppMatrix;
  ParamMatrix paramMatrix;
  Set<String> covariates;
  Set<String> factors;
  Set<String> parameters;
  LinkFunction linkFunction;

  public GeneralRegressionSpec( ModelSchema schemaParam, PPMatrix ppMatrix, ParamMatrix paramMatrix, Set<String> parameters, Set<String> covariates, Set<String> factors, LinkFunction linkFunction )
    {
    super( schemaParam );
    this.ppMatrix = ppMatrix;
    this.paramMatrix = paramMatrix;
    this.parameters = parameters;
    this.covariates = covariates;
    this.factors = factors;
    this.linkFunction = linkFunction;
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "GeneralRegressionParam{" );
    sb.append( "ppMatrix=" ).append( ppMatrix );
    sb.append( ", paramMatrix=" ).append( paramMatrix );
    sb.append( ", covariates=" ).append( covariates );
    sb.append( ", factors=" ).append( factors );
    sb.append( ", parameters=" ).append( parameters );
    sb.append( ", linkFunction=" ).append( linkFunction );
    sb.append( '}' );
    return sb.toString();
    }
  }
