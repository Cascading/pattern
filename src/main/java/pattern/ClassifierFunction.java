/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;


public class ClassifierFunction extends BaseOperation implements Function
  {
  public Classifier model;

  /**
   * @param fieldDeclaration
   * @param model
   */
  public ClassifierFunction( Fields fieldDeclaration, Classifier model )
    {
    super( 1, fieldDeclaration );
    this.model = model;
    }

  /**
   * @param flowProcess
   * @param functionCall
   */
  public void operate( FlowProcess flowProcess, FunctionCall functionCall )
    {
    TupleEntry argument = functionCall.getArguments();
    String label = model.classifyTuple( argument.getTuple() );

    functionCall.getOutputCollector().add( new Tuple( label ) );
    }
  }

