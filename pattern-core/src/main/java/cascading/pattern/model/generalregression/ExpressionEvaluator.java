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

import cascading.tuple.TupleEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ExpressionEvaluator
  {
  private static final Logger LOG = LoggerFactory.getLogger( ExpressionEvaluator.class );

  private final String name;
  private final ParameterExpression[] expressions;

  public ExpressionEvaluator( ParameterExpression[] expressions )
    {
    this.name = createName( expressions );
    this.expressions = expressions;
    }

  private String createName( ParameterExpression[] expressions )
    {
    String name = expressions[ 0 ].getName();

    for( int i = 1; i < expressions.length; i++ )
      name += "." + expressions[ i ].getName();

    return name;
    }

  public double calculate( TupleEntry tupleEntry )
    {
    double result = 0.0d;

    for( ParameterExpression expression : expressions )
      {
      if( expression.applies( tupleEntry ) )
        result += expression.calculate( tupleEntry );
      }

    LOG.debug( "expression: {}, result: {}", name, result );

    return result;
    }
  }
