/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.rf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pattern.ClassifierFactory;
import pattern.PatternException;

import cascading.tuple.Tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class RandomForestClassifierTest
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( RandomForestClassifierTest.class );

  /**
   * evaluate sample model + data from temp files
   *
   * @throws Exception
   */
  @Test
  public void testMain() throws Exception
    {
    String pmml_file = makeFile( "rf_test", ".xml", pmml_text );
    String data_file = makeFile( "rf_test", ".tsv", data_text );

    RandomForestClassifier model = (RandomForestClassifier) ClassifierFactory.getClassifier( pmml_file );
    eval_data( data_file, model );
    }

  /**
   * @param base
   * @param suffix
   * @param text
   * @return
   */
  protected String makeFile( String base, String suffix, String text ) throws IOException
    {
    String filename = null;
    PrintStream out = null;

    try
      {
      File file = File.createTempFile( base, suffix );
      file.deleteOnExit();

      filename = file.getCanonicalFile().toString();

      if( LOG.isDebugEnabled() )
        LOG.debug( "file: {}", filename );

      out = new PrintStream( new FileOutputStream( file ) );
      out.print( text );
      }
    catch( IOException exception )
      {
      LOG.error( "could not create temp file", exception );
      fail( "cannot set up test environment" );
      }
    finally
      {
      if( out != null )
        {
        out.flush();
        out.close();
        }

      return filename;
      }
    }

  /**
   * @param data_file
   * @param model
   * @throws IOException
   * @throws PatternException
   */
  protected void eval_data( String data_file, RandomForestClassifier model ) throws IOException, PatternException
    {
    FileReader fr = new FileReader( data_file );
    BufferedReader br = new BufferedReader( fr );
    String line;
    int count = 0;

    while( ( line = br.readLine() ) != null )
      {
      if( count++ > 0 )
        {
        // for each tuple in the reference data, knowing that "label"
        // is in the first field...

        String[] test_vector = line.split( "\\t" );
        String predicted = test_vector[ test_vector.length - 1 ];

        Tuple values = new Tuple();
        int i = 1;

        for( String key : model.schema.keySet() )
          values.addString( test_vector[ i++ ] );

        // compare classifier label vs. predicted

        model.prepare();
        model.evalPredicates( values );

        HashMap<String, Integer> votes = new HashMap<String, Integer>();
        String label = model.tallyVotes( votes );

        if( !predicted.equals( label ) )
          {
          StringBuilder sb = new StringBuilder();

          sb.append( String.format( "regression: classifier label [ %s ] does not match predicted [ %s ]\n", label, predicted ) )
            .append( line ).append( "\n" )
            .append( "votes: " ).append( votes );

	  fail( sb.toString() );
          }

        assertEquals( "RandomForest", predicted, label );
        }
      }

    fr.close();
    }

  protected String pmml_text = "<?xml version=\"1.0\"?>\n<PMML xmlns=\"http://www.dmg.org/PMML-4_0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"4.0\" xsi:schemaLocation=\"http://www.dmg.org/PMML-4_0 http://www.dmg.org/v4-0/pmml-4-0.xsd\"><Header copyright=\"Copyright (c) 2012 ceteri\" description=\"Random Forest Tree Model\"><Extension name=\"user\" value=\"ceteri\" extender=\"Rattle/PMML\"/><Application name=\"Rattle/PMML\" version=\"1.2.30\"/><Timestamp>2012-10-22 19:39:28</Timestamp></Header><DataDictionary numberOfFields=\"4\"><DataField name=\"label\" optype=\"categorical\" dataType=\"string\"><Value value=\"0\"/><Value value=\"1\"/></DataField><DataField name=\"var0\" optype=\"continuous\" dataType=\"double\"/><DataField name=\"var1\" optype=\"continuous\" dataType=\"double\"/><DataField name=\"var2\" optype=\"continuous\" dataType=\"double\"/></DataDictionary><MiningModel modelName=\"randomForest_Model\" functionName=\"classification\"><MiningSchema><MiningField name=\"label\" usageType=\"predicted\"/><MiningField name=\"var0\" usageType=\"active\"/><MiningField name=\"var1\" usageType=\"active\"/><MiningField name=\"var2\" usageType=\"active\"/></MiningSchema><Segmentation multipleModelMethod=\"majorityVote\"><Segment id=\"1\"><True/><TreeModel modelName=\"randomForest_Model\" functionName=\"classification\" algorithmName=\"randomForest\" splitCharacteristic=\"binarySplit\"><MiningSchema><MiningField name=\"label\" usageType=\"predicted\"/><MiningField name=\"var0\" usageType=\"active\"/><MiningField name=\"var1\" usageType=\"active\"/><MiningField name=\"var2\" usageType=\"active\"/></MiningSchema><Node id=\"1\"><True/><Node id=\"2\"><SimplePredicate field=\"var0\" operator=\"lessOrEqual\" value=\"0.5\"/><Node id=\"4\" score=\"1\"><SimplePredicate field=\"var2\" operator=\"lessOrEqual\" value=\"0.5\"/></Node><Node id=\"5\" score=\"0\"><SimplePredicate field=\"var2\" operator=\"greaterThan\" value=\"0.5\"/></Node></Node><Node id=\"3\"><SimplePredicate field=\"var0\" operator=\"greaterThan\" value=\"0.5\"/><Node id=\"6\" score=\"0\"><SimplePredicate field=\"var1\" operator=\"lessOrEqual\" value=\"0.5\"/></Node><Node id=\"7\" score=\"1\"><SimplePredicate field=\"var1\" operator=\"greaterThan\" value=\"0.5\"/></Node></Node></Node></TreeModel></Segment><Segment id=\"2\"><True/><TreeModel modelName=\"randomForest_Model\" functionName=\"classification\" algorithmName=\"randomForest\" splitCharacteristic=\"binarySplit\"><MiningSchema><MiningField name=\"label\" usageType=\"predicted\"/><MiningField name=\"var0\" usageType=\"active\"/><MiningField name=\"var1\" usageType=\"active\"/><MiningField name=\"var2\" usageType=\"active\"/></MiningSchema><Node id=\"1\"><True/><Node id=\"2\" score=\"0\"><SimplePredicate field=\"var1\" operator=\"lessOrEqual\" value=\"0.5\"/></Node><Node id=\"3\" score=\"1\"><SimplePredicate field=\"var1\" operator=\"greaterThan\" value=\"0.5\"/></Node></Node></TreeModel></Segment></Segmentation></MiningModel></PMML>";
  protected String data_text = "label\tvar0\tvar1\tvar2\torder_id\tpredicted\n1\t0\t1\t0\t6f8e1014\t1\n0\t0\t0\t1\t6f8ea22e\t0\n1\t0\t1\t0\t6f8ea435\t1\n0\t0\t0\t1\t6f8ea5e1\t0\n1\t0\t1\t0\t6f8ea785\t1\n";
  }
