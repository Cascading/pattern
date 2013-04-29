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

import cascading.flow.FlowProcess;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.pattern.model.ModelScoringFunction;
import cascading.tuple.TupleEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RegressionFunction extends ModelScoringFunction<RegressionSpec, ExpressionEvaluator[]>
  {
  private static final Logger LOG = LoggerFactory.getLogger( RegressionFunction.class );

  public RegressionFunction( RegressionSpec regressionSpec )
    {
    super( regressionSpec );
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<Context<ExpressionEvaluator[]>> operationCall )
    {
    super.prepare( flowProcess, operationCall );

    operationCall.getContext().payload = getSpec().getRegressionTableEvaluators( operationCall.getArgumentFields() );
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context<ExpressionEvaluator[]>> functionCall )
    {
    TupleEntry arguments = functionCall.getArguments();

    double result = functionCall.getContext().payload[ 0 ].calculate( arguments );

    LOG.debug( "result: " + result );

    functionCall.getOutputCollector().add( functionCall.getContext().result( result ) );
    }
  }
