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

import java.io.Serializable;

import cascading.pattern.model.tree.predicate.Predicate;

public class Node implements Serializable
  {
  String id;
  Predicate predicate = null;
  String score = null;

  public Node( String id, Predicate predicate )
    {
    this( id );

    this.predicate = predicate;
    }

  public Node( String id, Predicate predicate, String score )
    {
    this( id );
    this.predicate = predicate;
    this.score = score;
    }

  public Node( String id )
    {
    if( id == null )
      throw new IllegalArgumentException( "id may not be null" );

    this.id = id;
    }

  public String getID()
    {
    return id;
    }

  public Predicate getPredicate()
    {
    return predicate;
    }

  /** @param score evaluated model score */
  public void setScore( String score )
    {
    this.score = score;
    }

  /** @return String */
  public String getScore()
    {
    return score;
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "Node{" );
    sb.append( "id='" ).append( id ).append( '\'' );
    sb.append( ", predicate=" ).append( predicate );
    sb.append( ", score='" ).append( score ).append( '\'' );
    sb.append( '}' );
    return sb.toString();
    }

  @Override
  public boolean equals( Object object )
    {
    if( this == object )
      return true;

    if( object == null || getClass() != object.getClass() )
      return false;

    Node node = (Node) object;

    if( !id.equals( node.id ) )
      return false;

    return true;
    }

  @Override
  public int hashCode()
    {
    return id.hashCode();
    }
  }
