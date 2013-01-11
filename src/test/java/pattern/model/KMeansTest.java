/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.model;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pattern.Classifier;


public class KMeansTest extends ModelTest
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( KMeansTest.class );

  /**
   * evaluate sample model + data from temp files
   *
   * @throws Exception
   */
  @Test
  public void testMain() throws Exception
    {
    String pmml_file = makeFile( "km_test", ".xml", pmml_text );
    String data_file = makeFile( "km_test", ".tsv", data_text );

    Classifier classifier = new Classifier( pmml_file );
    eval_data( data_file, classifier );
    }

  protected String pmml_text = "<?xml version=\"1.0\"?><PMML version=\"3.2\" xmlns=\"http://www.dmg.org/PMML-3_2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.dmg.org/PMML-3_2 http://www.dmg.org/v3-2/pmml-3-2.xsd\"><Header copyright=\"Copyright (c)2012, Concurrent, Inc. (www.concurrentinc.com)\" description=\"KMeans cluster model\"><Extension name=\"user\" value=\"ceteri\" extender=\"Rattle/PMML\"/><Application name=\"Rattle/PMML\" version=\"1.2.30\"/><Timestamp>2013-01-10 18:44:35</Timestamp></Header><DataDictionary numberOfFields=\"4\"><DataField name=\"sepal_length\" optype=\"continuous\" dataType=\"double\"/><DataField name=\"sepal_width\" optype=\"continuous\" dataType=\"double\"/><DataField name=\"petal_length\" optype=\"continuous\" dataType=\"double\"/><DataField name=\"petal_width\" optype=\"continuous\" dataType=\"double\"/></DataDictionary><ClusteringModel modelName=\"KMeans_Model\" functionName=\"clustering\" algorithmName=\"KMeans: Hartigan and Wong\" modelClass=\"centerBased\" numberOfClusters=\"3\"><MiningSchema><MiningField name=\"sepal_length\" usageType=\"active\"/><MiningField name=\"sepal_width\" usageType=\"active\"/><MiningField name=\"petal_length\" usageType=\"active\"/><MiningField name=\"petal_width\" usageType=\"active\"/></MiningSchema><ComparisonMeasure kind=\"distance\"><squaredEuclidean/></ComparisonMeasure><ClusteringField field=\"sepal_length\" compareFunction=\"absDiff\"/><ClusteringField field=\"sepal_width\" compareFunction=\"absDiff\"/><ClusteringField field=\"petal_length\" compareFunction=\"absDiff\"/><ClusteringField field=\"petal_width\" compareFunction=\"absDiff\"/><Cluster name=\"1\" size=\"38\"><Array n=\"4\" type=\"real\">6.85 3.07368421052632 5.74210526315789 2.07105263157895</Array></Cluster><Cluster name=\"2\" size=\"50\"><Array n=\"4\" type=\"real\">5.006 3.428 1.462 0.246</Array></Cluster><Cluster name=\"3\" size=\"62\"><Array n=\"4\" type=\"real\">5.90161290322581 2.74838709677419 4.39354838709678 1.43387096774194</Array></Cluster></ClusteringModel></PMML>";

  protected String data_text = "sepal_length\tsepal_width\tpetal_length\tpetal_width\tpredict\n5.1\t3.5\t1.4\t0.2\t2";
  }
