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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import cascading.pattern.model.generalregression.expression.ExpressionEvaluator;
import cascading.pattern.model.generalregression.expression.ParameterExpression;
import cascading.tuple.Fields;

/**
 * Class RegressionTable simply holds a set of {@link Parameter} instances.
 * <p/>
 * If used for classification or categorization with CategoricalRegressionFunction, the
 * table must have a {@code targetCategory} value.
 */
public class RegressionTable implements Serializable
  {
  private String targetCategory;

  Map<String, Parameter> parameters = new LinkedHashMap<String, Parameter>();

  public RegressionTable()
    {
    }

  public RegressionTable( String targetCategory )
    {
    this.targetCategory = targetCategory;
    }

  public String getTargetCategory()
    {
    return targetCategory;
    }

  public void setTargetCategory( String targetCategory )
    {
    this.targetCategory = targetCategory;
    }

  public void addParameter( Parameter parameter )
    {
    if( parameters.containsKey( parameter.getName() ) )
      throw new IllegalArgumentException( "may not have duplicate parameter names, got: " + parameter.getName() );

    parameters.put( parameter.getName(), parameter );
    }

  public Parameter getParameter( String name )
    {
    return parameters.get( name );
    }

  public boolean isNoOp()
    {
    for( Parameter parameter : parameters.values() )
      {
      if( !parameter.isNoOp() )
        return false;
      }

    return true;
    }

  ExpressionEvaluator bind( Fields argumentFields )
    {
    if( isNoOp() )
      return new ExpressionEvaluator( targetCategory );

    ParameterExpression[] expressions = new ParameterExpression[ parameters.size() ];

    int count = 0;

    for( Parameter parameter : parameters.values() )
      expressions[ count++ ] = parameter.createExpression( argumentFields );

    return new ExpressionEvaluator( targetCategory, expressions );
    }

  public Set<String> getParameterNames()
    {
    return parameters.keySet();
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "GeneralRegressionTable{" );
    sb.append( "targetCategory='" ).append( targetCategory ).append( '\'' );
    sb.append( ", parameters=" ).append( parameters );
    sb.append( '}' );
    return sb.toString();
    }
  }
