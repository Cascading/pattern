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

package cascading.pattern.pmml.iris;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import cascading.flow.Flow;
import cascading.flow.FlowDef;
import cascading.flow.local.LocalFlowConnector;
import cascading.pattern.pmml.PMMLPlanner;
import cascading.scheme.local.TextDelimited;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.local.FileTap;
import cascading.tuple.TupleEntryIterator;

/**
 *
 */
public class RegressionFlowExample
  {
  public static void main( String[] args ) throws Exception
    {
    new RegressionFlowExample().run();
    }

  public void run() throws IOException
    {
    Tap<?, ?, ?> irisTap = new FileTap( new TextDelimited( true, "\t", "\"" ), "data/iris.lm_p.tsv", SinkMode.KEEP );

    Tap<Properties, ?, ?> resultsTap = new FileTap( new TextDelimited( true, "\t", "\"" ), "build/test/output/flow/results.tsv", SinkMode.REPLACE );

    FlowDef flowDef = FlowDef.flowDef()
      .setName( "pmml flow" )
      .addSource( "iris", irisTap )
      .addSink( "results", resultsTap );

    PMMLPlanner pmmlPlanner = new PMMLPlanner()
      .setPMMLInput( new File( "data/iris.lm_p.xml" ) )
      .retainOnlyActiveIncomingFields();

    flowDef.addAssemblyPlanner( pmmlPlanner );

    @SuppressWarnings( "unchecked" )
	Flow<Properties> flow = new LocalFlowConnector().connect( flowDef );

    flow.complete();

    TupleEntryIterator iterator = resultsTap.openForRead( flow.getFlowProcess() );

    while( iterator.hasNext() )
      System.out.println( iterator.next() );

    iterator.close();
    }
  }
