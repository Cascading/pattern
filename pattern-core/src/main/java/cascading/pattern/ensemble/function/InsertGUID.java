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

package cascading.pattern.ensemble.function;

import cascading.operation.expression.ExpressionFunction;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.pipe.SubAssembly;
import cascading.tuple.Fields;

/**
 * Class InsertGUID creates a globally unique ID by calling {@link java.util.UUID#randomUUID()}.
 * <p/>
 * This Function also returns {@code false} for {@link cascading.operation.Operation#isSafe()}, preventing
 * duplicate ids from being generated for the same record.
 */
public class InsertGUID extends SubAssembly
  {
  public InsertGUID( Pipe previous, Fields declaredFields )
    {
    super( previous );

    String expression = "java.util.UUID.randomUUID().toString()";

    ExpressionFunction expressionFunction = new ExpressionFunction( declaredFields, expression )
    {
    @Override
    public boolean isSafe()
      {
      return false;
      }
    };

    previous = new Each( previous, Fields.NONE, expressionFunction, Fields.ALL );

    setTails( previous );
    }
  }
