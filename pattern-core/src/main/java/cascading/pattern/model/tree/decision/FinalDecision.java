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

package cascading.pattern.model.tree.decision;

import java.util.Arrays;

import cascading.pattern.model.tree.Node;
import cascading.pattern.model.tree.Tree;
import cascading.tuple.TupleEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class FinalDecision extends Decision
  {
  private static final Logger LOG = LoggerFactory.getLogger( FinalDecision.class );

  private final Object score;
  private final int index;

  public FinalDecision( Tree tree, Node node )
    {
    this( null, tree, node );
    }

  public FinalDecision( String[] categories, Tree tree, Node node )
    {
    super( tree, node );

    this.score = node.getScore();

    if( this.score == null )
      throw new IllegalStateException( "score may not be null, likely missing leaf node in tree at: " + getName() );

    if( categories != null )
      this.index = Arrays.asList( categories ).indexOf( this.score );
    else
      this.index = -1;
    }

  public Object getScore()
    {
    return score;
    }

  public int getIndex()
    {
    return index;
    }

  @Override
  protected FinalDecision decide( TupleEntry tupleEntry )
    {
    LOG.debug( "decision: {}", name );

    return this;
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "FinalDecision{" );
    sb.append( "name='" ).append( getName() ).append( '\'' );
    sb.append( ",score='" ).append( score ).append( '\'' );
    sb.append( '}' );
    return sb.toString();
    }
  }
