/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cascading.flow.FlowProcess;
import cascading.scheme.hadoop.TextLine;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleEntryIterator;
import pattern.model.Model;
import pattern.model.MiningModel;
import pattern.model.clust.ClusteringModel;
import pattern.model.lm.RegressionModel;
import pattern.model.tree.TreeModel;


public class Classifier implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( Classifier.class );

  public Model model;

  /**
   * Construct a Classifier by parsing the PMML file, verifying the
   * model type, and building an appropriate Model.
   *
   * @param pmmlUri XML source for the PMML description
   * @throws PatternException
   */
  public Classifier( String pmmlUri ) throws PatternException
    {
    try
      {
	  //PMML pmml = new PMML( new FileReader( pmmlUri ) );
      PMML pmml = new PMML( getSourceReader( pmmlUri ) );

      if( PMML.Models.MINING.equals( pmml.model_type ) )
        model = new MiningModel( pmml );
      else if( PMML.Models.TREE.equals( pmml.model_type ) )
        model = new TreeModel( pmml );
      else if( PMML.Models.REGRESSION.equals( pmml.model_type ) )
        model = new RegressionModel( pmml );
      else if( PMML.Models.CLUSTERING.equals( pmml.model_type ) )
        model = new ClusteringModel( pmml );
      else
        throw new PatternException( "unsupported model type: " + pmml.model_type.name() );
      }
    catch( IOException exception )
      {
      LOG.error( "could not read PMML file", exception );
      throw new PatternException( " could not read PMML file", exception );
      }
    }

  /**
   * Construct a Reader by reading the PMML file into a string buffer
   * first. This is needed in the case of cloud deployment where the
   * PMML source may not be in a typical file system, and therefore
   * needs credentials to be read. In the case of Amazon AWS, the PMML
   * source will likely be in an S3 bucket, which the java.io.*
   * classes cannot access directly. So we leverage Cascading built-in
   * support for an Hfs tap to read the text.
   *
   * @param pmmlUri XML source for the PMML description
   * @return Reader
   * @throws IOException
   */
  public Reader getSourceReader( String pmmlUri ) throws IOException
    {
    Hfs pmmlTap = new Hfs( new TextLine( new Fields( "line" ) ), pmmlUri );
    TupleEntryIterator iterator = pmmlTap.openForRead( FlowProcess.NULL );
    StringBuilder sb = new StringBuilder();

    while( iterator.hasNext() )
      {
      TupleEntry tuple = (TupleEntry) iterator.next();
      sb.append( tuple.getString( "line" ) );
      }

    LOG.debug( "PMML {}", sb.toString() );

    return new StringReader( sb.toString() );
    }

  /**
   * Prepare to classify with this model. Called immediately before
   * the enclosing Operation instance is put into play processing
   * Tuples.
   */
  public void prepare()
    {
    model.prepare();
    }

  /**
   * Classify an input tuple, returning the predicted label.
   *
   * @param values tuple values
   * @return String
   * @throws PatternException
   */
  public String classifyTuple( Tuple values ) throws PatternException
    {
    return model.classifyTuple( values );
    }
  }
