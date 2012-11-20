/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.io.Serializable;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;


public abstract class Classifier implements Serializable
  {
  public Schema schema;

  /**
   * Prepare to classify with this model. Called immediately before
   * the enclosing Operation instance is put into play processing
   * Tuples.
   *
   * @param values
   */
  public abstract void prepare();

  /**
   * Classify an input tuple, returning the predicted label.
   *
   * @param values
   * @return
   * @throws PatternException
   */
  public abstract String classifyTuple( Tuple values ) throws PatternException;

  /**
   * Returns a Fields data structure.
   *
   * @return
   */
  public Fields getFields()
    {
    return schema.getFields();
    }
  }
