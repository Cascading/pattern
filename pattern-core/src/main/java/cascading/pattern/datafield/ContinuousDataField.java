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

package cascading.pattern.datafield;

import java.lang.reflect.Type;

import cascading.pattern.PatternException;
import cascading.tuple.Tuple;
import cascading.tuple.coerce.Coercions;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.SimplePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContinuousDataField extends DataField
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( ContinuousDataField.class );

  /**
   * @param name
   * @param dataType
   */
  public ContinuousDataField( String name, Type dataType )
    {
    this.name = name;
    this.dataType = dataType;
    }

  @Override
  public String getExpression( Predicate predicate )
    {
    String operator = ( (SimplePredicate) predicate ).getOperator().value();
    String value = ( (SimplePredicate) predicate ).getValue();

    String eval;

    if( operator.equals( "equal" ) )
      eval = name + " == " + value;
    else if( operator.equals( "notEqual" ) )
      eval = name + " != " + value;
    else if( operator.equals( "lessThan" ) )
      eval = name + " < " + value;
    else if( operator.equals( "lessOrEqual" ) )
      eval = name + " <= " + value;
    else if( operator.equals( "greaterThan" ) )
      eval = name + " > " + value;
    else if( operator.equals( "greaterOrEqual" ) )
      eval = name + " >= " + value;
    else
      throw new PatternException( "unknown operator: " + operator );

    return eval;

    }

  /** @return Class */
  public Class getClassType()
    {
    return (Class) dataType;
    }

  /**
   * @param values
   * @param i
   * @return Object
   * @throws PatternException
   */
  public Object getValue( Tuple values, int i ) throws PatternException
    {
    try
      {
      return Coercions.coerce( values.getObject( i ), dataType );
      }
    catch( NumberFormatException exception )
      {
      LOG.error( "tuple format is bad", exception );
      throw new PatternException( "tuple format is bad", exception );
      }
    }
  }
