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
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;


public class ClassifierFunction extends BaseOperation<ClassifierFunction.Context> implements Function<ClassifierFunction.Context>
  {
  public Classifier classifier;

  /** Class Context is used to hold intermediate values. */
  protected static class Context
    {
    Tuple tuple = Tuple.size( 1 );

    public Tuple result( String label )
      {
      tuple.set( 0, label );

      return tuple;
      }
    }

  /**
   * @param fieldDeclaration
   * @param classifier
   */
  public ClassifierFunction( Fields fieldDeclaration, Classifier classifier )
    {
    super( 1, fieldDeclaration );
    this.classifier = classifier;
    }

  /**
   * @param flowProcess
   * @param operationCall
   */
  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<ClassifierFunction.Context> operationCall )
    {
    super.prepare( flowProcess, operationCall );
    operationCall.setContext( new ClassifierFunction.Context() );
    classifier.prepare();
    }

  /**
   * @param flowProcess
   * @param functionCall
   */
  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<ClassifierFunction.Context> functionCall )
    {
    TupleEntry argument = functionCall.getArguments();
    String label = classifier.classifyTuple( argument.getTuple() );

    functionCall.getOutputCollector().add( functionCall.getContext().result( label ) );
    }
  }
