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

import cascading.flow.FlowProcess;
import cascading.operation.FunctionCall;
import cascading.pattern.model.ClassifierFunction;

/**
 *
 */
public class ClusteringFunction extends ClassifierFunction<ClusteringSpec, Void>
  {
  public ClusteringFunction( ClusteringSpec clusteringParam )
    {
    super( clusteringParam );

    if( getFieldDeclaration().size() != 1 )
      throw new IllegalArgumentException( "may only declare one field, was " + getFieldDeclaration().print() );
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context<Void>> functionCall )
    {
    double bestDist = Double.MAX_VALUE;
    String bestLabel = null;

    for( Cluster cluster : getSpec().getClusters() )
      {
      double distance = ( (DistanceCluster) cluster ).calcDistance( functionCall.getArguments().getTuple() );

      if( distance < bestDist )
        {
        bestDist = distance;
        bestLabel = cluster.getLabel();
        }
      }

    functionCall.getOutputCollector().add( functionCall.getContext().result( bestLabel ) );
    }
  }
