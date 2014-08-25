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
import cascading.operation.OperationCall;
import cascading.pattern.model.ModelScoringFunction;
import cascading.pattern.model.generalregression.expression.ExpressionEvaluator;
import cascading.tuple.Fields;

/**
 *
 */
abstract class BaseRegressionFunction extends ModelScoringFunction<GeneralRegressionSpec, BaseRegressionFunction.ExpressionContext>
  {
  protected static class ExpressionContext
    {
    public ExpressionEvaluator[] expressions;
    public double[] results;
    }

  public BaseRegressionFunction( GeneralRegressionSpec spec )
    {
    super( spec );
    }

  @Override
  public void prepare( @SuppressWarnings( "rawtypes" ) FlowProcess flowProcess, OperationCall<Context<ExpressionContext>> operationCall )
    {
    super.prepare( flowProcess, operationCall );

    Fields argumentFields = operationCall.getArgumentFields();

    operationCall.getContext().payload = new ExpressionContext();
    operationCall.getContext().payload.expressions = getSpec().getRegressionTableEvaluators( argumentFields );
    }
  }
