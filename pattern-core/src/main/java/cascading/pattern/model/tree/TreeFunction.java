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

package cascading.pattern.model.tree;

import cascading.flow.FlowProcess;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.pattern.model.ModelScoringFunction;
import cascading.pattern.model.tree.decision.DecisionTree;
import cascading.pattern.model.tree.decision.FinalDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class TreeFunction extends ModelScoringFunction<TreeSpec, DecisionTree>
  {
  private static final Logger LOG = LoggerFactory.getLogger( TreeFunction.class );

  public TreeFunction( TreeSpec treeParam )
    {
    super( treeParam );
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<Context<DecisionTree>> operationCall )
    {
    super.prepare( flowProcess, operationCall );

    operationCall.getContext().payload = getSpec().getTree().createDecisionTree( operationCall.getArgumentFields() );
    }

  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<Context<DecisionTree>> functionCall )
    {
    DecisionTree decisionTree = functionCall.getContext().payload;

    FinalDecision finalDecision = decisionTree.decide( functionCall.getArguments() );

    LOG.debug( "final decision: {}", finalDecision );

    functionCall.getOutputCollector().add( functionCall.getContext().result( finalDecision.getCategory() ) );
    }
  }
