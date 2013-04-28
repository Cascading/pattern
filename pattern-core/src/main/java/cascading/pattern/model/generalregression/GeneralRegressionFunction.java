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
import cascading.operation.OperationCall;
import cascading.pattern.model.ClassifierFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class GeneralRegressionFunction extends ClassifierFunction<GeneralRegressionSpec, ExpressionEvaluator>
  {
  private static final Logger LOG = LoggerFactory.getLogger( GeneralRegressionFunction.class );

  public GeneralRegressionFunction( GeneralRegressionSpec param )
    {
    super( param );
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<Context<ExpressionEvaluator>> operationCall )
    {
    super.prepare( flowProcess, operationCall );

    ExpressionEvaluator expression = getSpec().getGeneralRegressionTable().bind( operationCall.getArgumentFields() );

    operationCall.getContext().payload = expression;
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context<ExpressionEvaluator>> functionCall )
    {
    ExpressionEvaluator evaluator = functionCall.getContext().payload;
    LinkFunction linkFunction = getSpec().linkFunction;

    double result = evaluator.calculate( functionCall.getArguments() );
    double linkResult = linkFunction.calculate( result );

    LOG.debug( "result: {}", linkResult );

    functionCall.getOutputCollector().add( functionCall.getContext().result( linkResult ) );
    }
  }
