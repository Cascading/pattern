/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.io.Serializable;

import org.w3c.dom.Node;

import cascading.tuple.Tuple;


public abstract class DataField implements Serializable
  {
  public String name;
  public String op_type;
  public String data_type;

  /**
   * Does nothing. May override if a DataField subclass needs to parse additional info from PMML.
   *
   * @param reader
   * @param node
   */
  public void parse( XPathReader reader, Node node )
    {
    }

  /** @return  */
  public abstract Class getClassType();

  /**
   * @return
   * @throws PatternException
   */
  public abstract Object getValue( Tuple values, int i ) throws PatternException;

  /** @return  */
  @Override
  public String toString()
    {
    return name + ":" + op_type + ":" + data_type;
    }
  }
