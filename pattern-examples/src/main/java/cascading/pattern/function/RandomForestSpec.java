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

package cascading.pattern.function;

import java.util.ArrayList;
import java.util.List;

import cascading.pattern.ensemble.EnsembleSpec;
import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.tree.Tree;
import cascading.pattern.model.tree.TreeSpec;
import cascading.pattern.model.tree.decision.DecisionTree;
import cascading.tuple.Fields;

/**
 *
 */
public class RandomForestSpec extends EnsembleSpec<TreeSpec>
  {
  public RandomForestSpec( ModelSchema modelSchema )
    {
    super( modelSchema );
    }

  public RandomForestSpec( ModelSchema modelSchema, List<TreeSpec> treeSpecs )
    {
    super( modelSchema, treeSpecs );
    }

  public void addTreeSpec( TreeSpec treeSpec )
    {
    addModelSpec( treeSpec );
    }

  public List<Tree> getTrees()
    {
    List<Tree> trees = new ArrayList<Tree>();

    for( TreeSpec treeSpec : getModelSpecs() )
      trees.add( treeSpec.getTree() );

    return trees;
    }

  public DecisionTree[] getDecisionTrees( String[] categories, Fields argumentFields )
    {
    List<Tree> trees = getTrees();
    DecisionTree[] decisionTrees = new DecisionTree[ trees.size() ];

    for( int i = 0; i < trees.size(); i++ )
      decisionTrees[ i ] = trees.get( i ).createDecisionTree( categories, argumentFields );

    return decisionTrees;
    }
  }
