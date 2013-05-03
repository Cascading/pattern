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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.Spec;


public class TreeSpec extends Spec
  {
  public Tree tree;

  public TreeSpec( ModelSchema modelSchema )
    {
    super( modelSchema );
    }

  public TreeSpec( Tree tree )
    {
    super( null );
    this.tree = tree;
    }

  public TreeSpec( ModelSchema schemaParam, Tree tree )
    {
    super( schemaParam );
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

  public List<String> getNodeCategories()
    {
    List<String> categories = new ArrayList<String>();

    Set<String> set = new LinkedHashSet<String>();

    for( Node node : tree.getGraph().vertexSet() )
      {
      if( node.getCategory() != null )
        set.add( node.getCategory() );
      }

    categories.addAll( set );

    return categories;
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
