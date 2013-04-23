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

package cascading.pattern.pmml.tree;

import java.util.ArrayList;
import java.util.List;

import cascading.pattern.PatternException;
import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.tree.TreeContext;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Predicates
  {
  private static final Logger LOG = LoggerFactory.getLogger( Predicates.class );

  public static Integer makePredicate( TreeContext treeContext, ModelSchema modelSchema, Predicate predicate, List<String> params )
    {
    String field;

    if( predicate instanceof SimplePredicate )
      field = ( (SimplePredicate) predicate ).getField().getValue();
    else if( predicate instanceof SimpleSetPredicate )
      field = ( (SimpleSetPredicate) predicate ).getField().getValue();
    else
      throw new PatternException( "unknown predicate type: " + predicate.getClass().getName() );

    String expression = modelSchema.getExpectedField( field ).getExpression( predicate );

    ArrayList<Integer> predicateVars = new ArrayList<Integer>();

    LOG.debug( "expression: " + expression + " | " + params.toString() );

    for( String s : expression.split( "[^\\w\\_]" ) )
      {
      s = s.trim();

      if( s.length() > 0 )
        {
        int index = params.indexOf( s );

        if( index >= 0 )
          predicateVars.add( index );

        LOG.debug( "param: " + s + " ? " + index + " | " + predicateVars.toString() );
        }
      }

    if( !treeContext.predicates.contains( expression ) )
      {
      treeContext.predicates.add( expression );
      treeContext.variables.add( predicateVars );

      LOG.debug( "pred: " + expression + " ? " + treeContext.predicates.toString() );
      }

    return treeContext.predicates.indexOf( expression );
    }
  }
