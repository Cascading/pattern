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

package cascading.pattern.model.generalregression.expression;

import cascading.pattern.model.generalregression.Parameter;
import cascading.pattern.model.generalregression.predictor.CovariantPredictor;
import cascading.pattern.model.generalregression.predictor.FactorPredictor;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ParameterExpression
  {
  private static final Logger LOG = LoggerFactory.getLogger( ParameterExpression.class );

  private final String name;
  private final double beta;

  FactorInvoker[] factorInvokers;
  CovariantInvoker[] covariantInvokers;

  private class FactorInvoker
    {
    int index;
    FactorPredictor predictor;

    private FactorInvoker( int index, FactorPredictor predictor )
      {
      this.index = index;
      this.predictor = predictor;
      }

    public boolean applies( TupleEntry tupleEntry )
      {
      return predictor.matches( tupleEntry.getString( index ) );
      }
    }

  private class CovariantInvoker
    {
    int index;
    CovariantPredictor predictor;

    private CovariantInvoker( int index, CovariantPredictor predictor )
      {
      this.index = index;
      this.predictor = predictor;
      }

    public double calculate( TupleEntry tupleEntry )
      {
      return predictor.calculate( tupleEntry.getDouble( index ) );
      }
    }

  public ParameterExpression( Fields argumentsFields, Parameter parameter )
    {
    this.name = parameter.getName();
    this.beta = parameter.getBeta();

    factorInvokers = new FactorInvoker[ parameter.getFactors().size() ];

    for( int i = 0; i < parameter.getFactors().size(); i++ )
      {
      FactorPredictor predictor = parameter.getFactors().get( i );
      int pos = argumentsFields.getPos( predictor.getFieldName() );

      factorInvokers[ i ] = new FactorInvoker( pos, predictor );
      }

    covariantInvokers = new CovariantInvoker[ parameter.getCovariants().size() ];

    for( int i = 0; i < parameter.getCovariants().size(); i++ )
      {
      CovariantPredictor predictor = parameter.getCovariants().get( i );
      int pos = argumentsFields.getPos( predictor.getFieldName() );

      covariantInvokers[ i ] = new CovariantInvoker( pos, predictor );
      }
    }

  public String getName()
    {
    return name;
    }

  public boolean applies( TupleEntry tupleEntry )
    {
    for( FactorInvoker invoker : factorInvokers )
      {
      if( !invoker.applies( tupleEntry ) )
        return false;
      }

    return true;
    }

  public double calculate( TupleEntry tupleEntry )
    {
    double result = beta;

    for( CovariantInvoker invoker : covariantInvokers )
      result *= invoker.calculate( tupleEntry );

    LOG.debug( "parameter: {}, result: {}", name, result );

    return result;
    }
  }
