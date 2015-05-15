/*
 * Copyright (c) 2007-2015 Concurrent, Inc. All Rights Reserved.
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

package cascading.pattern.ensemble;

import java.io.IOException;
import java.util.List;

import cascading.flow.Flow;
import cascading.flow.FlowDef;
import cascading.pattern.PatternPlatformTestCase;
import cascading.pattern.model.tree.TreeSpec;
import cascading.pipe.Pipe;
import cascading.pipe.assembly.Discard;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class EnsemblePlatformTestCase extends PatternPlatformTestCase
  {
  public static final String DATA_PATH = System.getProperty( "test.data.path", "../pattern-core/src/test/resources/data/" );
  private static final Logger LOG = LoggerFactory.getLogger( SimpleEnsemblePlatformTest.class );
  private String resultPath;

  protected String getResultPath()
    {
    if( resultPath == null )
      resultPath = getOutputPath( getTestName() );

    return resultPath;
    }

  protected String getFlowPlanPath()
    {
    return getRootPath() + "/dot/" + getTestName();
    }

  @Override
  public void setUp() throws Exception
    {
    super.setUp();

    enableLogging( "cascading.pattern", "debug" );
    }

  protected void performTest( String inputData, Fields predictedFields, Fields expectedFields, EnsembleSpec<TreeSpec> ensembleSpec ) throws IOException
    {
    Pipe pipe = new Pipe( "head" );
    pipe = new Discard( pipe, predictedFields );
    pipe = new ParallelEnsembleAssembly( pipe, ensembleSpec );
    pipe = new Pipe( "tail", pipe );

    Tap source = getPlatform().getDelimitedFile( expectedFields.append( predictedFields ), true, ",", "\"", DATA_PATH + inputData, SinkMode.KEEP );
    Tap sink = getPlatform().getDelimitedFile( Fields.ALL, true, ",", "\"", getResultPath(), SinkMode.REPLACE );

    FlowDef flowDef = FlowDef.flowDef()
      .addSource( "head", source )
      .addSink( "tail", sink )
      .addTail( pipe );

    Flow flow = getPlatform().getFlowConnector().connect( flowDef );

    flow.writeDOT( getFlowPlanPath() + "/plan.dot" );

    flow.complete();

    Fields sourceSelector = source.getSourceFields();
    Fields sinkSelector = sink.getSinkFields();

    LOG.debug( "source select = {}", sourceSelector.printVerbose() );
    LOG.debug( "sink select   = {}", sinkSelector.printVerbose() );

    List<Tuple> sourceTuples = asList( flow, source, sourceSelector );
    List<Tuple> sinkTuples = asList( flow, sink, sinkSelector );

    assertEquals( sourceTuples, sinkTuples, 0.000001d );
    }
  }
