/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import javax.xml.xpath.XPathConstants;

import pattern.rf.RandomForest;


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
    XPathReader reader = new XPathReader( pmml_file );
    Classifier classifier = null;

    String expr = "/PMML/MiningModel/Segmentation/Segment/TreeModel/@algorithmName";
    String model_type = (String) reader.read( expr, XPathConstants.STRING );

    if( "randomForest".equals( model_type ) )
      classifier = new RandomForest( reader );
    else
      throw new PatternException( "unsupported model type: " + model_type );

    return classifier;
    }
  }
