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

package cascading.pattern.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import cascading.pattern.PatternException;
import cascading.pattern.datafield.DataField;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MiningSchemaParam extends LinkedHashMap<String, DataField>
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( MiningSchemaParam.class );

  /** Field label_field - metadata of the label to be produced by the classifier */
  public DataField labelField;

  public void setLabelField( DataField labelField )
    {
    this.labelField = labelField;
    }

  public void addActiveField( DataField activeField )
    {
    put( activeField.name, activeField );
    }

  /**
   * Returns a Fields data structure naming the input tuple fields.
   *
   * @return Fields
   */
  public Fields getInputFields()
    {
    Fields fields = Fields.NONE;

    for( String name : keySet() )
      fields = fields.append( new Fields( name, get( name ).dataType ) );

    return fields;
    }

  public Fields getDeclaredFields()
    {
    if( labelField == null )
      return new Fields( "predict", String.class );

    return new Fields( labelField.getName(), labelField.dataType );
    }

  /**
   * Returns the expected name for each field in the Tuple, to be
   * used as Janino parameters.
   *
   * @return String[]
   */
  public String[] getParamNames()
    {
    return keySet().toArray( new String[ 0 ] );
    }

  /**
   * Returns the expected class for each field in the Tuple, to be
   * used as Janino parameters.
   *
   * @return Class[]
   */
  public Class[] getParamTypes()
    {
    Class[] param_types = new Class[ size() ];
    Iterator<DataField> iter = values().iterator();

    for( int i = 0; i < size(); i++ )
      {
      DataField df = iter.next();
      param_types[ i ] = df.getClassType();
      }

    return param_types;
    }

  /**
   * Convert values for the fields in the Tuple, in a form that Janino expects.
   *
   * @param values
   * @param param_values
   * @throws cascading.pattern.PatternException
   *
   */
  public void setParamValues( Tuple values, Object[] param_values ) throws PatternException
    {
    Iterator<DataField> iter = values().iterator();

    for( int i = 0; i < size(); i++ )
      {
      DataField df = iter.next();
      param_values[ i ] = df.getValue( values, i );
      }
    }

  /**
   * Returns a Map of names/values for each field in the Tuple.
   *
   * @param values
   * @return Map<String, Object>
   * @throws PatternException
   */
  public Map<String, Object> getParamMap( Tuple values ) throws PatternException
    {
    HashMap<String, Object> param_map = new HashMap<String, Object>();
    Iterator<DataField> iter = values().iterator();

    for( int i = 0; i < size(); i++ )
      {
      DataField df = iter.next();
      param_map.put( df.name, df.getValue( values, i ) );
      }

    return param_map;
    }
  }
