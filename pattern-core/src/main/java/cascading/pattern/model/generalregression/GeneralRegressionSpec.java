/*
 * Copyright (c) 2007-2015 Concurrent, Inc. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cascading.pattern.datafield.CategoricalDataField;
import cascading.pattern.datafield.DataField;
import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.Spec;
import cascading.pattern.model.generalregression.expression.ExpressionEvaluator;
import cascading.pattern.model.generalregression.normalization.Normalization;
import cascading.tuple.Fields;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;


/**
 * Class GeneralRegressionSpec is used to define a general regression model for classification or prediction, and
 * can be used with {@link CategoricalRegressionFunction} and {@link PredictionRegressionFunction} respectively.
 * <p/>
 * General regression applies a {@link RegressionTable} to each Tuple. If only one table is specified, this spec
 * can be used with the PredictionRegressionFunction. If multiple tables are specified, each must have a unique
 * category and should be used with CategoricalRegressionFunction.
 * <p/>
 * By default, no link function or normalization method is used.
 */
public class GeneralRegressionSpec extends Spec
  {
  List<RegressionTable> regressionTables = new ArrayList<RegressionTable>();
  LinkFunction linkFunction = LinkFunction.NONE;
  Normalization normalization = Normalization.NONE;

  public GeneralRegressionSpec( ModelSchema modelSchema, RegressionTable regressionTable, LinkFunction linkFunction )
    {
    super( modelSchema );
    this.linkFunction = linkFunction;

    addRegressionTable( regressionTable );
    }

  public GeneralRegressionSpec( ModelSchema modelSchema )
    {
    super( modelSchema );
    }

  public void addRegressionTable( RegressionTable regressionTable )
    {
    regressionTables.add( regressionTable );
    }

  public List<RegressionTable> getRegressionTables()
    {
    return regressionTables;
    }

  public void setNormalization( Normalization normalization )
    {
    this.normalization = normalization;
    }

  public Normalization getNormalization()
    {
    return normalization;
    }

  public LinkFunction getLinkFunction()
    {
    return linkFunction;
    }

  public void setLinkFunction( LinkFunction linkFunction )
    {
    this.linkFunction = linkFunction;
    }

  public ExpressionEvaluator[] getRegressionTableEvaluators( Fields argumentFields )
    {
    List<RegressionTable> tables = new ArrayList<RegressionTable>( regressionTables );

    final DataField predictedField = getModelSchema().getPredictedField( getModelSchema().getPredictedFieldNames().get( 0 ) );

    // order tables in category order as this is the declared field name order
    if( predictedField instanceof CategoricalDataField )
      {
      Ordering<RegressionTable> ordering = Ordering.natural().onResultOf( new Function<RegressionTable, Comparable>()
      {
      private List<String> categories = ( (CategoricalDataField) predictedField ).getCategories();

      @Override
      public Comparable apply( RegressionTable regressionTable )
        {
        return categories.indexOf( regressionTable.getTargetCategory() );
        }
      } );

      Collections.sort( tables, ordering );
      }

    ExpressionEvaluator[] evaluators = new ExpressionEvaluator[ tables.size() ];

    for( int i = 0; i < tables.size(); i++ )
      evaluators[ i ] = tables.get( i ).bind( argumentFields );

    return evaluators;
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "GeneralRegressionSpec{" );
    sb.append( "generalRegressionTables=" ).append( regressionTables );
    sb.append( ", linkFunction=" ).append( linkFunction );
    sb.append( ", normalization=" ).append( normalization );
    sb.append( '}' );
    return sb.toString();
    }
  }
