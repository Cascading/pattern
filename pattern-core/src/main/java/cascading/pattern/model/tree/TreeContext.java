/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
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

package cascading.pattern.model.tree;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import cascading.pattern.PatternException;
import cascading.pattern.model.ModelSchema;
import cascading.tuple.Tuple;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TreeContext implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( TreeContext.class );

  public List<String> predicates = new ArrayList<String>();
  public List<ArrayList<Integer>> variables = new ArrayList<ArrayList<Integer>>();

  protected Boolean[] predicateEval;
  protected Object[] paramValues;
  protected ExpressionEvaluator[] expressionEvaluators;

  /**
   * Prepare to classify with this model. Called immediately before
   * the enclosing Operation instance is put into play processing
   * Tuples.
   *
   * @param modelSchema model schema
   */
  public void prepare( ModelSchema modelSchema )
    {
    // handle the loop-invariant preparations here,
    // in lieu of incurring overhead for each tuple

    String[] param_names = modelSchema.getParamNames();
    Class[] param_types = modelSchema.getParamTypes();

    expressionEvaluators = new ExpressionEvaluator[ predicates.size() ];

    for( int i = 0; i < predicates.size(); i++ )
      try
        {
        ArrayList<Integer> pred_vars = variables.get( i );
        String[] pred_param_names = new String[ pred_vars.size() ];
        Class[] pred_param_types = new Class[ pred_vars.size() ];
        int j = 0;

        for( Integer pv : pred_vars )
          {
          LOG.debug( "pv: " + pv + " name: " + param_names[ pv ] + " type: " + param_types[ pv ] );
          pred_param_names[ j ] = param_names[ pv ];
          pred_param_types[ j++ ] = param_types[ pv ];
          }

        LOG.debug( "eval: " + predicates.get( i ) + " param len: " + pred_vars.size() + " ? " + pred_vars );
        expressionEvaluators[ i ] = new ExpressionEvaluator( predicates.get( i ), boolean.class, pred_param_names, pred_param_types, new Class[ 0 ], null );
        }
      catch( NullPointerException exception )
        {
        String message = String.format( "predicate [ %s ] failed", predicates.get( i ) );
        LOG.error( message, exception );
        throw new PatternException( message, exception );
        }
      catch( CompileException exception )
        {
        String message = String.format( "predicate [ %s ] did not compile", predicates.get( i ) );
        LOG.error( message, exception );
        throw new PatternException( message, exception );
        }

    paramValues = new Object[ modelSchema.expectedFields.size() ];
    predicateEval = new Boolean[ predicates.size() ];
    }

  /**
   * Evaluate a tuple of input values to generate an array of
   * predicate values for the tree/forest.
   *
   * @param modelSchema model schema
   * @param values            tuple values
   * @return Boolean[]
   * @throws PatternException
   */
  public Boolean[] evalPredicates( ModelSchema modelSchema, Tuple values ) throws PatternException
    {
    modelSchema.setParamValues( values, paramValues );

    for( int i = 0; i < predicates.size(); i++ )
      try
        {
        ArrayList<Integer> pred_vars = variables.get( i );
        Object[] pred_param_values = new Object[ pred_vars.size() ];
        int j = 0;

        for( Integer pv : pred_vars )
          {
          LOG.debug( "pv: " + pv + " value: " + paramValues[ pv ] );
          pred_param_values[ j++ ] = paramValues[ pv ];
          }

        predicateEval[ i ] = new Boolean( expressionEvaluators[ i ].evaluate( pred_param_values ).toString() );
        }
      catch( InvocationTargetException exception )
        {
        String message = String.format( "predicate [ %s ] did not evaluate", predicates.get( i ) );
        LOG.error( message, exception );
        throw new PatternException( message, exception );
        }

    return predicateEval;
    }

  /** @return String */
  @Override
  public String toString()
    {
    StringBuilder buf = new StringBuilder();

    for( String predicate : predicates )
      {
      buf.append( "expr[ " + predicates.indexOf( predicate ) + " ]: " + predicate );
      buf.append( "\n" );
      }

    return buf.toString();
    }

  }
