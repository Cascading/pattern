/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.io.Serializable;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import pattern.model.Model;
import pattern.model.MiningModel;
import pattern.model.lm.RegressionModel;
import pattern.model.tree.TreeModel;


public class Classifier implements Serializable
  {
  public Model model;

  /**
   * Construct a Classifier by parsing the PMML file, verifying the
   * model type, and building an appropriate Model.
   *
   * @param pmml_file PMML file
   * @throws PatternException
   */
  public Classifier( String pmml_file ) throws PatternException
    {
    PMML pmml = new PMML( pmml_file );

    if( PMML.Models.MINING.equals( pmml.model_type ) )
      model = new MiningModel( pmml );
    else if( PMML.Models.TREE.equals( pmml.model_type ) )
      model = new TreeModel( pmml );
    else if( PMML.Models.REGRESSION.equals( pmml.model_type ) )
      model = new RegressionModel( pmml );
    else
      throw new PatternException( "unsupported model type: " + pmml.model_type.name() );
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
