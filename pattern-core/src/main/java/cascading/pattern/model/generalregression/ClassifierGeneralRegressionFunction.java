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

import cascading.flow.FlowProcess;
import cascading.operation.FunctionCall;
import cascading.pattern.datafield.CategoricalDataField;
import cascading.pattern.datafield.DataField;
import cascading.pattern.model.ModelSchema;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ClassifierGeneralRegressionFunction extends GeneralRegressionFunction
  {
  private static final Logger LOG = LoggerFactory.getLogger( ClassifierGeneralRegressionFunction.class );

  public ClassifierGeneralRegressionFunction( GeneralRegressionSpec regressionSpec )
    {
    super( regressionSpec );

    if( regressionSpec.getNormalization() == null )
      throw new IllegalArgumentException( "normalization may not be null" );

    ModelSchema modelSchema = regressionSpec.getModelSchema();

    DataField predictedField = modelSchema.getPredictedField( modelSchema.getPredictedFieldNames().get( 0 ) );

    if( !( predictedField instanceof CategoricalDataField ) )
      throw new IllegalArgumentException( "predicted field must be categorical" );

    if( ( (CategoricalDataField) predictedField ).getCategories().size() != regressionSpec.getGeneralRegressionTables().size() )
      throw new IllegalArgumentException( "predicted field categories must be same size as the number of regression tables" );
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context<ExpressionEvaluator[]>> functionCall )
    {
    TupleEntry arguments = functionCall.getArguments();
    ExpressionEvaluator[] expressions = functionCall.getContext().payload;

    double[] results = new double[ expressions.length ];

    for( int i = 0; i < expressions.length; i++ )
      results[ i ] = expressions[ i ].calculate( arguments );

    if( LOG.isDebugEnabled() )
      LOG.debug( "raw regression: {}", results );

    for( int i = 0; i < expressions.length; i++ )
      results[ i ] = getSpec().getLinkFunction().calculate( results[ i ] );

    if( LOG.isDebugEnabled() )
      LOG.debug( "link regression: {}", results );

    results = getSpec().getNormalization().normalize( results );

    if( LOG.isDebugEnabled() )
      LOG.debug( "probabilities: {}", results );

    double max = Doubles.max( results );
    int index = Doubles.indexOf( results, max );

    String category = expressions[ index ].getTargetCategory();

    LOG.debug( "category: {}", category );

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
