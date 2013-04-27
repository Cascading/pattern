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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cascading.pattern.datafield.CategoricalDataField;
import cascading.pattern.datafield.ContinuousDataField;
import cascading.pattern.datafield.DataField;
import cascading.tuple.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ModelSchema implements Serializable
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( ModelSchema.class );

  public Map<String, DataField> expectedFields = new LinkedHashMap<String, DataField>();
  public List<DataField> predictedFields;

  public ModelSchema()
    {
    }

  public ModelSchema( Fields expectedFields, Fields predictedFields )
    {
    addExpectedFields( expectedFields );
    setPredictedFields( predictedFields );
    }

  public void setPredictedFields( Fields fields )
    {
    predictedFields = toDataFields( fields );

    if( predictedFields.size() != 1 )
      throw new IllegalArgumentException( "currently only support one predicted field, got: " + fields.printVerbose() );
    }

  public void setPredictedFields( DataField predictedFields )
    {
    this.predictedFields = Arrays.asList( predictedFields );
    }

  public DataField getExpectedField( String name )
    {
    return expectedFields.get( name );
    }

  public void addExpectedFields( Fields fields )
    {
    for( DataField dataField : toDataFields( fields ) )
      expectedFields.put( dataField.getName(), dataField );
    }

  public void addExpectedFields( Fields fields, String[][] categories )
    {
    for( DataField dataField : toDataFields( fields, categories ) )
      expectedFields.put( dataField.getName(), dataField );
    }

  public void addExpectedField( DataField expectedField )
    {
    expectedFields.put( expectedField.name, expectedField );
    }

  private static List<DataField> toDataFields( Fields fields )
    {
    List<DataField> dataFields = new ArrayList<DataField>();

    for( Comparable field : fields )
      {
      if( field instanceof Number )
        throw new IllegalArgumentException( "all fields must be names, not ordinal, got: " + field );

      dataFields.add( new ContinuousDataField( (String) field, fields.getType( field ) ) );
      }

    return dataFields;
    }

  private static List<DataField> toDataFields( Fields fields, String[][] categories )
    {
    List<DataField> dataFields = new ArrayList<DataField>();

    for( Comparable field : fields )
      {
      if( field instanceof Number )
        throw new IllegalArgumentException( "all fields must be names, not ordinal, got: " + field );

      dataFields.add( new CategoricalDataField( (String) field, fields.getType( field ), categories[ fields.getPos( field ) ] ) );
      }

    return dataFields;
    }

  /**
   * Returns a Fields data structure naming the input tuple fields.
   *
   * @return Fields
   */
  public Fields getInputFields()
    {
    Fields fields = Fields.NONE;

    for( String name : expectedFields.keySet() )
      fields = fields.append( new Fields( name, expectedFields.get( name ).type ) );

    return fields;
    }

  public Fields getDeclaredFields()
    {
    if( predictedFields == null )
      return new Fields( "predict", String.class );

    return new Fields( predictedFields.get( 0 ).getName(), predictedFields.get( 0 ).type );
    }
  }
