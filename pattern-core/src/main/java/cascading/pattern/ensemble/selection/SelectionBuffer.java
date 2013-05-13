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
import cascading.operation.BaseOperation;
import cascading.operation.Buffer;
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
public class SelectionBuffer extends BaseOperation<SelectionBuffer.DecisionContext> implements Buffer<SelectionBuffer.DecisionContext>
  {
  private static final Logger LOG = LoggerFactory.getLogger( SelectionBuffer.class );

  private final EnsembleSpec ensembleSpec;
  private final String[] categories;

  protected class DecisionContext
    {
    public Tuple tuple;
    public String[] categories;
    public int[] results;

    public Tuple result( Object value )
      {
      tuple.set( 0, value );

      return tuple;
      }
    }

  public SelectionBuffer( EnsembleSpec ensembleSpec )
    {
    super( ensembleSpec.getModelSchema().getDeclaredFields() );
    this.ensembleSpec = ensembleSpec;
    this.categories = (String[]) ensembleSpec.getCategories().toArray( new String[ ensembleSpec.getCategories().size() ] );
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<DecisionContext> operationCall )
    {
    ( (BufferCall) operationCall ).setRetainValues( true );

    DecisionContext context = new DecisionContext();

    context.tuple = Tuple.size( getFieldDeclaration().size() );
    context.categories = this.categories;
    context.results = new int[ categories.length ];

    operationCall.setContext( context );
    }

  @Override
  public void operate( FlowProcess flowProcess, BufferCall<DecisionContext> bufferCall )
    {
    int[] results = bufferCall.getContext().results;

    Arrays.fill( results, 0 ); // clear before use

    Iterator<TupleEntry> iterator = bufferCall.getArgumentsIterator();

    while( iterator.hasNext() )
      {
      TupleEntry next = iterator.next();
      Integer category = (Integer) next.getObject( 0 );

      results[ category ] += 1;
      }

    int index = ensembleSpec.getSelectionStrategy().select( results );

    String category = categories[ index ];

    LOG.debug( "winning category: {}", category );

    if( !ensembleSpec.getModelSchema().isIncludePredictedCategories() )
      {
      bufferCall.getOutputCollector().add( bufferCall.getContext().result( category ) );
      return;
      }

    Tuple result = bufferCall.getContext().tuple;

    result.set( 0, category );

    for( int i = 0; i < results.length; i++ )
      result.set( i + 1, results[ i ] );

    bufferCall.getOutputCollector().add( result );
    }
  }
