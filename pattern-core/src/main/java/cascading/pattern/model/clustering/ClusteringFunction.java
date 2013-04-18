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

import java.util.Map;

import cascading.flow.FlowProcess;
import cascading.operation.FunctionCall;
import cascading.pattern.model.ClassifierFunction;

/**
 *
 */
public class ClusteringFunction extends ClassifierFunction<ClusteringParam>
  {
  public ClusteringFunction( ClusteringParam clusteringParam )
    {
    super( clusteringParam );
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context> functionCall )
    {
    Map<String, Object> paramMap = getParam().getSchemaParam().getParamMap( functionCall.getArguments().getTuple() );
    String[] paramNames = getParam().getSchemaParam().getParamNames();
    Double[] paramValues = new Double[ paramNames.length ];

    for( int i = 0; i < paramNames.length; i++ )
      paramValues[ i ] = (Double) paramMap.get( paramNames[ i ] );

    Exemplar bestExemplar = null;
    double bestDist = 0.0;

    for( Exemplar exemplar : getParam().exemplars )
      {
      double distance = exemplar.calcDistance( paramValues );

      if( ( bestExemplar == null ) || ( distance < bestDist ) )
        {
        bestExemplar = exemplar;
        bestDist = distance;
        }
      }

    functionCall.getOutputCollector().add( functionCall.getContext().result( bestExemplar.name ) );
    }
  }
