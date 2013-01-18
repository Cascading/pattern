/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
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
   * @param fieldDeclaration result field
   * @param pmmlPath PMML file
   */
  public ClassifierFunction( Fields fieldDeclaration, String pmmlPath )
    {
    super( 1, fieldDeclaration );
    this.classifier = new Classifier( pmmlPath );
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

  /**
   * Returns a Fields data structure naming the input tuple fields.
   *
   * @return
   */
  public Fields getInputFields()
    {
    return classifier.model.schema.getInputFields();
    }

  /**
   * Returns a String naming the predictor tuple fields.
   *
   * @return
   */
  public String getPredictor()
    {
    return classifier.model.schema.label_field.name;
    }
  }
