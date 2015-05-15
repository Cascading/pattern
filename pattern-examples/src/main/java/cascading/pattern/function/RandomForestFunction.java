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

package cascading.pattern.function;

import java.util.Arrays;
import java.util.List;

import cascading.flow.FlowProcess;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.ModelScoringFunction;
import cascading.pattern.model.tree.decision.DecisionTree;
import cascading.pattern.model.tree.decision.FinalDecision;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Sets.newHashSet;

/** Sample code if, for example, an ensemble should be executed as a single Function. */
public class RandomForestFunction extends ModelScoringFunction<RandomForestSpec, RandomForestFunction.DecisionContext>
  {
  private static final Logger LOG = LoggerFactory.getLogger( RandomForestFunction.class );

  protected static class DecisionContext
    {
    public String[] categories;
    public DecisionTree[] trees;
    public int[] results;
    }

  public RandomForestFunction( RandomForestSpec randomForestSpec )
    {
    super( randomForestSpec );

    ModelSchema modelSchema = randomForestSpec.getModelSchema();

    String predictedFieldName = modelSchema.getPredictedFieldNames().get( 0 );

    List<String> predictedCategories = modelSchema.getPredictedCategories( predictedFieldName );

    if( modelSchema.isIncludePredictedCategories() && predictedCategories.isEmpty() )
      throw new IllegalArgumentException( "no predicted categories were set, but include predicted is true" );

    List<String> nodeCategories = randomForestSpec.getModelCategories();
    Sets.SetView<String> difference = Sets.difference( newHashSet( predictedCategories ), newHashSet( nodeCategories ) );

    if( !difference.isEmpty() && !predictedCategories.isEmpty() )
      throw new IllegalArgumentException( "forest declares differing categories than declared by the predicted field: " + difference );
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<Context<DecisionContext>> operationCall )
    {
    super.prepare( flowProcess, operationCall );

    Fields argumentFields = operationCall.getArgumentFields();
    String[] categories = spec.getCategoriesArray();
    DecisionTree[] decisionTrees = getSpec().getDecisionTrees( categories, argumentFields );

    operationCall.getContext().payload = new DecisionContext();
    operationCall.getContext().payload.categories = categories;
    operationCall.getContext().payload.trees = decisionTrees;
    operationCall.getContext().payload.results = new int[ categories.length ];
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context<DecisionContext>> functionCall )
    {
    TupleEntry arguments = functionCall.getArguments();

    String[] categories = functionCall.getContext().payload.categories;
    int[] results = functionCall.getContext().payload.results;

    Arrays.fill( results, 0 ); // clear before use

    DecisionTree[] decisionTrees = functionCall.getContext().payload.trees;

    for( int i = 0; i < decisionTrees.length; i++ )
      {
      FinalDecision finalDecision = decisionTrees[ i ].decide( arguments );

      if( LOG.isDebugEnabled() )
        LOG.debug( "segment: {}, returned category: {}", i, finalDecision.getScore() );

      results[ finalDecision.getIndex() ]++;
      }

    int max = Ints.max( results );
    int index = Ints.indexOf( results, max );

    String category = categories[ index ];

    LOG.debug( "winning score: {}, with votes: {}", categories, max );

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
