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

package cascading.pattern.model.randomforest;

import cascading.flow.FlowProcess;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.pattern.model.ClassifierFunction;
import cascading.pattern.model.MiningParam;
import cascading.pattern.model.Param;
import cascading.pattern.model.tree.TreeParam;
import cascading.tuple.Tuple;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RandomForestFunction extends ClassifierFunction<MiningParam>
  {
  private static final Logger LOG = LoggerFactory.getLogger( RandomForestFunction.class );

  public RandomForestFunction( MiningParam param )
    {
    super( param );
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<Context> operationCall )
    {
    super.prepare( flowProcess, operationCall );

    getParam().treeContext.prepare( getParam().getSchemaParam() ); // should be stored in context
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context> functionCall )
    {
    Tuple values = functionCall.getArguments().getTuple();

    Boolean[] predicateEval = getParam().treeContext.evalPredicates( getParam().getSchemaParam(), values );

    // todo: create simpler way to track frequency of labels
    Multiset<String> votes = LinkedHashMultiset.create();

    // tally the vote for each tree in the forest
    for( Param param : getParam().segments )
      {
      String label = ( (TreeParam) param ).tree.traverse( predicateEval );

      LOG.debug( "segment: {}, returned label: {}", param, label );

      votes.add( label );
      }

    // determine the winning label
    int count = 0;
    String label = null;

    for( Multiset.Entry<String> entry : votes.entrySet() )
      {
      if( entry.getCount() < count )
        continue;

      count = entry.getCount();
      label = entry.getElement();
      }

    LOG.debug( "label: {}", label );

    functionCall.getOutputCollector().add( functionCall.getContext().result( label ) );
    }
  }
