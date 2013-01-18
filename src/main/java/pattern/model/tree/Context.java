/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern.model.tree;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cascading.tuple.Tuple;
import pattern.PatternException;
import pattern.Schema;
import pattern.XPathReader;


public class Context implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( Context.class );

  public List<String> predicates = new ArrayList<String>();
  public List<ArrayList<Integer>> variables = new ArrayList<ArrayList<Integer>>();

  protected Boolean[] pred_eval;
  protected Object[] param_values;
  protected ExpressionEvaluator[] ee_list;

  /**
   * Make a predicate representing the decision point for a vertext in
   * the tree, plus the indicies for the variables it requires. Return
   * an ID for the predicate.
   *
   * @param schema model schema
   * @param reader XML reader
   * @param node predicate node in the XML
   * @param params parameter names
   * @return Integer
   * @throws PatternException
   */
  public Integer makePredicate( Schema schema, XPathReader reader, Element node, List<String> params ) throws PatternException
    {
    String field = node.getAttribute( "field" );
    String eval = schema.get( field ).getEval( reader, node );
    ArrayList<Integer> pred_vars = new ArrayList<Integer>();
    LOG.debug( "eval: " + eval + " | " + params.toString() );

    for ( String s: eval.split( "[^\\w\\_]" ) )
      {
      s = s.trim();

      if( s.length() > 0 )
        {
        int var_index = params.indexOf( s ) ;

        if( var_index >= 0 )
          pred_vars.add( var_index );
          LOG.debug( "param: " + s + " ? " + var_index + " | " + pred_vars.toString() );
        }
      }

    if( !predicates.contains( eval ) )
      {
      predicates.add( eval );
      variables.add( pred_vars );
      LOG.debug( "pred: " + eval + " ? " + predicates.toString() );
      }

    Integer predicate_id = predicates.indexOf( eval );

    return predicate_id;
    }

  /**
   * Prepare to classify with this model. Called immediately before
   * the enclosing Operation instance is put into play processing
   * Tuples.
   *
   * @param schema model schema
   */
  public void prepare( Schema schema )
    {
    // handle the loop-invariant preparations here,
    // in lieu of incurring overhead for each tuple

    String[] param_names = schema.getParamNames();
    Class[] param_types = schema.getParamTypes();

    ee_list = new ExpressionEvaluator[ predicates.size() ];

    for( int i = 0; i < predicates.size(); i++ )
      try
        {
        ArrayList<Integer> pred_vars = variables.get( i );
        String[] pred_param_names = new String[ pred_vars.size() ];
        Class[] pred_param_types = new Class[ pred_vars.size() ];
        int j = 0;

        for (Integer pv : pred_vars)
          {
          LOG.debug( "pv: " + pv + " name: " + param_names[ pv ] + " type: " + param_types[ pv ] );
          pred_param_names[ j ] = param_names[ pv ];
          pred_param_types[ j++ ] = param_types[ pv ];
          }

        LOG.debug( "eval: " + predicates.get( i ) + " param len: " + pred_vars.size() + " ? " + pred_vars );
        ee_list[ i ] = new ExpressionEvaluator( predicates.get( i ), boolean.class, pred_param_names, pred_param_types, new Class[ 0 ], null );
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

    param_values = new Object[ schema.size() ];
    pred_eval = new Boolean[ predicates.size() ];
    }

  /**
   * Evaluate a tuple of input values to generate an array of
   * predicate values for the tree/forest.
   *
   * @param schema model schema
   * @param values tuple values
   * @return Boolean[]
   * @throws PatternException
   */
  public Boolean[] evalPredicates( Schema schema, Tuple values ) throws PatternException
    {
    schema.setParamValues( values, param_values );

    for( int i = 0; i < predicates.size(); i++ )
      try
        {
        ArrayList<Integer> pred_vars = variables.get( i );
        Object[] pred_param_values = new Object[ pred_vars.size() ];
        int j = 0;

        for (Integer pv : pred_vars)
          {
          LOG.debug( "pv: " + pv + " value: " + param_values[ pv ] );
          pred_param_values[ j++ ] = param_values[ pv ];
          }

        pred_eval[ i ] = new Boolean( ee_list[ i ].evaluate( pred_param_values ).toString() );
        }
      catch( InvocationTargetException exception )
        {
        String message = String.format( "predicate [ %s ] did not evaluate", predicates.get( i ) );
        LOG.error( message, exception );
        throw new PatternException( message, exception );
        }

    return pred_eval;
    }

  /** @return String  */
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
