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

import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.Spec;


/**
 * Class TreeSpec is used to define a decision tree model. It simply holds a {@link Tree} instance
 * populated with {@link cascading.pattern.model.tree.predicate.Predicate} instances.
 */
public class TreeSpec extends Spec
  {
  public Tree tree;

  public TreeSpec( ModelSchema modelSchema )
    {
    super( modelSchema );
    }

  public TreeSpec( ModelSchema modelSchema, Tree tree )
    {
    super( modelSchema );
    this.tree = tree;
    }

  public Tree getTree()
    {
    return tree;
    }

  public void setTree( Tree tree )
    {
    this.tree = tree;
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "TreeSpec{" );
    sb.append( "tree=" ).append( tree );
    sb.append( '}' );
    return sb.toString();
    }
  }
