/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
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

package cascading.pattern;

import java.io.File;
import java.util.Properties;

import cascading.flow.Flow;
import cascading.flow.FlowDef;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.pattern.pmml.PMMLPlanner;
import cascading.property.AppProps;
import cascading.scheme.hadoop.TextDelimited;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import joptsimple.OptionParser;
import joptsimple.OptionSet;


public class Main
  {
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

    OptionSet options = optParser.parse( args );

    // connect the taps, pipes, etc., into a flow
    FlowDef flowDef = FlowDef.flowDef()
      .setName( "classify" )
      .addSource( "input", inputTap )
      .addSink( "classify", classifyTap );

    // build a Cascading assembly from the PMML description
    if( options.hasArgument( "pmml" ) )
      {
      String pmmlPath = (String) options.valuesOf( "pmml" ).get( 0 );

      PMMLPlanner pmmlPlanner = new PMMLPlanner()
        .setPMMLInput( new File( pmmlPath ) )
        .retainOnlyActiveIncomingFields()
        .setDefaultPredictedField( new Fields( "predict", Double.class ) ); // default value if missing from the model

      flowDef.addAssemblyPlanner( pmmlPlanner );
      }

    // write a DOT file and run the flow
    Flow classifyFlow = flowConnector.connect( flowDef );
    classifyFlow.writeDOT( "dot/classify.dot" );
    classifyFlow.complete();
    }
  }
