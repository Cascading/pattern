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
import cascading.pattern.model.MiningSpec;
import cascading.pattern.model.Spec;
import cascading.pattern.model.tree.TreeSpec;
import cascading.tuple.Tuple;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RandomForestFunction extends ClassifierFunction<MiningSpec, Void>
  {
  private static final Logger LOG = LoggerFactory.getLogger( RandomForestFunction.class );

  public RandomForestFunction( MiningSpec param )
    {
    super( param );
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<Context<Void>> operationCall )
    {
    super.prepare( flowProcess, operationCall );

    getSpec().treeContext.prepare( getSpec().getModelSchema() ); // should be stored in context
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context<Void>> functionCall )
    {
    Tuple values = functionCall.getArguments().getTuple();

    Boolean[] predicateEval = getSpec().treeContext.evalPredicates( getSpec().getModelSchema(), values );

    // todo: create simpler way to track frequency of labels
    Multiset<String> votes = LinkedHashMultiset.create();

    // tally the vote for each tree in the forest
    for( Spec spec : getSpec().segments )
      {
      String label = ( (TreeSpec) spec ).tree.traverse( predicateEval );

      LOG.debug( "segment: {}, returned label: {}", spec, label );

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
