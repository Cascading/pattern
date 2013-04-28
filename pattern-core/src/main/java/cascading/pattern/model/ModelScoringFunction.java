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

package cascading.pattern.model;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.OperationCall;
import cascading.tuple.Tuple;


public abstract class ModelScoringFunction<S extends Spec, P> extends BaseOperation<ModelScoringFunction.Context<P>> implements Function<ModelScoringFunction.Context<P>>
  {
  protected S spec;

  /** Class Context is used to hold intermediate values. */
  protected static class Context<Payload>
    {
    public Tuple tuple = Tuple.size( 1 );
    public Payload payload;

    public Tuple result( Object label )
      {
      tuple.set( 0, label );

      return tuple;
      }
    }

  protected ModelScoringFunction( S spec )
    {
    super( spec.getModelSchema().getInputFields().size(), spec.getModelSchema().getDeclaredFields() );
    this.spec = spec;
    }

  public S getSpec()
    {
    return spec;
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<ModelScoringFunction.Context<P>> operationCall )
    {
    operationCall.setContext( new ModelScoringFunction.Context() );
    }
  }
