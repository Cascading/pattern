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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.sax.SAXSource;

import cascading.flow.AssemblyPlanner;
import cascading.flow.FlowDescriptors;
import cascading.flow.planner.PlannerException;
import cascading.pattern.PatternException;
import cascading.pattern.datafield.ContinuousDataField;
import cascading.pattern.ensemble.EnsembleSpec;
import cascading.pattern.ensemble.ParallelEnsembleAssembly;
import cascading.pattern.ensemble.selection.SelectionStrategy;
import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.ModelScoringFunction;
import cascading.pattern.model.clustering.ClusteringFunction;
import cascading.pattern.model.clustering.ClusteringSpec;
import cascading.pattern.model.clustering.measure.EuclideanMeasure;
import cascading.pattern.model.clustering.measure.SquaredEuclideanMeasure;
import cascading.pattern.model.generalregression.CategoricalRegressionFunction;
import cascading.pattern.model.generalregression.GeneralRegressionSpec;
import cascading.pattern.model.generalregression.LinkFunction;
import cascading.pattern.model.generalregression.PredictionRegressionFunction;
import cascading.pattern.model.generalregression.RegressionTable;
import cascading.pattern.model.tree.Tree;
import cascading.pattern.model.tree.TreeFunction;
import cascading.pattern.model.tree.TreeSpec;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.pipe.assembly.Coerce;
import cascading.pipe.assembly.Retain;
import cascading.scheme.util.FieldTypeResolver;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import cascading.util.Util;
import org.dmg.pmml.Cluster;
import org.dmg.pmml.ClusteringModel;
import org.dmg.pmml.ComparisonMeasure;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Euclidean;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.GeneralRegressionModel;
import org.dmg.pmml.Measure;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.Model;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.dmg.pmml.RegressionModel;
import org.dmg.pmml.Segment;
import org.dmg.pmml.SquaredEuclidean;
import org.dmg.pmml.TreeModel;
import org.dmg.pmml.True;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import static cascading.pattern.pmml.DataFields.createDataFields;
import static cascading.pattern.pmml.TreeUtil.createTree;

/**
 * Class PMMLPlanner is an implementation of a Cascading {@link AssemblyPlanner} that will convert a PMML document into
 * a Cascading {@link cascading.flow.Flow} instance.
 * <p/>
 * Predictive Model Markup Language (<a href="http://en.wikipedia.org/wiki/Predictive_Model_Markup_Language">PMML</a>)
 * is an XML format use to share common machine learning model parameters between applications. PMML documents are
 * typically exported from tools such as R after a model has been created..
 * <p/>
 * PMML is very flexible, but sometimes it doesn't declare defaults for missing elements. For example, the
 * "predicted" field is optional (the output field), but Cascading requires it for obvious reasons.
 * <p/>
 * So methods on this class help set sensible defaults if values are missing in the PMML document.
 * <p/>
 * PMMLPlanner in essence maps the PMML elements to Pattern model classes and populates those models with the
 * given parameters. There may not always be a 1 to 1 correlation of elements to Pattern models, for example PMML
 * regression is mapped to Pattern's {@link GeneralRegressionSpec}.
 * <p/>
 * To use, hand PMMLPlanner an PMML XML file, and pass the PMMLPlanner instance to
 * {@link cascading.flow.FlowDef#addAssemblyPlanner(cascading.flow.AssemblyPlanner)}. Use the FlowDef as usual from
 * there. The {@link cascading.flow.FlowConnector#connect(cascading.flow.FlowDef)} will then return a
 * {@link cascading.flow.Flow} that will score your data.
 * <p/>
 * Optionally {@link #resolveAssembly(cascading.pipe.Pipe)} can be used directly, but requires more Cascading
 * experience.
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
  private Fields defaultPredictedField;

  /**
   * Instantiates a new PMML planner.
   *
   * @param pmmlFile the pmml file
   */
  public PMMLPlanner( File pmmlFile )
    {
    this.pmmlFile = pmmlFile;
    }

  /**
   * Instantiates a new PMML planner.
   *
   * @param pmmlStream the pmml stream
   */
  public PMMLPlanner( InputStream pmmlStream )
    {
    this.pmmlStream = pmmlStream;
    }

  /** Instantiates a new PMML planner. */
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

  /**
   * If a predicted data field is not declared, use this field name.
   *
   * @param defaultPredictedField
   * @return
   */
  public PMMLPlanner setDefaultPredictedField( Fields defaultPredictedField )
    {
    this.defaultPredictedField = defaultPredictedField;

    return this;
    }

  public Fields getDefaultPredictedField()
    {
    return defaultPredictedField;
    }

  /**
   * Gets head name.
   *
   * @return the head name
   */
  public String getHeadName()
    {
    return headName;
    }

  /**
   * Sets incoming source head name to use.
   * <p/>
   * If not set, will use the only available input source Tap name. If more than one
   * source was declared, the planner will throw an exception.
   * <p/>
   * Cascading binds heads of the pipe assembly to the corresponding named source Tap.
   *
   * @param headName the head name
   * @return the head name
   */
  public PMMLPlanner setHeadName( String headName )
    {
    this.headName = headName;

    return this;
    }

  public String getBranchName()
    {
    return branchName;
    }

  /**
   * Sets branch name to use for the resulting pipe assembly.
   *
   * @param branchName the branch name
   * @return the branch name
   */
  public PMMLPlanner setBranchName( String branchName )
    {
    this.branchName = branchName;

    return this;
    }

  public String getTailName()
    {
    return tailName;
    }

  /**
   * Sets the outgoing tail name to use.
   * <p/>
   * If not set, will use the only available input sink Tap name. If more than one
   * sink was declared, the planner will throw an exception.
   * <p/>
   * Cascading binds tails of the pipe assembly to the corresponding named sink Tap.
   *
   * @param tailName the tail name
   * @return the tail name
   */
  public PMMLPlanner setTailName( String tailName )
    {
    this.tailName = tailName;

    return this;
    }

  public Fields getRetainIncomingFields()
    {
    return retainIncomingFields;
    }

  /**
   * Sets the incoming fields that should be retained, by default all incoming fields
   * are passed through the assembly from the source Tap.
   * <p/>
   * This must include the fields required by the underlying models.
   * <p/>
   * Use {@link #retainOnlyActiveIncomingFields()} to dynamically limit the result to
   * those required.
   *
   * @param retainIncomingFields the retain incoming fields
   * @return the retain incoming fields
   */
  public PMMLPlanner setRetainIncomingFields( Fields retainIncomingFields )
    {
    if( retainIncomingFields.isNone() )
      throw new IllegalArgumentException( "may not retain Fields.NONE" );

    this.retainIncomingFields = retainIncomingFields;

    return this;
    }

  /**
   * Retain only active incoming fields as declared by the PMML. All other fields
   * in the incoming source Tap will be discarded.
   *
   * @return the pMML planner
   */
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

  private PMML getPMML()
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

  private void setPMML( PMML pmml )
    {
    this.pmml = pmml;
    }

  private static PMML parse( InputStream inputStream )
    {
    try
      {
      InputSource source = new InputSource( inputStream );
      SAXSource transformedSource = ImportFilter.apply( source );
      return JAXBUtil.unmarshalPMML( transformedSource );
      }
    catch( Exception exception )
      {
      throw new PatternException( "could not read inputStream: " + inputStream, exception );
      }
    }

  /**
   * Returns the "active" fields declared in the PMML document.
   *
   * @return the active fields
   */
  public Fields getActiveFields()
    {
    return getPMMLModel().getActiveFields();
    }

  /**
   * Returns predicted fields declared in the PMML document.
   *
   * @return the predicted fields
   */
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

    String headName = findHeadName( context );

    if( context.getTails().size() == 0 && headName != null )
      tail = new Pipe( headName );
    else if( context.getTails().size() == 1 )
      tail = context.getTails().get( 0 );

    tail = applyCoercion( tail, context.getFlow().getSource( headName ) );

    tail = resolveAssembly( tail ); // branch name is applied

    tail = new Pipe( findTailName( context ), tail ); // bind the tail to the sink tailName

    return Arrays.asList( tail );
    }

  @Override
  public Map<String, String> getFlowDescriptor()
    {
    if( getPMMLFile() == null )
      return Collections.emptyMap();

    Map<String, String> map = new HashMap<String, String>();

    map.put( "pmml-resource", getPMMLFile().toString() );

    return map;
    }

  private Pipe applyCoercion( Pipe tail, Tap source )
    {
    Fields sourceFields = source.getSourceFields();
    FieldTypeResolver fieldTypeResolver = getFieldTypeResolver();

    Fields coercedFields = Fields.NONE;

    int count = 0;
    for( Comparable sourceField : sourceFields )
      {
      Type incoming = sourceFields.getType( sourceField );
      Type outgoing = fieldTypeResolver.inferTypeFrom( count++, sourceField.toString() );

      if( incoming != outgoing )
        coercedFields = coercedFields.append( new Fields( sourceField, outgoing ) );
      }

    if( coercedFields.isNone() )
      return tail;

    return new Coerce( tail, coercedFields );
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

  /**
   * Resolve assembly can be used if the give PMML needs to be extended beyond the capability of a Flow created
   * by a FlowConnector and FlowDef.
   * <p/>
   * The models will be appended to the given head Pipe instance, the final tail Pipe will be returned.
   *
   * @param pipe the pipe
   * @return the pipe
   */
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

  private Pipe handleTreeModel( Pipe tail, TreeModel model )
    {
    ModelSchema modelSchema = createModelSchema( model );

    Tree tree = createTree( model, modelSchema );

    TreeSpec treeSpec = new TreeSpec( modelSchema, tree );

    return create( tail, modelSchema, new TreeFunction( treeSpec ) );
    }

  private Pipe handleGeneralRegressionModel( Pipe tail, GeneralRegressionModel model )
    {
    ModelSchema modelSchema = createModelSchema( model );

    Set<String> parameterList = GeneralRegressionUtil.createParameters( model );
    Set<String> covariateList = GeneralRegressionUtil.createCovariates( model );
    Set<String> factorsList = GeneralRegressionUtil.createFactors( model );

    RegressionTable regressionTable = GeneralRegressionUtil.createPPMatrix( model, parameterList, factorsList, covariateList );

    LinkFunction linkFunction = LinkFunction.getFunction( model.getLinkFunction().value() );

    GeneralRegressionSpec modelParam = new GeneralRegressionSpec( modelSchema, regressionTable, linkFunction );

    return create( tail, modelSchema, new PredictionRegressionFunction( modelParam ) );
    }

  private Pipe handleRegressionModel( Pipe tail, RegressionModel model )
    {
    if( model.getFunctionName() == MiningFunctionType.REGRESSION )
      return handlePredictionRegressionModel( tail, model );

    if( model.getFunctionName() == MiningFunctionType.CLASSIFICATION )
      return handleCategoricalRegressionModel( tail, model );

    throw new UnsupportedOperationException( "unsupported mining type, got: " + model.getFunctionName() );
    }

  private Pipe handleCategoricalRegressionModel( Pipe tail, RegressionModel model )
    {
    ModelSchema modelSchema = createModelSchema( model );

    List<String> predictedCategories = new ArrayList<String>( modelSchema.getPredictedCategories( modelSchema.getPredictedFieldNames().get( 0 ) ) );

    if( predictedCategories.isEmpty() )
      throw new PatternException( "no categories specified" );

    GeneralRegressionSpec regressionSpec = new GeneralRegressionSpec( modelSchema );

    regressionSpec.setNormalization( RegressionUtil.getNormalizationMethod( model ) );

    for( org.dmg.pmml.RegressionTable regressionTable : model.getRegressionTables() )
      regressionSpec.addRegressionTable( RegressionUtil.createTable( regressionTable ) );

    return create( tail, modelSchema, new CategoricalRegressionFunction( regressionSpec ) );
    }

  private Pipe handlePredictionRegressionModel( Pipe tail, RegressionModel model )
    {
    if( model.getRegressionTables().size() != 1 )
      throw new UnsupportedOperationException( "regression model only supports a single regression table, got: " + model.getRegressionTables().size() );

    ModelSchema modelSchema = createModelSchema( model );

    org.dmg.pmml.RegressionTable regressionTable = model.getRegressionTables().get( 0 );

    GeneralRegressionSpec regressionSpec = new GeneralRegressionSpec( modelSchema );

    regressionSpec.setLinkFunction( LinkFunction.NONE );

    regressionSpec.addRegressionTable( RegressionUtil.createTable( regressionTable ) );

    return create( tail, modelSchema, new PredictionRegressionFunction( regressionSpec ) );
    }

  private Pipe handleClusteringModel( Pipe tail, ClusteringModel model )
    {
    ModelSchema modelSchema = createModelSchema( model );

    if( model.getModelClass() != ClusteringModel.ModelClass.CENTER_BASED )
      throw new UnsupportedOperationException( "unsupported model class, got: " + model.getModelClass() );

    ComparisonMeasure comparisonMeasure = model.getComparisonMeasure();

    if( comparisonMeasure.getKind() != ComparisonMeasure.Kind.DISTANCE )
      throw new UnsupportedOperationException( "unsupported comparison kind, got: " + comparisonMeasure.getKind() );

    ClusteringSpec clusteringSpec = new ClusteringSpec( modelSchema );

    Measure measure = comparisonMeasure.getMeasure();
    if( measure instanceof Euclidean )
      clusteringSpec.setComparisonMeasure( new EuclideanMeasure() );
    else if( measure instanceof SquaredEuclidean )
      clusteringSpec.setComparisonMeasure( new SquaredEuclideanMeasure() );
    else
      throw new UnsupportedOperationException( "unsupported comparison measure: " + comparisonMeasure );

    clusteringSpec.setDefaultCompareFunction( ClusteringUtil.setComparisonFunction( model ) );

    for( Cluster cluster : model.getClusters() )
      {
      List<Double> exemplar = (List<Double>) PMMLUtil.parseArray( cluster.getArray() );

      LOG.debug( "exemplar: {}", exemplar );

      clusteringSpec.addCluster( new cascading.pattern.model.clustering.Cluster( cluster.getName(), exemplar ) );
      }

    return create( tail, modelSchema, new ClusteringFunction( clusteringSpec ) );
    }

  private Pipe handleMiningModel( Pipe tail, MiningModel model )
    {
    ModelSchema modelSchema = createModelSchema( model );
    List<TreeSpec> models = new LinkedList<TreeSpec>();

    for( Segment segment : model.getSegmentation().getSegments() )
      {
      if( !segment.getPredicate().getClass().equals( True.class ) )
        throw new PatternException( "segment predicates currently not supported, got: " + segment.getPredicate() );

      Model segmentModel = segment.getModel();

      if( segmentModel instanceof TreeModel )
        models.add( new TreeSpec( modelSchema, createTree( (TreeModel) segmentModel, modelSchema ) ) );
      else
        throw new PatternException( "ensemble model currently not supported, got: " + segmentModel );
      }

    EnsembleSpec<TreeSpec> miningSpec = new EnsembleSpec<TreeSpec>( modelSchema, models );

    LOG.debug( "creating: {}, input: {}, output: {}", new Object[]{miningSpec, modelSchema.getInputFields(),
                                                                   modelSchema.getDeclaredFields()} );

    SelectionStrategy strategy = PMMLUtil.getSelectionStrategy( model );

    miningSpec.setSelectionStrategy( strategy );

    tail = new ParallelEnsembleAssembly( tail, miningSpec );

    return tail;
    }

  private Pipe create( Pipe tail, ModelSchema schemaParam, ModelScoringFunction function )
    {
    Fields inputFields = schemaParam.getInputFields();
    Fields declaredFields = schemaParam.getDeclaredFields();

    LOG.debug( "creating: {}, input: {}, output: {}", new Object[]{schemaParam, inputFields, declaredFields} );

    return new Each( tail, inputFields, function, Fields.ALL );
    }

  private ModelSchema createModelSchema( Model model )
    {
    ModelSchema modelSchema = new ModelSchema();

    for( MiningField miningField : model.getMiningSchema().getMiningFields() )
      {
      DataField dataField = getPMMLModel().getDataField( miningField.getName() );

      if( miningField.getUsageType() == FieldUsageType.ACTIVE )
        modelSchema.addExpectedField( createDataFields( dataField ) );
      else if( miningField.getUsageType() == FieldUsageType.PREDICTED )
        modelSchema.setPredictedFields( createDataFields( dataField ) );
      }

    if( modelSchema.getPredictedFieldNames().isEmpty() && defaultPredictedField == null )
      throw new PatternException( "no predicted field name provided in PMML model, use setDefaultPredictedField() method" );

    if( modelSchema.getPredictedFieldNames().isEmpty() )
      modelSchema.setPredictedFields( new ContinuousDataField( defaultPredictedField ) );

    return modelSchema;
    }

  }
