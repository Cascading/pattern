/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
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

package pattern;

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
import cascading.scheme.Scheme;
import cascading.scheme.hadoop.TextDelimited;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import java.util.Properties;


public class
  Main
  {
  /**
   *
   * @param args
   */
  public static void
  main( String[] args )
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

    // build the classifier model from PMML
    Classifier model = null;

    try {
      model = ClassifierFactory.getClassifier( pmmlPath );
    } catch ( PatternException e ) {
      e.printStackTrace();
      System.exit( -1 );
    }

    // define a "Classifier" to evaluate the orders
    Pipe classifyPipe = new Pipe( "classify" );
    classifyPipe = new Each( classifyPipe, Fields.ALL, new ClassifierFunction( new Fields( "score" ), model ), Fields.ALL );

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
