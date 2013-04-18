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

package cascading.pattern.pmml.generalregression;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cascading.pattern.model.generalregression.PCell;
import cascading.pattern.model.generalregression.PPCell;
import cascading.pattern.model.generalregression.PPMatrix;
import cascading.pattern.model.generalregression.ParamMatrix;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.dmg.pmml.GeneralRegressionModel;
import org.dmg.pmml.Parameter;
import org.dmg.pmml.Predictor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class GLMUtil
  {
  private static final Logger LOG = LoggerFactory.getLogger( GLMUtil.class );

  public static PPMatrix createPPMatrix( GeneralRegressionModel model )
    {
    PPMatrix ppMatrix = new PPMatrix();

    for( org.dmg.pmml.PPCell modelPPCell : model.getPPMatrix().getPPCells() )
      {
      String parameterName = modelPPCell.getParameterName();
      String predictorName = modelPPCell.getPredictorName().getValue();
      String value = modelPPCell.getValue();

      if( !ppMatrix.containsKey( parameterName ) )
        ppMatrix.put( parameterName, new ArrayList<PPCell>() );

      ppMatrix.get( parameterName ).add( new PPCell( parameterName, predictorName, value ) );

      LOG.debug( "PMML add DataField: {}", ppMatrix.get( parameterName ) );
      }

    return ppMatrix;
    }

  public static ParamMatrix createParamMatrix( GeneralRegressionModel model )
    {
    ParamMatrix paramMatrix = new ParamMatrix();

    for( org.dmg.pmml.PCell modelPCell : model.getParamMatrix().getPCells() )
      {
      String parameterName = modelPCell.getParameterName();
      double beta = modelPCell.getBeta();
      BigInteger df = modelPCell.getDf();

      if( !paramMatrix.containsKey( parameterName ) )
        paramMatrix.put( parameterName, new ArrayList<PCell>() );

      paramMatrix.get( parameterName ).add( new PCell( parameterName, beta, df ) );

      LOG.debug( "PMML add DataField: {}" + paramMatrix.get( parameterName ) );
      }

    return paramMatrix;
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

    return new HashSet<String>( list );
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

    return new HashSet<String>( list );
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

    return new HashSet<String>( list );
    }
  }
