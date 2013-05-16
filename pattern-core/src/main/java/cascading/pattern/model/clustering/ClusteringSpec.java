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

import cascading.pattern.datafield.CategoricalDataField;
import cascading.pattern.datafield.DataField;
import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.Spec;
import cascading.pattern.model.clustering.compare.CompareFunction;
import cascading.pattern.model.clustering.measure.ComparisonMeasure;
import cascading.tuple.Fields;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;

/**
 * Class ClusteringSpec is used to define a cluster model for scoring.
 */
public class ClusteringSpec extends Spec
  {
  private List<Cluster> clusters = new ArrayList<Cluster>();
  private CompareFunction defaultCompareFunction;
  private ComparisonMeasure comparisonMeasure;

  public ClusteringSpec()
    {
    }

  public ClusteringSpec( ModelSchema schemaParam )
    {
    super( schemaParam );
    }

  public ClusteringSpec( ModelSchema schemaParam, List<Cluster> clusters )
    {
    super( schemaParam );
    setClusters( clusters );
    }

  public void setDefaultCompareFunction( CompareFunction defaultCompareFunction )
    {
    this.defaultCompareFunction = defaultCompareFunction;
    }

  public CompareFunction getDefaultCompareFunction()
    {
    return defaultCompareFunction;
    }

  public void setComparisonMeasure( ComparisonMeasure comparisonMeasure )
    {
    this.comparisonMeasure = comparisonMeasure;
    }

  public ComparisonMeasure getComparisonMeasure()
    {
    return comparisonMeasure;
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
    if( getModelSchema().getExpectedFieldNames().size() != cluster.getPointsSize() )
      throw new IllegalArgumentException( "given points must be same size as active fields" );

    cluster.setOrdinal( getClusters().size() + 1 );

    this.clusters.add( cluster );
    }

  public ClusterEvaluator[] getClusterEvaluator( Fields argumentFields )
    {
    List<Cluster> sorted = new ArrayList<Cluster>( clusters );

    final DataField predictedField = getModelSchema().getPredictedField( getModelSchema().getPredictedFieldNames().get( 0 ) );

    // order tables in category order as this is the declared field name order
    if( predictedField instanceof CategoricalDataField )
      {
      Ordering<Cluster> ordering = Ordering.natural().onResultOf( new Function<Cluster, Comparable>()
      {
      @Override
      public Comparable apply( Cluster cluster )
        {
        return ( (CategoricalDataField) predictedField ).getCategories().indexOf( cluster.getTargetCategory() );
        }
      } );

      Collections.sort( sorted, ordering );
      }

    ClusterEvaluator[] clusterEvaluators = new ClusterEvaluator[ sorted.size() ];

    for( int i = 0; i < sorted.size(); i++ )
      clusterEvaluators[ i ] = new ClusterEvaluator( argumentFields, sorted.get( i ), getComparisonMeasure(), getDefaultCompareFunction() );

    return clusterEvaluators;
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
