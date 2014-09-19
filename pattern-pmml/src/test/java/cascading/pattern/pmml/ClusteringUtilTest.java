/*
 * Copyright (c) 2014 Concurrent, Inc. All Rights Reserved.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.ClusteringField;
import org.dmg.pmml.ClusteringModel;
import org.dmg.pmml.CompareFunctionType;
import org.dmg.pmml.ComparisonMeasure;
import org.dmg.pmml.ComparisonMeasure.Kind;
import org.dmg.pmml.FieldName;
import org.junit.Test;

import static org.junit.Assert.*;
import cascading.pattern.model.clustering.compare.AbsoluteDifferenceCompareFunction;
import cascading.pattern.model.clustering.compare.CompareFunction;
import cascading.pattern.model.clustering.compare.DeltaCompareFunction;
import cascading.pattern.model.clustering.compare.EqualCompareFunction;
import cascading.pattern.model.clustering.compare.GaussianSimilarityCompareFunction;

public class ClusteringUtilTest
  {
  @Test
  public void testGetDefaultCompareFunction()
    {
    testGetDefaultCompareFunction( CompareFunctionType.ABS_DIFF, AbsoluteDifferenceCompareFunction.class );
    testGetDefaultCompareFunction( CompareFunctionType.DELTA, DeltaCompareFunction.class );
    testGetDefaultCompareFunction( CompareFunctionType.EQUAL, EqualCompareFunction.class );
    CompareFunction fun = testGetDefaultCompareFunction( CompareFunctionType.GAUSS_SIM,
        GaussianSimilarityCompareFunction.class );
    GaussianSimilarityCompareFunction gaussSim = (GaussianSimilarityCompareFunction) fun;
    assertEquals( new GaussianSimilarityCompareFunction( null ), gaussSim );
    }

  private CompareFunction testGetDefaultCompareFunction( CompareFunctionType type, Class<?> expectedClass )
    {
    ClusteringModel model = new ClusteringModel( null, null, null, null, null );
    ComparisonMeasure measure = new ComparisonMeasure( Kind.SIMILARITY );
    measure.setCompareFunction( type );
    model.setComparisonMeasure( measure );

    CompareFunction fun = ClusteringUtil.getDefaultComparisonFunction( model );

    assertEquals( expectedClass, fun.getClass() );
    return fun;
    }

  @Test
  public void testGetCompareFunctions()
    {
    ClusteringModel model = new ClusteringModel( null, null, null, null, null );

    Map<ClusteringField, CompareFunction> fieldMap = new HashMap<ClusteringField, CompareFunction>();
    fieldMap.put( field( "default", null, 2.0 ), new GaussianSimilarityCompareFunction(2.0 ) );
    fieldMap.put( field( "absDiff", CompareFunctionType.ABS_DIFF, null ), new AbsoluteDifferenceCompareFunction() );
    fieldMap.put( field( "delta", CompareFunctionType.DELTA, null ), new DeltaCompareFunction() );
    fieldMap.put( field( "equal", CompareFunctionType.EQUAL, null ), new EqualCompareFunction() );
    fieldMap.put( field( "gaussSim", CompareFunctionType.GAUSS_SIM, 1.0 ), new GaussianSimilarityCompareFunction( 1.0 ) );

    List<ClusteringField> fields = model.getClusteringFields();
    Map<String, CompareFunction> expected = new HashMap<String, CompareFunction>();
    
    for( Map.Entry<ClusteringField, CompareFunction> entry: fieldMap.entrySet() )
      {
      fields.add( entry.getKey() );
      expected.put( entry.getKey().getField().getValue(), entry.getValue() );
      }
    
    CompareFunction defaultFunction = new GaussianSimilarityCompareFunction( 2.0 );
    Map<String, CompareFunction> result = ClusteringUtil.getComparisonFunctions( model, defaultFunction );
    assertEquals(expected, result);
    }

  private ClusteringField field( String name, CompareFunctionType type, Double similarityScale )
    {
    ClusteringField field = new ClusteringField( new FieldName( name ) );
    field.setCompareFunction( type );
    field.setSimilarityScale( similarityScale );
    return field;
    }
  }