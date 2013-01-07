/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.model.tree;

import java.io.Serializable;

public class Vertex implements Serializable
  {
  public String id;
  public String score = null;

  /** @param id vertex ID  */
  public Vertex( String id )
    {
    this.id = id;
    }

  /** @param score evaluated model score  */
  public void setScore( String score )
    {
    this.score = score;
    }

  /** @return String  */
  public String getScore()
    {
    return score;
    }

  /** @return String  */
  @Override
  public String toString()
    {
    if( score != null )
      return id + ":" + score;
    else
      return id;
    }
  }
