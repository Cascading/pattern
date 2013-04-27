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
import cascading.pattern.model.clustering.ClusteringFunction;
import cascading.pattern.model.clustering.ClusteringSpec;
import cascading.pattern.model.clustering.SquaredEuclidean;
import cascading.pattern.model.randomforest.RandomForestFunction;
import cascading.pattern.model.randomforest.RandomForestSpec;
import cascading.pattern.model.regression.RegressionFunction;
import cascading.pattern.model.regression.RegressionSpec;
import cascading.pattern.model.regression.RegressionTable;
import cascading.pattern.model.regression.predictor.CategoricalPredictor;
import cascading.pattern.model.regression.predictor.NumericPredictor;
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

    clusteringSpec.addCluster( new SquaredEuclidean( "1", 5.006d, 3.428d, 1.462d, 0.246d ) );
    clusteringSpec.addCluster( new SquaredEuclidean( "2", 5.9296875d, 2.7578125d, 4.4109375d, 1.4390625d ) );
    clusteringSpec.addCluster( new SquaredEuclidean( "3", 6.85277777777778d, 3.075d, 5.78611111111111d, 2.09722222222222d ) );

    ClusteringFunction clusteringFunction = new ClusteringFunction( clusteringSpec );

    TupleEntry tupleArguments = new TupleEntry( expectedFields, new Tuple( 6.4d, 3.1d, 5.5d, 1.8d ) );

    TupleListCollector collector = invokeFunction( clusteringFunction, tupleArguments, predictedFields );

    assertEquals( new Tuple( "3" ), collector.entryIterator().next().getTuple() );
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
  public void testRegressionFunction()
    {
    Fields predictedFields = new Fields( "sepal_length", double.class );

    Fields expectedFields = Fields.NONE
      .append( new Fields( "sepal_width", double.class ) )
      .append( new Fields( "petal_length", double.class ) )
      .append( new Fields( "petal_width", double.class ) )
      .append( new Fields( "species", String.class ) );

    ModelSchema modelSchema = new ModelSchema( expectedFields, predictedFields );

    RegressionSpec regressionSpec = new RegressionSpec( modelSchema );

    RegressionTable regressionTable = new RegressionTable();
    regressionTable.setIntercept( 2.24166872421148d );

    regressionTable.addPredictor( new NumericPredictor( "sepal_width", 0.53448203205212d ) );
    regressionTable.addPredictor( new NumericPredictor( "petal_length", 0.691035562908626d ) );
    regressionTable.addPredictor( new NumericPredictor( "petal_width", -0.21488157609202d ) );

    CategoricalPredictor species = new CategoricalPredictor( "species" );
    species.addCategory( "setosa", 0d );
    species.addCategory( "versicolor", -0.43150751368126d );
    species.addCategory( "virginica", -0.61868924203063d );

    regressionTable.addPredictor( species );

    regressionSpec.addRegressionTable( regressionTable );

    RegressionFunction regressionFunction = new RegressionFunction( regressionSpec );

    TupleEntry tupleArguments = new TupleEntry( expectedFields, new Tuple( 3d, 1.3d, 0.2d, "setosa" ) );

    TupleListCollector collector = invokeFunction( regressionFunction, tupleArguments, predictedFields );

    assertEquals( new Tuple( 4.70048473693065d ), collector.entryIterator().next().getTuple() );
    }

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
   * <MiningField name="label" usageType="predicted"/>
   * <MiningField name="var0" usageType="active"/>
   * <MiningField name="var1" usageType="active"/>
   * <MiningField name="var2" usageType="active"/>
   * </MiningSchema>
   * <Segmentation multipleModelMethod="majorityVote">
   * <Segment id="1">
   * <True/>
   * <TreeModel modelName="randomForest_Model" functionName="classification" algorithmName="randomForest" splitCharacteristic="binarySplit">
   * <MiningSchema>
   * <MiningField name="label" usageType="predicted"/>
   * <MiningField name="var0" usageType="active"/>
   * <MiningField name="var1" usageType="active"/>
   * <MiningField name="var2" usageType="active"/>
   * </MiningSchema>
   * <Node id="1">
   * <True/>
   * <Node id="2">
   * <SimplePredicate field="var0" operator="lessOrEqual" value="0.5"/>
   * <Node id="4" score="1">
   * <SimplePredicate field="var2" operator="lessOrEqual" value="0.5"/>
   * </Node>
   * <Node id="5" score="0">
   * <SimplePredicate field="var2" operator="greaterThan" value="0.5"/>
   * </Node>
   * </Node>
   * <Node id="3">
   * <SimplePredicate field="var0" operator="greaterThan" value="0.5"/>
   * <Node id="6" score="0">
   * <SimplePredicate field="var1" operator="lessOrEqual" value="0.5"/>
   * </Node>
   * <Node id="7" score="1">
   * <SimplePredicate field="var1" operator="greaterThan" value="0.5"/>
   * </Node>
   * </Node>
   * </Node>
   * </TreeModel>
   * </Segment>
   * <Segment id="2">
   * <True/>
   * <TreeModel modelName="randomForest_Model" functionName="classification" algorithmName="randomForest" splitCharacteristic="binarySplit">
   * <MiningSchema>
   * <MiningField name="label" usageType="predicted"/>
   * <MiningField name="var0" usageType="active"/>
   * <MiningField name="var1" usageType="active"/>
   * <MiningField name="var2" usageType="active"/>
   * </MiningSchema>
   * <Node id="1">
   * <True/>
   * <Node id="2" score="0">
   * <SimplePredicate field="var1" operator="lessOrEqual" value="0.5"/>
   * </Node>
   * <Node id="3" score="1">
   * <SimplePredicate field="var1" operator="greaterThan" value="0.5"/>
   * </Node>
   * </Node>
   * </TreeModel>
   * </Segment>
   * </Segmentation>
   * <p/>
   * label	var0	var1	var2	order_id	predict
   * 1	0	1	0	6f8e1014	1
   */
  public void testRandomForest()
    {
    Fields predictedFields = new Fields( "label", String.class );

    Fields expectedFields = Fields.NONE
      .append( new Fields( "var0", double.class ) )
      .append( new Fields( "var1", double.class ) )
      .append( new Fields( "var2", double.class ) );

    ModelSchema modelSchema = new ModelSchema( expectedFields, predictedFields );

    RandomForestSpec randomForestSpec = new RandomForestSpec( modelSchema );

    {
    TreeSpec treeSpec = new TreeSpec( modelSchema );

    Tree tree = new Tree( "1" );

    tree.addPredicate( "1", "2", new LessOrEqualThanPredicate( "var0", 0.5d ) );
    tree.addPredicate( "2", "4", new LessOrEqualThanPredicate( "var2", 0.5d ), "1" );
    tree.addPredicate( "2", "5", new GreaterThanPredicate( "var2", 0.5d ), "0" );
    tree.addPredicate( "1", "3", new GreaterThanPredicate( "var0", 0.5d ) );
    tree.addPredicate( "3", "6", new LessOrEqualThanPredicate( "var1", 0.5d ), "0" );
    tree.addPredicate( "3", "7", new GreaterThanPredicate( "var1", 0.5d ), "1" );

    treeSpec.setTree( tree );

    randomForestSpec.addTreeSpec( treeSpec );
    }

    {
    TreeSpec treeSpec = new TreeSpec( modelSchema );

    Tree tree = new Tree( "1" );

    tree.addPredicate( "1", "2", new LessOrEqualThanPredicate( "var1", 0.5d ), "1" );
    tree.addPredicate( "1", "3", new GreaterThanPredicate( "var1", 0.5d ), "1" );

    treeSpec.setTree( tree );

    randomForestSpec.addTreeSpec( treeSpec );
    }

    RandomForestFunction randomForestFunction = new RandomForestFunction( randomForestSpec );

    TupleEntry tupleArguments = new TupleEntry( expectedFields, new Tuple( 0d, 1d, 0d ) );

    TupleListCollector collector = invokeFunction( randomForestFunction, tupleArguments, predictedFields );

    assertEquals( new Tuple( "1" ), collector.entryIterator().next().getTuple() );
    }
  }
