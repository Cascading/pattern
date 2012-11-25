/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.util.Properties;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import cascading.flow.Flow;
import cascading.flow.FlowDef;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.operation.AssertionLevel;
import cascading.operation.Debug;
import cascading.operation.DebugLevel;
import cascading.operation.aggregator.Count;
import cascading.operation.assertion.AssertMatches;
import cascading.operation.expression.ExpressionFunction;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.property.AppProps;
import cascading.scheme.hadoop.TextDelimited;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;


public class Main
  {
  /** @param args  */
  public static void main( String[] args ) throws RuntimeException
    {
    String pmmlPath = args[ 0 ];
    String ordersPath = args[ 1 ];
    String classifyPath = args[ 2 ];
    String trapPath = args[ 3 ];

    // set up the config properties
    Properties properties = new Properties();
    AppProps.setApplicationJarClass( properties, Main.class );
    HadoopFlowConnector flowConnector = new HadoopFlowConnector( properties );

    // create source and sink taps
    Tap ordersTap = new Hfs( new TextDelimited( true, "\t" ), ordersPath );
    Tap classifyTap = new Hfs( new TextDelimited( true, "\t" ), classifyPath );
    Tap trapTap = new Hfs( new TextDelimited( true, "\t" ), trapPath );
    Tap measureTap = null;

    // handle command line options
    OptionParser optParser = new OptionParser();
    optParser.accepts( "measure" ).withRequiredArg();
    optParser.accepts( "debug" );
    optParser.accepts( "assert" );

    OptionSet options = optParser.parse( args );

    // define a "Classifier" model from the PMML description
    Classifier classifier = new Classifier( pmmlPath );
    ClassifierFunction classFunc = new ClassifierFunction( new Fields( "score" ), classifier );
    Pipe classifyPipe = new Each( new Pipe( "classify" ), classifier.getFields(), classFunc, Fields.ALL );

    // optionally: measure model results vs. what was predicted during model creation
    Pipe measurePipe = null;
    Pipe verifyPipe = null;

    if( options.hasArgument( "measure" ) )
      {
      String measurePath = (String) options.valuesOf( "measure" ).get( 0 );
      measureTap = new Hfs( new TextDelimited( true, "\t" ), measurePath );

      // add a stream assertion which implements a full regression test
      verifyPipe = new Pipe( "verify", classifyPipe );
      String expression = "predict.equals( score )";
      ExpressionFunction matchExpression = new ExpressionFunction( new Fields( "match" ), expression, String.class );
      verifyPipe = new Each( verifyPipe, Fields.ALL, matchExpression, Fields.ALL );
      verifyPipe = new Each( verifyPipe, DebugLevel.VERBOSE, new Debug( true ) );

      AssertMatches assertMatches = new AssertMatches( ".*true" );
      verifyPipe = new Each( verifyPipe, AssertionLevel.STRICT, assertMatches );

      // calculate a confusion matrix for the model results
      measurePipe = new Pipe( "measure", verifyPipe );
      measurePipe = new GroupBy( measurePipe, new Fields( "predict", "score" ) );
      measurePipe = new Every( measurePipe, Fields.ALL, new Count(), Fields.ALL );
      }

    // connect the taps, pipes, etc., into a flow
    FlowDef flowDef = FlowDef.flowDef().setName( "classify" )
      .addSource( classifyPipe, ordersTap )
      .addTrap( classifyPipe, trapTap )
      ;

    if( measureTap == null )
      flowDef.addTailSink( classifyPipe, classifyTap );
    else
      {
      flowDef.addSink( classifyPipe, classifyTap )
        .addTrap( verifyPipe, trapTap )
        .addTailSink( measurePipe, measureTap )
        ;
      }

    // set to DebugLevel.VERBOSE for trace, or DebugLevel.NONE in production
    if( options.hasArgument( "debug" ) )
      flowDef.setDebugLevel( DebugLevel.VERBOSE );
    else
      flowDef.setDebugLevel( DebugLevel.NONE );

    // set to AssertionLevel.STRICT for all assertions, or AssertionLevel.NONE in production
    if( options.hasArgument( "assert" ) )
      flowDef.setAssertionLevel( AssertionLevel.STRICT );
    else
      flowDef.setAssertionLevel( AssertionLevel.NONE );

    // write a DOT file and run the flow
    Flow classifyFlow = flowConnector.connect( flowDef );
    classifyFlow.writeDOT( "dot/classify.dot" );
    classifyFlow.complete();
    }
  }
