/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.util.Properties;

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
    String measurePath = args[ 3 ];
    String trapPath = args[ 4 ];

    Properties properties = new Properties();
    AppProps.setApplicationJarClass( properties, Main.class );
    HadoopFlowConnector flowConnector = new HadoopFlowConnector( properties );

    // create source and sink taps
    Tap ordersTap = new Hfs( new TextDelimited( true, "\t" ), ordersPath );
    Tap classifyTap = new Hfs( new TextDelimited( true, "\t" ), classifyPath );
    Tap measureTap = new Hfs( new TextDelimited( true, "\t" ), measurePath );
    Tap trapTap = new Hfs( new TextDelimited( true, "\t" ), trapPath );

    // define a "Classifier" model from PMML to evaluate the orders
    Classifier model = ClassifierFactory.getClassifier( pmmlPath );
    Pipe classifyPipe = new Each( new Pipe( "classify" ), model.getFields(), new ClassifierFunction( new Fields( "score" ), model ), Fields.ALL );

    // verify the model results vs. what R predicted
    Pipe verifyPipe = new Pipe( "verify", classifyPipe );
    String expression = "predicted == score";
    ExpressionFunction matchExpression = new ExpressionFunction( new Fields( "match" ), expression, Integer.class );
    verifyPipe = new Each( verifyPipe, Fields.ALL, matchExpression, Fields.ALL );
    verifyPipe = new Each( verifyPipe, DebugLevel.VERBOSE, new Debug( true ) );

    AssertMatches assertMatches = new AssertMatches( ".*true" );
    verifyPipe = new Each( verifyPipe, AssertionLevel.STRICT, assertMatches );

    // calculate a confusion matrix for the model results
    Pipe measurePipe = new Pipe( "measure", verifyPipe );
    measurePipe = new GroupBy( measurePipe, new Fields( "label", "score" ) );
    measurePipe = new Every( measurePipe, Fields.ALL, new Count(), Fields.ALL );

    // connect the taps, pipes, etc., into a flow
    FlowDef flowDef = FlowDef.flowDef()
      .setName( "classify" )
      .addSource( classifyPipe, ordersTap )
      .addSink( classifyPipe, classifyTap )
      .addTrap( classifyPipe, trapTap )
      .addTrap( verifyPipe, trapTap )
      .addTailSink( measurePipe, measureTap );

    // set to DebugLevel.VERBOSE for trace, or DebugLevel.NONE in production
    flowDef.setDebugLevel( DebugLevel.NONE );

    // set to AssertionLevel.STRICT for all assertions, or AssertionLevel.NONE in production
    flowDef.setAssertionLevel( AssertionLevel.STRICT );

    // write a DOT file and run the flow
    Flow classifyFlow = flowConnector.connect( flowDef );
    classifyFlow.writeDOT( "dot/classify.dot" );
    classifyFlow.complete();
    }
  }
