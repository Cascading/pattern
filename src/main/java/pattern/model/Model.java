/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.model;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import pattern.PatternException;
import pattern.Schema;

import java.io.Serializable;


public abstract class Model implements Serializable
  {
  public Schema schema = null;

  /**
   * Prepare to classify with this model. Called immediately before
   * the enclosing Operation instance is put into play processing
   * Tuples.
   */
  public abstract void prepare();

  /**
   * Classify an input tuple, returning the predicted label.
   *
   *
   * @param values tuple values
   * @param fields
   * @return String
   * @throws PatternException
   */
  public abstract String classifyTuple(Tuple values, Fields fields) throws PatternException;
  }
