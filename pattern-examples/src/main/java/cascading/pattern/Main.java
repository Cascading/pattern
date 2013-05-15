/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package cascading.pattern;

import java.io.File;
import java.util.Properties;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cascading.flow.Flow;
import cascading.flow.FlowDef;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.operation.AssertionLevel;
import cascading.operation.Debug;
import cascading.operation.DebugLevel;
import cascading.operation.assertion.AssertMatches;
import cascading.pattern.pmml.PMMLPlanner;
import cascading.property.AppProps;
import cascading.scheme.hadoop.TextDelimited;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;


public class Main
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( Main.class );

  /** @param args  */
  public static void main( String[] args ) throws RuntimeException
    {
    String inputPath = args[ 0 ];
    String classifyPath = args[ 1 ];

    // set up the config properties
    Properties properties = new Properties();
    AppProps.setApplicationJarClass( properties, Main.class );
    HadoopFlowConnector flowConnector = new HadoopFlowConnector( properties );

    // create source and sink taps
    Tap inputTap = new Hfs( new TextDelimited( true, "\t" ), inputPath );
    Tap classifyTap = new Hfs( new TextDelimited( true, "\t" ), classifyPath );

    // handle command line options
    OptionParser optParser = new OptionParser();
    optParser.accepts( "pmml" ).withRequiredArg();
    optParser.accepts( "debug" );
    optParser.accepts( "assert" );

    OptionSet options = optParser.parse( args );

    // connect the taps, pipes, etc., into a flow
    FlowDef flowDef = FlowDef.flowDef()
      .setName( "classify" )
      .addSource( "input", inputTap )
      .addSink( "classify", classifyTap );

    // define a "Classifier" model from the PMML description
    if( options.hasArgument( "pmml" ) )
      {
      String pmmlPath = (String) options.valuesOf( "pmml" ).get( 0 );

      PMMLPlanner pmmlPlanner = new PMMLPlanner()
        .setPMMLInput( new File( pmmlPath ) )
        .retainOnlyActiveIncomingFields();

      flowDef.addAssemblyPlanner( pmmlPlanner );
      }

    // set to DebugLevel.VERBOSE for trace, or DebugLevel.NONE
    // in production
    if( options.has( "debug" ) )
      flowDef.setDebugLevel( DebugLevel.VERBOSE );
    else
      flowDef.setDebugLevel( DebugLevel.NONE );

    // set to AssertionLevel.STRICT for all assertions, or
    // AssertionLevel.NONE in production
    if( options.has( "assert" ) )
      flowDef.setAssertionLevel( AssertionLevel.STRICT );
    else
      flowDef.setAssertionLevel( AssertionLevel.NONE );

    // write a DOT file and run the flow
    Flow classifyFlow = flowConnector.connect( flowDef );
    classifyFlow.writeDOT( "dot/classify.dot" );
    classifyFlow.complete();
    }
  }
