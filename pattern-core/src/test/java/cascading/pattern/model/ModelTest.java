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

import cascading.CascadingTestCase;
import cascading.pattern.model.clustering.Cluster;
import cascading.pattern.model.clustering.ClusteringFunction;
import cascading.pattern.model.clustering.ClusteringSpec;
import cascading.pattern.model.clustering.compare.AbsoluteDifferenceCompareFunction;
import cascading.pattern.model.clustering.measure.SquaredEuclideanMeasure;
import cascading.pattern.model.generalregression.CategoricalRegressionFunction;
import cascading.pattern.model.generalregression.GeneralRegressionSpec;
import cascading.pattern.model.generalregression.LinkFunction;
import cascading.pattern.model.generalregression.Parameter;
import cascading.pattern.model.generalregression.PredictionRegressionFunction;
import cascading.pattern.model.generalregression.RegressionTable;
import cascading.pattern.model.generalregression.normalization.SoftMaxNormalization;
import cascading.pattern.model.generalregression.predictor.CovariantPredictor;
import cascading.pattern.model.generalregression.predictor.FactorPredictor;
import cascading.pattern.model.tree.Tree;
import cascading.pattern.model.tree.TreeFunction;
import cascading.pattern.model.tree.TreeSpec;
import cascading.pattern.model.tree.predicate.GreaterThanPredicate;
import cascading.pattern.model.tree.predicate.LessOrEqualThanPredicate;
import cascading.pattern.util.Logging;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleListCollector;
import org.junit.Test;

/**
 *
 */
public class ModelTest extends CascadingTestCase
  {
  public ModelTest()
    {
    }

  @Override
  public void setUp() throws Exception
    {
    Logging.setLogLevel( this.getClass(), "cascading.pattern", "debug" );

    super.setUp();
    }

  /**
   * <ComparisonMeasure kind="distance">
   * <squaredEuclidean/>
   * </ComparisonMeasure>
   * <ClusteringField field="sepal_length"/>
   * <ClusteringField field="sepal_width"/>
   * <ClusteringField field="petal_length"/>
   * <ClusteringField field="petal_width"/>
   * <Cluster name="1">
   * <Array n="4" type="real">5.006 3.428 1.462 0.246</Array>
   * </Cluster>
   * <Cluster name="2">
   * <Array n="4" type="real">5.9296875 2.7578125 4.4109375 1.4390625</Array>
   * </Cluster>
   * <Cluster name="3">
   * <Array n="4" type="real">6.85277777777778 3.075 5.78611111111111 2.09722222222222</Array>
   * </Cluster>
   * <p/>
   *
   * @throws Exception
   */
  @Test
  public void testClusteringFunction() throws Exception
    {
    Fields predictedFields = new Fields( "predicted", String.class );

    Fields expectedFields = Fields.NONE
      .append( new Fields( "sepal_length", double.class ) )
      .append( new Fields( "sepal_width", double.class ) )
      .append( new Fields( "petal_length", double.class ) )
      .append( new Fields( "petal_width", double.class ) );

    ModelSchema modelSchema = new ModelSchema( expectedFields, predictedFields );

    ClusteringSpec clusteringSpec = new ClusteringSpec( modelSchema );

    clusteringSpec.setDefaultCompareFunction( new AbsoluteDifferenceCompareFunction() );
    clusteringSpec.setComparisonMeasure( new SquaredEuclideanMeasure() );

    clusteringSpec.addCluster( new Cluster( "1", 5.006d, 3.428d, 1.462d, 0.246d ) );
    clusteringSpec.addCluster( new Cluster( "2", 5.9296875d, 2.7578125d, 4.4109375d, 1.4390625d ) );
    clusteringSpec.addCluster( new Cluster( "3", 6.85277777777778d, 3.075d, 5.78611111111111d, 2.09722222222222d ) );

    ClusteringFunction clusteringFunction = new ClusteringFunction( clusteringSpec );

//    TupleEntry tupleArguments = new TupleEntry( expectedFields, new Tuple( 6.9d, 3.1d, 4.9d, 1.5d ) );
    TupleEntry tupleArguments = new TupleEntry( expectedFields, new Tuple( 6.4d, 3.1d, 5.5d, 1.8d ) );

    TupleListCollector collector = invokeFunction( clusteringFunction, tupleArguments, predictedFields );

//    assertEquals( new Tuple( "2" ), collector.entryIterator().next().getTuple() );
    assertEquals( new Tuple( "3" ), collector.entryIterator().next().getTuple() );
    }

  @Test
  public void testTree()
    {
    Fields predictedFields = new Fields( "label", double.class );

    Fields expectedFields = Fields.NONE
      .append( new Fields( "var0", double.class ) )
      .append( new Fields( "var1", double.class ) )
      .append( new Fields( "var2", double.class ) );

    ModelSchema modelSchema = new ModelSchema( expectedFields, predictedFields );

    TreeSpec treeSpec = new TreeSpec( modelSchema );

    Tree tree = new Tree( "1" );

    tree.addPredicate( "1", "2", new LessOrEqualThanPredicate( "var0", 0.5d ) );
    tree.addPredicate( "2", "4", new LessOrEqualThanPredicate( "var2", 0.5d ), "1" );
    tree.addPredicate( "2", "5", new GreaterThanPredicate( "var2", 0.5d ), "0" );
    tree.addPredicate( "1", "3", new GreaterThanPredicate( "var0", 0.5d ) );
    tree.addPredicate( "3", "6", new LessOrEqualThanPredicate( "var1", 0.5d ), "0" );
    tree.addPredicate( "3", "7", new GreaterThanPredicate( "var1", 0.5d ), "1" );

    treeSpec.setTree( tree );

    TreeFunction treeFunction = new TreeFunction( treeSpec );

    TupleEntry tupleArguments = new TupleEntry( expectedFields, new Tuple( 0d, 1d, 0d ) );

    TupleListCollector collector = invokeFunction( treeFunction, tupleArguments, predictedFields );

    assertEquals( new Tuple( "1" ), collector.entryIterator().next().getTuple() );
    }

  /**
   * <MiningSchema>
   * <MiningField name="setosa" usageType="predicted"/>
   * <MiningField name="sepal_length" usageType="active"/>
   * <MiningField name="sepal_width" usageType="active"/>
   * <MiningField name="petal_length" usageType="active"/>
   * <MiningField name="petal_width" usageType="active"/>
   * </MiningSchema>
   * <ParameterList>
   * <Parameter name="p0" label="(Intercept)"/>
   * <Parameter name="p1" label="sepal_length"/>
   * <Parameter name="p2" label="sepal_width"/>
   * <Parameter name="p3" label="petal_length"/>
   * <Parameter name="p4" label="petal_width"/>
   * </ParameterList>
   * <FactorList/>
   * <CovariateList>
   * <Predictor name="sepal_length"/>
   * <Predictor name="sepal_width"/>
   * <Predictor name="petal_length"/>
   * <Predictor name="petal_width"/>
   * </CovariateList>
   * <PPMatrix>
   * <PPCell value="1" predictorName="sepal_length" parameterName="p1"/>
   * <PPCell value="1" predictorName="sepal_width" parameterName="p2"/>
   * <PPCell value="1" predictorName="petal_length" parameterName="p3"/>
   * <PPCell value="1" predictorName="petal_width" parameterName="p4"/>
   * </PPMatrix>
   * <ParamMatrix>
   * <PCell parameterName="p0" df="1" beta="-16.9456960387809"/>
   * <PCell parameterName="p1" df="1" beta="11.7592159418536"/>
   * <PCell parameterName="p2" df="1" beta="7.84157781514097"/>
   * <PCell parameterName="p3" df="1" beta="-20.0880078273996"/>
   * <PCell parameterName="p4" df="1" beta="-21.6076488529538"/>
   * </ParamMatrix>
   *
   * @throws Exception
   */
  @Test
  public void testGeneralRegressionFunction() throws Exception
    {
    Fields predictedFields = new Fields( "setosa", String.class );

    Fields expectedFields = Fields.NONE
      .append( new Fields( "sepal_length", double.class ) )
      .append( new Fields( "sepal_width", double.class ) )
      .append( new Fields( "petal_length", double.class ) )
      .append( new Fields( "petal_width", double.class ) );

    ModelSchema modelSchema = new ModelSchema( expectedFields, predictedFields );

    GeneralRegressionSpec regressionSpec = new GeneralRegressionSpec( modelSchema );

    regressionSpec.setLinkFunction( LinkFunction.LOGIT );

    RegressionTable table = new RegressionTable();

    table.addParameter( new Parameter( "p0", -16.9456960387809d ) );
    table.addParameter( new Parameter( "p1", 11.7592159418536d, new CovariantPredictor( "sepal_length" ) ) );
    table.addParameter( new Parameter( "p2", 7.84157781514097d, new CovariantPredictor( "sepal_width" ) ) );
    table.addParameter( new Parameter( "p3", -20.0880078273996d, new CovariantPredictor( "petal_length" ) ) );
    table.addParameter( new Parameter( "p4", -21.6076488529538d, new CovariantPredictor( "petal_width" ) ) );

    regressionSpec.addRegressionTable( table );

    PredictionRegressionFunction regressionFunction = new PredictionRegressionFunction( regressionSpec );

    TupleEntry tupleArguments = new TupleEntry( expectedFields, new Tuple( 5.1d, 3.8d, 1.6d, 0.2d ) );

    TupleListCollector collector = invokeFunction( regressionFunction, tupleArguments, predictedFields );

    assertEquals( 1.0d, collector.entryIterator().next().getTuple().getDouble( 0 ), 0.00001d );
    }

  /**
   * <MiningSchema>
   * <MiningField name="sepal_length" usageType="predicted"/>
   * <MiningField name="sepal_width" usageType="active"/>
   * <MiningField name="petal_length" usageType="active"/>
   * <MiningField name="petal_width" usageType="active"/>
   * <MiningField name="species" usageType="active"/>
   * </MiningSchema>
   * <RegressionTable intercept="2.24166872421148">
   * <NumericPredictor name="sepal_width" exponent="1" coefficient="0.53448203205212"/>
   * <NumericPredictor name="petal_length" exponent="1" coefficient="0.691035562908626"/>
   * <NumericPredictor name="petal_width" exponent="1" coefficient="-0.21488157609202"/>
   * <CategoricalPredictor name="species" value="setosa" coefficient="0"/>
   * <CategoricalPredictor name="species" value="versicolor" coefficient="-0.43150751368126"/>
   * <CategoricalPredictor name="species" value="virginica" coefficient="-0.61868924203063"/>
   * </RegressionTable>
   */
  @Test
  public void testRegressionFunctionWithFactors()
    {
    Fields predictedFields = new Fields( "sepal_length", double.class );

    Fields expectedFields = Fields.NONE
      .append( new Fields( "sepal_width", double.class ) )
      .append( new Fields( "petal_length", double.class ) )
      .append( new Fields( "petal_width", double.class ) )
      .append( new Fields( "species", String.class ) );

    ModelSchema modelSchema = new ModelSchema( expectedFields, predictedFields );

    GeneralRegressionSpec regressionSpec = new GeneralRegressionSpec( modelSchema );

    RegressionTable regressionTable = new RegressionTable();

    regressionTable.addParameter( new Parameter( "intercept", 2.24166872421148d ) );

    regressionTable.addParameter( new Parameter( "p1", 0.53448203205212d, new CovariantPredictor( "sepal_width" ) ) );
    regressionTable.addParameter( new Parameter( "p2", 0.691035562908626d, new CovariantPredictor( "petal_length" ) ) );
    regressionTable.addParameter( new Parameter( "p3", -0.21488157609202d, new CovariantPredictor( "petal_width" ) ) );

    regressionTable.addParameter( new Parameter( "p4", 0d, new FactorPredictor( "species", "setosa" ) ) );
    regressionTable.addParameter( new Parameter( "p5", -0.43150751368126d, new FactorPredictor( "species", "versicolor" ) ) );
    regressionTable.addParameter( new Parameter( "p6", -0.61868924203063d, new FactorPredictor( "species", "virginica" ) ) );

    regressionSpec.addRegressionTable( regressionTable );

    PredictionRegressionFunction regressionFunction = new PredictionRegressionFunction( regressionSpec );

    TupleEntry tupleArguments = new TupleEntry( expectedFields, new Tuple( 3d, 1.3d, 0.2d, "setosa" ) );

    TupleListCollector collector = invokeFunction( regressionFunction, tupleArguments, predictedFields );

    assertEquals( new Tuple( 4.70048473693065d ), collector.entryIterator().next().getTuple() );
    }

  /**
   * <RegressionModel modelName="multinom_Model" functionName="classification" algorithmName="multinom" normalizationMethod="softmax">
   * <MiningSchema>
   * <MiningField name="species" usageType="predicted"/>
   * <MiningField name="sepal_length" usageType="active"/>
   * <MiningField name="sepal_width" usageType="active"/>
   * <MiningField name="petal_length" usageType="active"/>
   * <MiningField name="petal_width" usageType="active"/>
   * </MiningSchema>
   * <Output>
   * <OutputField name="species" feature="predictedValue"/>
   * <OutputField name="Probability_setosa" optype="continuous" dataType="double" feature="probability" value="setosa"/>
   * <OutputField name="Probability_versicolor" optype="continuous" dataType="double" feature="probability" value="versicolor"/>
   * <OutputField name="Probability_virginica" optype="continuous" dataType="double" feature="probability" value="virginica"/>
   * </Output>
   * <RegressionTable intercept="86.7061379450354" targetCategory="versicolor">
   * <NumericPredictor name="sepal_length" exponent="1" coefficient="-11.3336819785783"/>
   * <NumericPredictor name="sepal_width" exponent="1" coefficient="-40.8601511206805"/>
   * <NumericPredictor name="petal_length" exponent="1" coefficient="38.439099544679"/>
   * <NumericPredictor name="petal_width" exponent="1" coefficient="-12.2920287460217"/>
   * </RegressionTable>
   * <RegressionTable intercept="-111.666532867146" targetCategory="virginica">
   * <NumericPredictor name="sepal_length" exponent="1" coefficient="-47.1170644419116"/>
   * <NumericPredictor name="sepal_width" exponent="1" coefficient="-51.6805606658275"/>
   * <NumericPredictor name="petal_length" exponent="1" coefficient="108.27736751831"/>
   * <NumericPredictor name="petal_width" exponent="1" coefficient="54.0277175236148"/>
   * </RegressionTable>
   * <RegressionTable intercept="0.0" targetCategory="setosa"/>
   * </RegressionModel>
   */
  @Test
  public void testClassifierRegressionFunction()
    {
    Fields predictedFields = new Fields( "species", String.class );

    Fields expectedFields = Fields.NONE
      .append( new Fields( "sepal_length", double.class ) )
      .append( new Fields( "sepal_width", double.class ) )
      .append( new Fields( "petal_length", double.class ) )
      .append( new Fields( "petal_width", double.class ) );

    ModelSchema modelSchema = new ModelSchema( expectedFields, predictedFields );

    modelSchema.setPredictedCategories( "species", "setosa", "versicolor", "virginica" );

    GeneralRegressionSpec regressionSpec = new GeneralRegressionSpec( modelSchema );

    regressionSpec.setNormalization( new SoftMaxNormalization() );

    {
    RegressionTable regressionTable = new RegressionTable( "versicolor" );

    regressionTable.addParameter( new Parameter( "intercept", 86.7061379450354d ) );
    regressionTable.addParameter( new Parameter( "p0", -11.3336819785783d, new CovariantPredictor( "sepal_length" ) ) );
    regressionTable.addParameter( new Parameter( "p1", -40.8601511206805d, new CovariantPredictor( "sepal_width" ) ) );
    regressionTable.addParameter( new Parameter( "p2", 38.439099544679d, new CovariantPredictor( "petal_length" ) ) );
    regressionTable.addParameter( new Parameter( "p3", -12.2920287460217d, new CovariantPredictor( "petal_width" ) ) );

    regressionSpec.addRegressionTable( regressionTable );
    }

    {
    RegressionTable regressionTable = new RegressionTable( "virginica" );

    regressionTable.addParameter( new Parameter( "intercept", -111.666532867146d ) );
    regressionTable.addParameter( new Parameter( "p0", -47.1170644419116d, new CovariantPredictor( "sepal_length" ) ) );
    regressionTable.addParameter( new Parameter( "p1", -51.6805606658275d, new CovariantPredictor( "sepal_width" ) ) );
    regressionTable.addParameter( new Parameter( "p2", 108.27736751831d, new CovariantPredictor( "petal_length" ) ) );
    regressionTable.addParameter( new Parameter( "p3", 54.0277175236148d, new CovariantPredictor( "petal_width" ) ) );

    regressionSpec.addRegressionTable( regressionTable );
    }

    {
    RegressionTable regressionTable = new RegressionTable( "setosa" );

    regressionTable.addParameter( new Parameter( "intercept", 0d ) );

    regressionSpec.addRegressionTable( regressionTable );
    }

    CategoricalRegressionFunction regressionFunction = new CategoricalRegressionFunction( regressionSpec );

    {
    TupleEntry tupleArguments = new TupleEntry( expectedFields, new Tuple( 7d, 3.2d, 4.7d, 1.4d ) );

    TupleListCollector collector = invokeFunction( regressionFunction, tupleArguments, modelSchema.getDeclaredFields() );

    assertEquals( "versicolor", collector.entryIterator().next().getTuple().getObject( 0 ) );
    }

    {
    TupleEntry tupleArguments = new TupleEntry( expectedFields, new Tuple( 5.8d, 4d, 1.2d, 0.2d ) );

    TupleListCollector collector = invokeFunction( regressionFunction, tupleArguments, modelSchema.getDeclaredFields() );

    assertEquals( "setosa", collector.entryIterator().next().getTuple().getObject( 0 ) );
    }
    }
  }
