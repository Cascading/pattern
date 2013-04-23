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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cascading.pattern.PatternException;
import cascading.tuple.Tuple;
import org.dmg.pmml.ArrayType;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.SimpleSetPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CategoricalDataField extends DataField
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( CategoricalDataField.class );

  public List<String> categories = new ArrayList<String>();

  public CategoricalDataField( String name, Type dataType, String... categories )
    {
    this( name, dataType, Arrays.asList( categories ) );
    }

  public CategoricalDataField( String name, Type dataType, List<String> categories )
    {
    this.name = name;
    this.dataType = dataType;
    this.categories.addAll( categories );
    }

  @Override
  public String getExpression( Predicate predicate )
    {
    SimpleSetPredicate simpleSetPredicate = (SimpleSetPredicate) predicate;
    String operator = simpleSetPredicate.getBooleanOperator().value();
    ArrayType array = simpleSetPredicate.getArray();

    PortableBitSet bits = new PortableBitSet( categories.size() );
    String value = array.getContent();

    value = value.substring( 1, value.length() - 1 );

    for( String s : value.split( "\\\"\\s+\\\"" ) )
      bits.set( categories.indexOf( s ) );

    if( operator.equals( "isIn" ) )
      return String.format( "pattern.datafield.PortableBitSet.isIn( \"%s\", %s )", bits.toString(), name );

    throw new PatternException( "unknown operator: " + operator );
    }

  /** @return Class */
  public Class getClassType()
    {
    return int.class;
    }

  /**
   * @param values
   * @param i
   * @return Object
   */
  public Object getValue( Tuple values, int i )
    {
    String field_value = values.getString( i );
    int index = categories.indexOf( field_value );

    if( LOG.isDebugEnabled() )
      LOG.debug( String.format( "%s @ %d | %s", field_value, index, categories ) );

    return index;
    }
  }
