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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cascading.pattern.ensemble.selection.MajorityVote;
import cascading.pattern.ensemble.selection.SelectionStrategy;
import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.Spec;


public class EnsembleSpec<S extends Spec> extends Spec implements Serializable
  {
  protected List<S> modelSpecs = new ArrayList<S>();
  protected SelectionStrategy selectionStrategy = new MajorityVote();

  public EnsembleSpec( ModelSchema modelSchema )
    {
    super( modelSchema );
    }

  public EnsembleSpec( ModelSchema modelSchema, List<S> modelSpecs )
    {
    super( modelSchema );
    this.modelSpecs = modelSpecs;
    }

  public boolean isParallel()
    {
    return selectionStrategy.isParallel();
    }

  public void addModelSpecs( List<S> modelSpec )
    {
    this.modelSpecs.addAll( modelSpec );
    }

  public void addModelSpec( S modelSpec )
    {
    this.modelSpecs.add( modelSpec );
    }

  public List<S> getModelSpecs()
    {
    return modelSpecs;
    }

  public SelectionStrategy getSelectionStrategy()
    {
    return selectionStrategy;
    }

  public void setSelectionStrategy( SelectionStrategy selectionStrategy )
    {
    this.selectionStrategy = selectionStrategy;
    }

  public List<String> getModelCategories()
    {
    List<String> categories = new ArrayList<String>();

    Set<String> set = new LinkedHashSet<String>();

    for( S spec : getModelSpecs() )
      set.addAll( spec.getCategories() );

    categories.addAll( set );

    return categories;
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "EnsembleSpec{" );
    sb.append( "modelSpecs=" ).append( modelSpecs );
    sb.append( '}' );
    return sb.toString();
    }
  }
