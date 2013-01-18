/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import pattern.PatternException;


public class ClassifierSplitFunction extends BaseOperation<ClassifierSplitFunction.Context> implements Function<ClassifierSplitFunction.Context>
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( ClassifierSplitFunction.class );

  public Map<String, Classifier> classifierMap;
  public String splitField;

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
   * @param splitField name of field for experiment ID
   * @param pmmlMap Map of experiment IDs vs. PMML models
   */
  public ClassifierSplitFunction( Fields fieldDeclaration, String splitField, Map<String, String> pmmlMap )
    {
    super( 1, fieldDeclaration );
    this.splitField = splitField;
    classifierMap = new HashMap<String, Classifier>();

    for( Map.Entry<String, String> entry : pmmlMap.entrySet() )
      {
      String splitId = entry.getKey();
      String pmmlPath = entry.getValue();

      classifierMap.put( splitId, new Classifier( pmmlPath ) );
      }
    }

  /**
   * @param flowProcess
   * @param operationCall
   */
  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<ClassifierSplitFunction.Context> operationCall )
    {
    super.prepare( flowProcess, operationCall );
    operationCall.setContext( new ClassifierSplitFunction.Context() );

    for( Classifier classifier : classifierMap.values() )
      classifier.prepare();
    }

  /**
   * @param flowProcess
   * @param functionCall
   */
  @Override
  public void operate( FlowProcess flowProcess, FunctionCall<ClassifierSplitFunction.Context> functionCall )
    {
    TupleEntry argument = functionCall.getArguments();
    String splitId = argument.getString( splitField );
    Classifier classifier = classifierMap.get( splitId );

    if( classifier != null )
      {
      String label = classifier.classifyTuple( argument.getTuple() );
      functionCall.getOutputCollector().add( functionCall.getContext().result( label ) );
      }
    else
      {
      String message = String.format( "unknown experimental split ID [ %s ]", splitId );
      LOG.error( message );
      throw new PatternException( message );
      }
    }

  /**
   * Returns a Fields data structure naming the input tuple fields.
   *
   * @return
   */
  public Fields getInputFields()
    {
    Fields results = new Fields();

    for( Classifier classifier : classifierMap.values() )
      results = Fields.merge( results, classifier.model.schema.getInputFields() );

    return results;
    }
  }
