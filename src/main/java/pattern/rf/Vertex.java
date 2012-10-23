/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
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

package pattern.rf;

import java.io.Serializable;


public class Vertex implements Serializable
  {
  public String id;
  public String score = null;


  /**
   *
   * @param id
   */
  public Vertex( String id )
    {
    this.id = id;
    }


  /**
   *
   * @param score
   */
  public void setScore( String score )
    {
    this.score = score;
    }


  /**
   *
   * @return
   */
  public String getScore()
    {
    return score;
    }


  /**
   *
   * @return
   */
  public String toString()
    {
    if( score != null )
      {
      return id + ":" + score;
      }
    else
      {
      return id;
      }
    }
  }
