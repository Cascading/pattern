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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cascading.flow.AssemblyPlanner;
import cascading.flow.planner.PlannerException;
import cascading.pattern.PatternException;
import cascading.pattern.model.ClassifierFunction;
import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.clustering.ClusteringFunction;
import cascading.pattern.model.clustering.ClusteringSpec;
import cascading.pattern.model.clustering.DistanceCluster;
import cascading.pattern.model.clustering.Euclidean;
import cascading.pattern.model.clustering.SquaredEuclidean;
import cascading.pattern.model.generalregression.GeneralRegressionFunction;
import cascading.pattern.model.generalregression.GeneralRegressionSpec;
import cascading.pattern.model.generalregression.GeneralRegressionTable;
import cascading.pattern.model.generalregression.LinkFunction;
import cascading.pattern.model.randomforest.RandomForestFunction;
import cascading.pattern.model.randomforest.RandomForestSpec;
import cascading.pattern.model.regression.RegressionFunction;
import cascading.pattern.model.regression.RegressionSpec;
import cascading.pattern.model.regression.predictor.Predictor;
import cascading.pattern.model.tree.Tree;
import cascading.pattern.model.tree.TreeFunction;
import cascading.pattern.model.tree.TreeSpec;
import cascading.pattern.pmml.generalregression.GLMUtil;
import cascading.pattern.pmml.regression.RegressionUtil;
import cascading.pattern.pmml.tree.TreeUtil;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.pipe.assembly.Retain;
import cascading.scheme.util.FieldTypeResolver;
import cascading.tuple.Fields;
import cascading.util.Util;
import org.dmg.pmml.Cluster;
import org.dmg.pmml.ClusteringModel;
import org.dmg.pmml.ComparisonMeasure;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.GeneralRegressionModel;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.Model;
import org.dmg.pmml.MultipleModelMethodType;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.dmg.pmml.RegressionModel;
import org.dmg.pmml.RegressionTable;
import org.dmg.pmml.Segment;
import org.dmg.pmml.TreeModel;
import org.jpmml.manager.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cascading.pattern.pmml.DataFields.createDataFields;

/**
 *
 */
public class PMMLPlanner implements AssemblyPlanner
  {
  private static final Logger LOG = LoggerFactory.getLogger( PMMLPlanner.class );

  File pmmlFile;
  InputStream pmmlStream;
  String branchName = "pmml";
  String headName;
  String tailName;

  private PMML pmml;
  private PMMLModel pmmlModel;
  private Fields retainIncomingFields;

  public PMMLPlanner( File pmmlFile )
    {
    this.pmmlFile = pmmlFile;
    }

  public PMMLPlanner( InputStream pmmlStream )
    {
    this.pmmlStream = pmmlStream;
    }

  public PMMLPlanner()
    {
    }

  public File getPMMLFile()
    {
    return pmmlFile;
    }

  public PMMLPlanner setPMMLInput( File pmmlFile )
    {
    this.pmmlFile = pmmlFile;

    return this;
    }

  public InputStream getPMMLStream()
    {
    return pmmlStream;
    }

  public PMMLPlanner setPMMLInput( InputStream pmmlStream )
    {
    this.pmmlStream = pmmlStream;

    return this;
    }

  public String getHeadName()
    {
    return headName;
    }

  public PMMLPlanner setHeadName( String headName )
    {
    this.headName = headName;

    return this;
    }

  public String getBranchName()
    {
    return branchName;
    }

  public PMMLPlanner setBranchName( String branchName )
    {
    this.branchName = branchName;

    return this;
    }

  public String getTailName()
    {
    return tailName;
    }

  public PMMLPlanner setTailName( String tailName )
    {
    this.tailName = tailName;

    return this;
    }

  public Fields getRetainIncomingFields()
    {
    return retainIncomingFields;
    }

  public PMMLPlanner setRetainIncomingFields( Fields retainIncomingFields )
    {
    if( retainIncomingFields.isNone() )
      throw new IllegalArgumentException( "may not retain Fields.NONE" );

    this.retainIncomingFields = retainIncomingFields;

    return this;
    }

  public PMMLPlanner retainOnlyActiveIncomingFields()
    {
    return setRetainIncomingFields( getActiveFields() );
    }

  protected PMMLModel getPMMLModel()
    {
    if( pmmlModel == null )
      this.pmmlModel = new PMMLModel( getPMML() );

    return pmmlModel;
    }

  protected PMML getPMML()
    {
    if( pmml == null )
      {
      InputStream stream = getPMMLStream();

      if( stream == null )
        stream = getFileInputStream();

      pmml = parse( stream );
      }

    return pmml;
    }

  private FileInputStream getFileInputStream()
    {
    try
      {
      return new FileInputStream( getPMMLFile() );
      }
    catch( FileNotFoundException exception )
      {
      throw new PatternException( "could not find pmml file: " + getPMMLFile(), exception );
      }
    }

  protected void setPMML( PMML pmml )
    {
    this.pmml = pmml;
    }

  private static PMML parse( InputStream inputStream )
    {
    try
      {
      return IOUtil.unmarshal( inputStream );
      }
    catch( Exception exception )
      {
      throw new PatternException( "could not read inputStream: " + inputStream, exception );
      }
    }

  public Fields getActiveFields()
    {
    return getPMMLModel().getActiveFields();
    }

  public Fields getPredictedFields()
    {
    return getPMMLModel().getPredictedFields();
    }

  public PMMLPlanner addDataTypes( Fields fields )
    {
    for( Comparable field : fields )
      {
      if( field instanceof Number )
        continue;

      addDataType( (String) field, fields.getType( fields.getPos( field ) ) );
      }

    return this;
    }

  public PMMLPlanner addDataType( String name, Type type )
    {
    FieldName fieldName = new FieldName( name );

    if( getPMMLModel().getDataField( fieldName ) != null )
      throw new IllegalStateException( "field name already in dictionary" );

    DataType dataType = DataTypes.getTypeToPmml( type );

    if( dataType == null )
      throw new IllegalStateException( "no data type mapping from: " + Util.getTypeName( type ) );

    getPMMLModel().addDataField( fieldName, OpType.CONTINUOUS, dataType );

    return this;
    }

  public FieldTypeResolver getFieldTypeResolver()
    {
    return new PMMLTypeResolver( this );
    }

  @Override
  public List<Pipe> resolveTails( Context context )
    {
    if( context.getTails().size() > 1 )
      throw new PlannerException( "assembly can only accept one inbound branch, got: " + context.getTails().size() );

    if( context.getTails().size() == 1 && getHeadName() != null )
      throw new PlannerException( "cannot specify a head name if there are incoming branches" );

    Pipe tail = null;

    if( context.getTails().size() == 0 && findHeadName( context ) != null )
      tail = new Pipe( findHeadName( context ) );
    else if( context.getTails().size() == 1 )
      tail = context.getTails().get( 0 );

    tail = resolveAssembly( tail ); // branch name is applied

    tail = new Pipe( findTailName( context ), tail ); // bind the tail to the sink tailName

    return Arrays.asList( tail );
    }

  private String findHeadName( Context context )
    {
    String headName;

    if( getHeadName() != null )
      headName = getHeadName();
    else if( context.getFlow().getSources().size() == 1 )
      headName = (String) context.getFlow().getSourceNames().get( 0 );
    else
      throw new IllegalStateException( "too man sources to choose from, found: " + context.getFlow().getSources().size() + ", use setHeadName" );

    return headName;
    }

  private String findTailName( Context context )
    {
    String tailName;

    if( getTailName() != null )
      tailName = getTailName();
    else if( context.getFlow().getSinks().size() == 1 )
      tailName = (String) context.getFlow().getSinkNames().get( 0 );
    else
      throw new IllegalStateException( "too man sinks to choose from, found: " + context.getFlow().getSinks().size() + ", use setTailName" );

    return tailName;
    }

  public Pipe resolveAssembly( Pipe pipe )
    {
    Pipe tail;

    if( pipe == null )
      tail = new Pipe( getBranchName() );
    else
      tail = new Pipe( getBranchName(), pipe );

    if( getRetainIncomingFields() != null )
      tail = new Retain( tail, getRetainIncomingFields() );

    for( Model model : getPMMLModel().getModels() )
      {
      if( !model.isScorable() )
        {
        LOG.info( "skipping non-scoreable model: " + model.getModelName() );
        continue;
        }

      if( model instanceof MiningModel )
        tail = handleMiningModel( tail, (MiningModel) model );
      else if( model instanceof ClusteringModel )
        tail = handleClusteringModel( tail, (ClusteringModel) model );
      else if( model instanceof RegressionModel )
        tail = handleRegressionModel( tail, (RegressionModel) model );
      else if( model instanceof GeneralRegressionModel )
        tail = handleGeneralRegressionModel( tail, (GeneralRegressionModel) model );
      else if( model instanceof TreeModel )
        tail = handleTreeModel( tail, (TreeModel) model );
      else
        throw new PatternException( "unsupported model type: " + model.getClass().getName() );
      }

    return tail;
    }

  private boolean isRandomForest( MiningModel model )
    {
    if( model.getSegmentation() == null )
      return false;

    for( Segment segment : model.getSegmentation().getSegments() )
      {
      if( segment.getModel() instanceof TreeModel )
        continue;

      return false;
      }

    if( model.getSegmentation().getMultipleModelMethod() != MultipleModelMethodType.MAJORITY_VOTE )
      throw new PatternException( "only majority vote method supported, got: " + model.getSegmentation().getMultipleModelMethod() );

    return true;
    }

  private Pipe handleRandomForestModel( Pipe tail, MiningModel model )
    {
    ModelSchema modelSchema = createModelSchema( model );
    List<TreeSpec> models = new LinkedList<TreeSpec>();

    for( Segment segment : model.getSegmentation().getSegments() )
      {
      TreeModel treeModel = (TreeModel) segment.getModel();
      Tree tree = TreeUtil.createTree( treeModel, modelSchema );

      models.add( new TreeSpec( tree ) );
      }

    RandomForestSpec miningSpec = new RandomForestSpec( modelSchema, models );

    return create( tail, modelSchema, new RandomForestFunction( miningSpec ) );
    }

  private Pipe handleTreeModel( Pipe tail, TreeModel model )
    {
    ModelSchema modelSchema = createModelSchema( model );

    Tree tree = TreeUtil.createTree( model, modelSchema );

    TreeSpec modelParam = new TreeSpec( modelSchema, tree );

    return create( tail, modelSchema, new TreeFunction( modelParam ) );
    }

  private Pipe handleGeneralRegressionModel( Pipe tail, GeneralRegressionModel model )
    {
    ModelSchema modelSchema = createModelSchema( model );

    Set<String> parameterList = GLMUtil.createParameters( model );
    Set<String> covariateList = GLMUtil.createCovariates( model );
    Set<String> factorsList = GLMUtil.createFactors( model );

    GeneralRegressionTable generalRegressionTable = GLMUtil.createPPMatrix( model, parameterList, factorsList, covariateList );

    LinkFunction linkFunction = LinkFunction.getFunction( model.getLinkFunction().value() );

    GeneralRegressionSpec modelParam = new GeneralRegressionSpec( modelSchema, generalRegressionTable, linkFunction );

    return create( tail, modelSchema, new GeneralRegressionFunction( modelParam ) );
    }

  private Pipe handleRegressionModel( Pipe tail, RegressionModel model )
    {
    if( model.getFunctionName() != MiningFunctionType.REGRESSION )
      throw new UnsupportedOperationException( "only regression function supported, got: " + model.getFunctionName() );

    if( model.getRegressionTables().size() != 1 )
      throw new UnsupportedOperationException( "regression model only supports a single regression table, got: " + model.getRegressionTables().size() );

    if( !model.getAlgorithmName().equals( "least squares" ) )
      throw new UnsupportedOperationException( "unsupported regression algorithm: " + model.getAlgorithmName() );

    ModelSchema modelSchema = createModelSchema( model );

    RegressionTable regressionTable = model.getRegressionTables().get( 0 );

    RegressionSpec regressionSpec = new RegressionSpec( modelSchema );

    double intercept = regressionTable.getIntercept();
    List<Predictor> predictors = RegressionUtil.createPredictors( regressionTable );

    regressionSpec.addRegressionTable( new cascading.pattern.model.regression.RegressionTable( intercept, predictors ) );

    return create( tail, modelSchema, new RegressionFunction( regressionSpec ) );
    }

  private Pipe handleClusteringModel( Pipe tail, ClusteringModel model )
    {
    ModelSchema modelSchema = createModelSchema( model );

    if( model.getModelClass() != ClusteringModel.ModelClass.CENTER_BASED )
      throw new UnsupportedOperationException( "unsupported model class, got: " + model.getModelClass() );

    ComparisonMeasure comparisonMeasure = model.getComparisonMeasure();

    if( comparisonMeasure.getKind() != ComparisonMeasure.Kind.DISTANCE )
      throw new UnsupportedOperationException( "unsupported comparison kind, got: " + comparisonMeasure.getKind() );

    boolean isSquared = comparisonMeasure.getSquaredEuclidean() != null;
    boolean isEuclidean = comparisonMeasure.getEuclidean() != null;

    if( isEuclidean && isSquared )
      throw new IllegalStateException( "cannot be both squared and euclidean models" );

    ClusteringSpec clusteringSpec = new ClusteringSpec( modelSchema );

    for( Cluster cluster : model.getClusters() )
      {
      List<Double> exemplar = PMMLUtil.parseArray( cluster.getArray() );

      LOG.debug( "exemplar: {}", exemplar );

      DistanceCluster distanceCluster;

      if( isEuclidean )
        distanceCluster = new Euclidean( cluster.getName(), exemplar );
      else if( isSquared )
        distanceCluster = new SquaredEuclidean( cluster.getName(), exemplar );
      else
        throw new UnsupportedOperationException( "unsupported comparison measure: " + comparisonMeasure );


      clusteringSpec.addCluster( distanceCluster );
      }

    return create( tail, modelSchema, new ClusteringFunction( clusteringSpec ) );
    }

  private Pipe handleMiningModel( Pipe tail, MiningModel model )
    {
    if( isRandomForest( model ) )
      tail = handleRandomForestModel( tail, model );
    else
      throw new PatternException( "unsupported mining model algorithm type: " + model.getAlgorithmName() );

    return tail;
    }

  private Pipe create( Pipe tail, ModelSchema schemaParam, ClassifierFunction function )
    {
    Fields inputFields = schemaParam.getInputFields();
    Fields declaredFields = schemaParam.getDeclaredFields();

    LOG.debug( "creating: {}, input: {}, output: {}", new Object[]{schemaParam, inputFields, declaredFields} );

    return new Each( tail, inputFields, function, Fields.ALL );
    }

  private ModelSchema createModelSchema( Model model )
    {
    ModelSchema schemaParam = new ModelSchema();

    for( MiningField miningField : model.getMiningSchema().getMiningFields() )
      {
      DataField dataField = getPMMLModel().getDataField( miningField.getName() );

      if( miningField.getUsageType() == FieldUsageType.ACTIVE )
        schemaParam.addExpectedField( createDataFields( dataField ) );
      else if( miningField.getUsageType() == FieldUsageType.PREDICTED )
        schemaParam.setPredictedFields( createDataFields( dataField ) );
      }

    return schemaParam;
    }
  }
