/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.io.Serializable;


public class DataField implements Serializable
  {
  public String name;
  public String type;

  /**
   * @param name
   * @param type
   */
  public DataField( String name, String type )
    {
    this.name = name;
    this.type = type;
    }

  /** @return  */
  public String toString()
    {
    return name + ":" + type;
    }
  }
