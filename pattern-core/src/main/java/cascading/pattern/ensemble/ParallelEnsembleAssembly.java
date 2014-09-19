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

package cascading.pattern.ensemble;

import java.util.ArrayList;
import java.util.List;

import cascading.pattern.ensemble.function.InsertGUID;
import cascading.pattern.ensemble.selection.CategoricalSelectionBuffer;
import cascading.pattern.ensemble.selection.PredictionSelectionBuffer;
import cascading.pattern.ensemble.selection.SelectionBuffer;
import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.ModelScoringFunction;
import cascading.pattern.model.Spec;
import cascading.pattern.model.tree.TreeFunction;
import cascading.pattern.model.tree.TreeSpec;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.SubAssembly;
import cascading.pipe.assembly.Discard;
import cascading.tuple.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class ParallelEnsembleAssembly allows for the execution of multiple models in parallel where the result
 * is either a prediction or classification, depending on the predicted fields data type.
 * <p/>
 * If {@link cascading.pattern.ensemble.EnsembleSpec#isPredictedCategorical()} returns {@code true}, the
 * ensemble will return a category based on the current {@link cascading.pattern.ensemble.selection.CategoricalSelector}.
 * If {@link cascading.pattern.model.ModelSchema#isIncludePredictedCategories()} returns {@code true}, a field
 * will be created for each declared category, and the count of occurrences of each category will be returned.
 * <p/>
 * Otherwise a numeric prediction is returned based on the current
 * {@link cascading.pattern.ensemble.selection.PredictionSelector}. {@code isIncludePredictedCategories()} is ignored.
 * <p/>
 * If {@link cascading.pattern.ensemble.EnsembleSpec#isParallel()} returns {@code false}, this assembly will
 * throw an IllegalArgumentException. The chosen {@link cascading.pattern.ensemble.selection.SelectionStrategy} must
 * support parallel execution.
 * <p/>
 * If {@link cascading.pattern.model.ModelSchema#getKeyFields()} is {@link Fields#NONE}, a globally unique ID will
 * be created to that the results of each independent model can be grouped together so the SelectionStrategy can
 * be applied. This field will be discarded downstream.
 * <p/>
 * Currently ParallelEnsembleAssembly does not support associating models with predicates.
 */
public class ParallelEnsembleAssembly extends SubAssembly
  {
  private static final Logger LOG = LoggerFactory.getLogger( ParallelEnsembleAssembly.class );

  public ParallelEnsembleAssembly( Pipe pipe, EnsembleSpec<?> ensembleSpec )
    {
    super( pipe );

    if( !ensembleSpec.isParallel() )
      throw new IllegalArgumentException( "given selection strategy must support parallel models, got: " + ensembleSpec.getSelectionStrategy() );

    if( ensembleSpec.getModelSpecs().size() < 2 )
      throw new IllegalArgumentException( "ensembles must have more than 1 model" );

    ModelSchema modelSchema = ensembleSpec.getModelSchema();

    Fields predictedFields = modelSchema.getPredictedFields();
    Fields keyFields = modelSchema.getKeyFields();

    if( keyFields.isNone() )
      {
      keyFields = new Fields( "ensemble-primary-key", String.class );
      pipe = new InsertGUID( pipe, keyFields );
      }

    boolean isCategorical = ensembleSpec.isPredictedCategorical();

    // the parallel bits
    List<Pipe> pipes = new ArrayList<Pipe>();

    for( int i = 0; i < ensembleSpec.getModelSpecs().size(); i++ )
      {
      Spec spec = (Spec) ensembleSpec.getModelSpecs().get( i );

      if( spec instanceof TreeSpec )
        pipes.add( createScoringPipe( i, pipe, modelSchema, new TreeFunction( (TreeSpec) spec, isCategorical, false ) ) );
      }

    pipe = new GroupBy( "vote", pipes.toArray( new Pipe[ pipes.size() ] ), keyFields );

    SelectionBuffer<?> buffer;

    if( isCategorical )
      buffer = new CategoricalSelectionBuffer( ensembleSpec );
    else
      buffer = new PredictionSelectionBuffer( ensembleSpec );

    pipe = new Every( pipe, predictedFields, buffer, Fields.SWAP );

    if( modelSchema.getKeyFields().isNone() )
      pipe = new Discard( pipe, keyFields );

    setTails( pipe );
    }

  private Each createScoringPipe( int ordinal, Pipe tail, ModelSchema ensembleSchema, ModelScoringFunction<?, ?> function )
    {
    Fields inputFields = ensembleSchema.getInputFields();
    Fields declaredFields = ensembleSchema.getDeclaredFields();

    if( LOG.isDebugEnabled() )
      {
      LOG.debug( "creating: {}", function.getSpec() );
      LOG.debug( "input: {}, output: {}", inputFields, declaredFields );
      }

    tail = new Pipe( "model-" + ordinal, tail );

    return new Each( tail, inputFields, function, Fields.ALL );
    }
  }
