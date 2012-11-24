/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
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
  protected Boolean[] pred_eval;

  protected Object[] param_values;
  protected ExpressionEvaluator[] ee_list;

  /**
   * Make a predicate representing the decision point for a vertext in
   * the tree. Return an ID for the predicate.
   *
   * @param schema model schema
   * @param reader XML reader
   * @param node predicate node in the XML
   * @return Integer
   * @throws PatternException
   */
  public Integer makePredicate( Schema schema, XPathReader reader, Element node ) throws PatternException
    {
    String field = node.getAttribute( "field" );
    String eval = schema.get( field ).getEval( reader, node );

    if( !predicates.contains( eval ) )
      predicates.add( eval );

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
          LOG.debug( "eval: " + predicates.get( i ) );
          ee_list[ i ] = new ExpressionEvaluator( predicates.get( i ), boolean.class, param_names, param_types, new Class[ 0 ], null );
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
        pred_eval[ i ] = new Boolean( ee_list[ i ].evaluate( param_values ).toString() );
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
