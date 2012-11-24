/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import pattern.tree.TreeModel;


public class ClassifierFactory
  {
  /**
   * Parse the given PMML file, verify the model type, and create the appropriate Classifier object.
   *
   * @param pmml_file PMML file
   * @return Classifier
   * @throws PatternException
   */
  public static Classifier getClassifier( String pmml_file ) throws PatternException
    {
    PMML pmml = new PMML( pmml_file );
    PMML.Models model_type = pmml.getModelType();
    Classifier classifier = null;

    if( PMML.Models.TREE.equals( model_type ) )
      classifier = new TreeModel( pmml );
    else
      throw new PatternException( "unsupported model type: " + model_type.name() );

    return classifier;
    }
  }
