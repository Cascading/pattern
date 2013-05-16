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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cascading.pattern.model.generalregression.expression.ParameterExpression;
import cascading.pattern.model.generalregression.predictor.CovariantPredictor;
import cascading.pattern.model.generalregression.predictor.FactorPredictor;
import cascading.pattern.model.generalregression.predictor.Predictor;
import cascading.tuple.Fields;

/**
 *
 */
public class Parameter implements Serializable
  {
  String name;
  double beta;
  int df;

  ArrayList<CovariantPredictor> covariants = new ArrayList<CovariantPredictor>();
  ArrayList<FactorPredictor> factors = new ArrayList<FactorPredictor>();

  public Parameter( String name, double beta, int df )
    {
    this.name = name;
    this.beta = beta;
    this.df = df;
    }

  public Parameter( String name, double beta )
    {
    this.name = name;
    this.beta = beta;
    }

  public Parameter( String name, double beta, Predictor... predictors )
    {
    this( name, beta, Arrays.asList( predictors ) );
    }

  public Parameter( String name, double beta, List<Predictor> predictors )
    {
    this.name = name;
    this.beta = beta;

    addPredictors( predictors );
    }

  public String getName()
    {
    return name;
    }

  public double getBeta()
    {
    return beta;
    }

  public int getDegreesOfFreedom()
    {
    return df;
    }

  public ArrayList<CovariantPredictor> getCovariants()
    {
    return covariants;
    }

  public ArrayList<FactorPredictor> getFactors()
    {
    return factors;
    }

  public boolean isNoOp()
    {
    return beta == 0;
    }

  public void addPredictors( List<Predictor> predictors )
    {
    for( Predictor predictor : predictors )
      addPredictor( predictor );
    }

  public void addPredictor( Predictor predictor )
    {
    if( predictor instanceof CovariantPredictor )
      covariants.add( (CovariantPredictor) predictor );

    if( predictor instanceof FactorPredictor )
      factors.add( (FactorPredictor) predictor );
    }

  public ParameterExpression createExpression( Fields argumentsFields )
    {
    return new ParameterExpression( argumentsFields, this );
    }
  }
