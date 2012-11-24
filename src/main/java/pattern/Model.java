/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.io.Serializable;

import cascading.tuple.Tuple;
import pattern.Schema;


public abstract class Model implements Serializable
  {
  public Schema schema;

  /**
   * Prepare to classify with this model. Called immediately before
   * the enclosing Operation instance is put into play processing
   * Tuples.
   */
  public abstract void prepare();

  /**
   * Classify an input tuple, returning the predicted label.
   *
   * @param values tuple values
   * @return String
   * @throws PatternException
   */
  public abstract String classifyTuple( Tuple values ) throws PatternException;
  }
