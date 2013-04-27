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

import java.util.List;

import cascading.flow.FlowProcess;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.pattern.model.ClassifierFunction;
import cascading.pattern.model.tree.Tree;
import cascading.pattern.model.tree.decision.DecisionTree;
import cascading.pattern.model.tree.decision.FinalDecision;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RandomForestFunction extends ClassifierFunction<RandomForestSpec, DecisionTree[]>
  {
  private static final Logger LOG = LoggerFactory.getLogger( RandomForestFunction.class );

  public RandomForestFunction( RandomForestSpec param )
    {
    super( param );
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<Context<DecisionTree[]>> operationCall )
    {
    super.prepare( flowProcess, operationCall );

    Fields argumentFields = operationCall.getArgumentFields();
    List<Tree> trees = getSpec().getTrees();
    DecisionTree[] decisionTrees = new DecisionTree[ trees.size() ];

    for( int i = 0; i < trees.size(); i++ )
      decisionTrees[ i ] = trees.get( i ).createDecisionTree( argumentFields );

    operationCall.getContext().payload = decisionTrees;
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context<DecisionTree[]>> functionCall )
    {
    TupleEntry arguments = functionCall.getArguments();

    // todo: pluggable voting strategy
    Multiset<String> votes = LinkedHashMultiset.create();

    DecisionTree[] decisionTrees = functionCall.getContext().payload;

    for( int i = 0; i < decisionTrees.length; i++ )
      {
      FinalDecision finalDecision = decisionTrees[ i ].decide( arguments );

      String score = finalDecision.getScore();

      LOG.debug( "segment: {}, returned decision: {}", i, finalDecision );

      votes.add( score );
      }

    // determine the winning score
    int count = 0;
    String score = null;

    for( Multiset.Entry<String> entry : votes.entrySet() )
      {
      if( entry.getCount() < count )
        continue;

      count = entry.getCount();
      score = entry.getElement();
      }

    LOG.debug( "winning score: {}, with votes: {}", score, count );

    functionCall.getOutputCollector().add( functionCall.getContext().result( score ) );
    }
  }
