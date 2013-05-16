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

package cascading.pattern.ensemble;

import java.io.IOException;

import cascading.pattern.ensemble.selection.Average;
import cascading.pattern.ensemble.selection.MajorityVote;
import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.tree.Tree;
import cascading.pattern.model.tree.TreeSpec;
import cascading.pattern.model.tree.predicate.GreaterThanPredicate;
import cascading.pattern.model.tree.predicate.LessOrEqualThanPredicate;
import cascading.tuple.Fields;
import org.junit.Test;

/**
 *
 */
public class SimpleEnsemblePlatformTest extends EnsemblePlatformTestCase
  {
  @Test
  public void testRandomForest() throws IOException
    {
    Fields predictedFields = new Fields( "label", String.class );

    Fields expectedFields = Fields.NONE
      .append( new Fields( "var0", double.class ) )
      .append( new Fields( "var1", double.class ) )
      .append( new Fields( "var2", double.class ) );

    ModelSchema modelSchema = new ModelSchema( expectedFields, predictedFields );

    modelSchema.setPredictedCategories( "label", "0", "1" );

    EnsembleSpec<TreeSpec> ensembleSpec = new EnsembleSpec<TreeSpec>( modelSchema );

    ensembleSpec.setSelectionStrategy( new MajorityVote() );

    {
    TreeSpec treeSpec = new TreeSpec( modelSchema );

    Tree tree = new Tree( "1" );

    tree.addPredicate( "1", "2", new LessOrEqualThanPredicate( "var0", 0.5d ) );
    tree.addPredicate( "2", "4", new LessOrEqualThanPredicate( "var2", 0.5d ), "1" );
    tree.addPredicate( "2", "5", new GreaterThanPredicate( "var2", 0.5d ), "0" );
    tree.addPredicate( "1", "3", new GreaterThanPredicate( "var0", 0.5d ) );
    tree.addPredicate( "3", "6", new LessOrEqualThanPredicate( "var1", 0.5d ), "0" );
    tree.addPredicate( "3", "7", new GreaterThanPredicate( "var1", 0.5d ), "1" );

    treeSpec.setTree( tree );

    ensembleSpec.addModelSpec( treeSpec );
    }

    {
    TreeSpec treeSpec = new TreeSpec( modelSchema );

    Tree tree = new Tree( "1" );

    tree.addPredicate( "1", "2", new LessOrEqualThanPredicate( "var1", 0.5d ), "1" );
    tree.addPredicate( "1", "3", new GreaterThanPredicate( "var1", 0.5d ), "0" );

    treeSpec.setTree( tree );

    ensembleSpec.addModelSpec( treeSpec );
    }

    {
    TreeSpec treeSpec = new TreeSpec( modelSchema );

    Tree tree = new Tree( "1" );

    tree.addPredicate( "1", "2", new LessOrEqualThanPredicate( "var0", 0.5d ), "1" );
    tree.addPredicate( "1", "3", new GreaterThanPredicate( "var0", 0.5d ), "0" );

    treeSpec.setTree( tree );

    ensembleSpec.addModelSpec( treeSpec );
    }

    String inputData = "randomforest.tsv";

    performTest( inputData, predictedFields, expectedFields, ensembleSpec );
    }

  @Test
  public void testRandomForestPredictive() throws IOException
    {
    Fields predictedFields = new Fields( "label", double.class );

    Fields expectedFields = Fields.NONE
      .append( new Fields( "var0", double.class ) )
      .append( new Fields( "var1", double.class ) )
      .append( new Fields( "var2", double.class ) );

    ModelSchema modelSchema = new ModelSchema( expectedFields, predictedFields );

    EnsembleSpec<TreeSpec> ensembleSpec = new EnsembleSpec<TreeSpec>( modelSchema );

    ensembleSpec.setSelectionStrategy( new Average() );

    {
    TreeSpec treeSpec = new TreeSpec( modelSchema );

    Tree tree = new Tree( "1" );

    tree.addPredicate( "1", "2", new LessOrEqualThanPredicate( "var0", 0.5d ) );
    tree.addPredicate( "2", "4", new LessOrEqualThanPredicate( "var2", 0.5d ), 1 );
    tree.addPredicate( "2", "5", new GreaterThanPredicate( "var2", 0.5d ), 0 );
    tree.addPredicate( "1", "3", new GreaterThanPredicate( "var0", 0.5d ) );
    tree.addPredicate( "3", "6", new LessOrEqualThanPredicate( "var1", 0.5d ), 0 );
    tree.addPredicate( "3", "7", new GreaterThanPredicate( "var1", 0.5d ), 1 );

    treeSpec.setTree( tree );

    ensembleSpec.addModelSpec( treeSpec );
    }

    {
    TreeSpec treeSpec = new TreeSpec( modelSchema );

    Tree tree = new Tree( "1" );

    tree.addPredicate( "1", "2", new LessOrEqualThanPredicate( "var1", 0.5d ), 1 );
    tree.addPredicate( "1", "3", new GreaterThanPredicate( "var1", 0.5d ), 0 );

    treeSpec.setTree( tree );

    ensembleSpec.addModelSpec( treeSpec );
    }

    {
    TreeSpec treeSpec = new TreeSpec( modelSchema );

    Tree tree = new Tree( "1" );

    tree.addPredicate( "1", "2", new LessOrEqualThanPredicate( "var0", 0.5d ), 1 );
    tree.addPredicate( "1", "3", new GreaterThanPredicate( "var0", 0.5d ), 0 );

    treeSpec.setTree( tree );

    ensembleSpec.addModelSpec( treeSpec );
    }

    String inputData = "randomforest-predict.tsv";

    performTest( inputData, predictedFields, expectedFields, ensembleSpec );
    }
  }
