/*
 * Copyright (c) 2007-2015 Concurrent, Inc. All Rights Reserved.
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
import cascading.operation.OperationCall;
import cascading.pattern.model.ModelScoringFunction;
import cascading.tuple.Tuple;
import com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class ClusteringFunction applies a given {@link ClusteringSpec} model to a stream of data.
 * <p/>
 * Use the ClusteringSpec to define the incoming and result values.
 * <p/>
 * If {@link cascading.pattern.model.ModelSchema#isIncludePredictedCategories()} is {@code true} then
 * a field named after every declared category will be emitted with the result of each {@link Cluster}.
 */
public class ClusteringFunction extends ModelScoringFunction<ClusteringSpec, ClusteringFunction.EvaluatorContext>
  {
  private static final Logger LOG = LoggerFactory.getLogger( ClusteringFunction.class );

  protected static class EvaluatorContext
    {
    public ClusterEvaluator[] evaluators;
    public double[] results;
    }

  public ClusteringFunction( ClusteringSpec clusteringParam )
    {
    super( clusteringParam );
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<Context<EvaluatorContext>> operationCall )
    {
    super.prepare( flowProcess, operationCall );

    operationCall.getContext().payload = new EvaluatorContext();

    operationCall.getContext().payload.evaluators = getSpec().getClusterEvaluator( operationCall.getArgumentFields() );
    operationCall.getContext().payload.results = new double[ getSpec().getClusters().size() ];
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context<EvaluatorContext>> functionCall )
    {
    ClusterEvaluator[] evaluators = functionCall.getContext().payload.evaluators;
    double[] results = functionCall.getContext().payload.results;

    for( int i = 0; i < evaluators.length; i++ )
      results[ i ] = evaluators[ i ].evaluate( functionCall.getArguments() );

    LOG.debug( "results: {}", results );

    // calc min distance
    double min = Doubles.min( results );
    int index = Doubles.indexOf( results, min );

    String category = evaluators[ index ].getTargetCategory();

    LOG.debug( "category: {}", category );

    // emit distance, and intermediate cluster category scores
    if( !getSpec().getModelSchema().isIncludePredictedCategories() )
      {
      functionCall.getOutputCollector().add( functionCall.getContext().result( category ) );
      return;
      }

    Tuple result = functionCall.getContext().tuple;

    result.set( 0, category );

    for( int i = 0; i < results.length; i++ )
      result.set( i + 1, results[ i ] );

    functionCall.getOutputCollector().add( result );
    }
  }
