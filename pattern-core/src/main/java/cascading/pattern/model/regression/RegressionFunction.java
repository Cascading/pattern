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

import java.util.List;
import java.util.Map;

import cascading.flow.FlowProcess;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.pattern.model.ClassifierFunction;
import cascading.pattern.model.regression.predictor.Predictor;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RegressionFunction extends ClassifierFunction<RegressionSpec, RegressionFunction.Payload>
  {
  private static final Logger LOG = LoggerFactory.getLogger( RegressionFunction.class );

  protected static class Payload
    {
    public Predictor[][] predictors;
    }

  public RegressionFunction( RegressionSpec param )
    {
    super( param );
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<Context<Payload>> operationCall )
    {
    super.prepare( flowProcess, operationCall );

    operationCall.getContext().payload = new Payload();

    List<RegressionTable> tables = spec.getRegressionTables();

    Predictor[][] orderedPredictors = new Predictor[ tables.size() ][];

    for( int i = 0; i < tables.size(); i++ )
      {
      Map<String, Predictor> predictors = tables.get( i ).predictors;
      orderedPredictors[ i ] = new Predictor[ predictors.size() ];

      int j = 0;

      for( Comparable comparable : operationCall.getArgumentFields() )
        orderedPredictors[ i ][ j++ ] = predictors.get( comparable.toString() );
      }

    operationCall.getContext().payload.predictors = orderedPredictors;
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context<Payload>> functionCall )
    {
    TupleEntry arguments = functionCall.getArguments();
    Tuple values = arguments.getTuple();

    Predictor[][] predictors = functionCall.getContext().payload.predictors;

    double result = getSpec().getRegressionTables().get( 0 ).intercept;

    for( int i = 0; i < values.size(); i++ )
      result += predictors[ 0 ][ i ].calcTerm( values.getObject( i ) );

    LOG.debug( "result: " + result );

    functionCall.getOutputCollector().add( functionCall.getContext().result( result ) );
    }
  }
