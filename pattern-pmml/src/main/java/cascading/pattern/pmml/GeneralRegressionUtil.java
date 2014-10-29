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

package cascading.pattern.pmml;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cascading.pattern.model.generalregression.RegressionTable;
import cascading.pattern.model.generalregression.predictor.CovariantPredictor;
import cascading.pattern.model.generalregression.predictor.FactorPredictor;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dmg.pmml.GeneralRegressionModel;
import org.dmg.pmml.Parameter;
import org.dmg.pmml.Predictor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
class GeneralRegressionUtil
  {
  private static final Logger LOG = LoggerFactory.getLogger( GeneralRegressionUtil.class );

  public static RegressionTable createPPMatrix( GeneralRegressionModel model, Set<String> parameterList, Set<String> factorsList, Set<String> covariateList )
    {
    RegressionTable regressionTable = new RegressionTable();

    for( org.dmg.pmml.PCell modelPCell : model.getParamMatrix().getPCells() )
      {
      String parameterName = modelPCell.getParameterName();
      double beta = modelPCell.getBeta();
      Integer df = modelPCell.getDf();

      regressionTable.addParameter( new cascading.pattern.model.generalregression.Parameter( parameterName, beta, df.intValue() ) );
      }

    for( org.dmg.pmml.PPCell modelPPCell : model.getPPMatrix().getPPCells() )
      {
      String parameterName = modelPPCell.getParameterName();
      String predictorName = modelPPCell.getPredictorName().getValue();
      String value = modelPPCell.getValue();

      cascading.pattern.model.generalregression.predictor.Predictor predictor;

      if( factorsList.contains( predictorName ) )
        predictor = new FactorPredictor( predictorName, value );
      else if( covariateList.contains( predictorName ) )
        predictor = new CovariantPredictor( predictorName, Long.parseLong( value ) );
      else
        throw new IllegalStateException( "unknown predictor name: " + predictorName );

      regressionTable.getParameter( parameterName ).addPredictor( predictor );
      }

    Set<String> parameterNames = regressionTable.getParameterNames();

    if( !parameterNames.containsAll( parameterList ) )
      LOG.warn( "different set of parameters: {}", Sets.difference( parameterNames, parameterList ) );

    return regressionTable;
    }

  public static Set<String> createFactors( GeneralRegressionModel model )
    {
    List<String> list = Lists.transform( model.getFactorList().getPredictors(), new Function<Predictor, String>()
    {
    @Override
    public String apply( Predictor input )
      {
      return input.getName().getValue();
      }
    } );

    return new LinkedHashSet<String>( list );
    }

  public static Set<String> createCovariates( GeneralRegressionModel model )
    {
    List<String> list = Lists.transform( model.getCovariateList().getPredictors(), new Function<Predictor, String>()
    {
    @Override
    public String apply( Predictor input )
      {
      return input.getName().getValue();
      }
    } );

    return new LinkedHashSet<String>( list );
    }

  public static Set<String> createParameters( GeneralRegressionModel model )
    {
    List<String> list = Lists.transform( model.getParameterList().getParameters(), new Function<Parameter, String>()
    {
    @Override
    public String apply( Parameter input )
      {
      return input.getName();
      }
    } );

    return new LinkedHashSet<String>( list );
    }
  }
