/*
 * Copyright (c) 2007-2015 Concurrent, Inc. All Rights Reserved.
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

package cascading.pattern.model.tree.predicate.compound;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import cascading.pattern.model.tree.predicate.Predicate;

/**
 *
 */
public abstract class CompoundPredicate extends Predicate
  {
  protected Predicate[] children;

  protected CompoundPredicate( List<Predicate> children )
    {
    this.children = children.toArray( new Predicate[ children.size() ] );
    }

  protected CompoundPredicate( Predicate... children )
    {
    this.children = children;
    }

  public Predicate[] getChildren()
    {
    return children;
    }

  public abstract Boolean evaluate( Iterator<Boolean> results );

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder();
    sb.append( getClass().getSimpleName() ).append( "{" );
    sb.append( "children=" ).append( Arrays.toString( children ) );
    sb.append( '}' );
    return sb.toString();
    }
  }
