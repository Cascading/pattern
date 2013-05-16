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

package cascading.pattern.ensemble.selection;

import java.util.Arrays;
import java.util.Iterator;

import cascading.flow.FlowProcess;
import cascading.operation.BufferCall;
import cascading.operation.OperationCall;
import cascading.pattern.ensemble.EnsembleSpec;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class PredictionSelectionBuffer extends SelectionBuffer<PredictionSelectionBuffer.DecisionContext>
  {
  private static final Logger LOG = LoggerFactory.getLogger( PredictionSelectionBuffer.class );

  private PredictionSelector selection;

  protected class DecisionContext
    {
    public Tuple tuple;
    public double[] results;

    public Tuple result( Object value )
      {
      tuple.set( 0, value );

      return tuple;
      }
    }

  public PredictionSelectionBuffer( EnsembleSpec ensembleSpec )
    {
    super( ensembleSpec.getModelSchema().getDeclaredFields(), ensembleSpec );

    this.selection = (PredictionSelector) ensembleSpec.getSelectionStrategy();
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<DecisionContext> operationCall )
    {
    ( (BufferCall) operationCall ).setRetainValues( true );

    DecisionContext context = new DecisionContext();

    context.tuple = Tuple.size( getFieldDeclaration().size() );
    context.results = new double[ ensembleSpec.getModelSpecs().size() ];

    operationCall.setContext( context );
    }

  @Override
  public void operate( FlowProcess flowProcess, BufferCall<DecisionContext> bufferCall )
    {
    double[] results = bufferCall.getContext().results;

    Arrays.fill( results, 0 ); // clear before use

    Iterator<TupleEntry> iterator = bufferCall.getArgumentsIterator();

    int count = 0;
    while( iterator.hasNext() )
      {
      TupleEntry next = iterator.next();
      Double score = next.getDouble( 0 );

      results[ count++ ] += score;
      }

    double prediction = selection.predict( results );

    LOG.debug( "prediction: {}", prediction );

    bufferCall.getOutputCollector().add( bufferCall.getContext().result( prediction ) );
    }
  }
