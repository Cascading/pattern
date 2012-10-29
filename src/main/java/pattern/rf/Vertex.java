/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.rf;

import java.io.Serializable;

public class Vertex implements Serializable
  {
  public String id;
  public String score = null;

  /** @param id  */
  public Vertex( String id )
    {
    this.id = id;
    }

  /** @param score  */
  public void setScore( String score )
    {
    this.score = score;
    }

  /** @return  */
  public String getScore()
    {
    return score;
    }

  /** @return  */
  public String toString()
    {
    if( score != null )
      return id + ":" + score;
    else
      return id;
    }
  }
