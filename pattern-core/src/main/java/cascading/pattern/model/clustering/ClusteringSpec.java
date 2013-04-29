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
import java.util.Collections;
import java.util.List;

import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.Spec;

/**
 *
 */
public class ClusteringSpec extends Spec
  {
  private List<Cluster> clusters = new ArrayList<Cluster>();

  public ClusteringSpec( ModelSchema schemaParam, List<Cluster> clusters )
    {
    super( schemaParam );
    setClusters( clusters );
    }

  public ClusteringSpec( ModelSchema schemaParam )
    {
    super( schemaParam );
    }

  public ClusteringSpec()
    {
    }

  public List<Cluster> getClusters()
    {
    return Collections.unmodifiableList( clusters );
    }

  public void setClusters( List<Cluster> clusters )
    {
    this.clusters.clear();

    for( Cluster cluster : clusters )
      addCluster( cluster );
    }

  public void addCluster( Cluster cluster )
    {
    if( cluster instanceof DistanceCluster && getModelSchema().getExpectedFieldNames().size() != ( (DistanceCluster) cluster ).points.length )
      throw new IllegalArgumentException( "given points must be same size as active fields" );

    cluster.setOrdinal( getClusters().size() + 1 );

    this.clusters.add( cluster );
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "ClusteringSpec{" );
    sb.append( "clusters=" ).append( clusters );
    sb.append( '}' );
    return sb.toString();
    }
  }
