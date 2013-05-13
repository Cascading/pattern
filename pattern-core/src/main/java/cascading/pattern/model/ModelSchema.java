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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

  Map<String, DataField> dictionary = new LinkedHashMap<String, DataField>();
  List<String> keyFields = new LinkedList<String>();
  List<String> expectedFields = new LinkedList<String>();
  List<String> predictedFields = new LinkedList<String>();
  boolean includePredictedCategories = false;

  public ModelSchema()
    {
    }

  public ModelSchema( Fields expectedFields, Fields predictedFields )
    {
    addExpectedFields( expectedFields );
    setPredictedFields( predictedFields );
    }

  public boolean isIncludePredictedCategories()
    {
    return includePredictedCategories;
    }

  public void setIncludePredictedCategories( boolean includePredictedCategories )
    {
    this.includePredictedCategories = includePredictedCategories;
    }

  public List<String> getKeyFieldNames()
    {
    return keyFields;
    }

  public DataField getKeyField( String name )
    {
    if( keyFields.contains( name ) )
      return dictionary.get( name );

    return null;
    }

  public void addKeyFields( Fields fields )
    {
    List<DataField> dataFields = toDataFields( fields );

    addToDictionary( dataFields );

    for( DataField dataField : dataFields )
      keyFields.add( dataField.getName() );
    }

  public DataField getPredictedField( String name )
    {
    if( predictedFields.contains( name ) )
      return dictionary.get( name );

    return null;
    }

  public List<String> getPredictedFieldNames()
    {
    return predictedFields;
    }

  public void setPredictedFields( Fields fields )
    {
    setPredictedFields( toDataFields( fields ) );
    }

  public void setPredictedFields( List<DataField> dataFields )
    {
    if( dataFields.size() != 1 )
      throw new IllegalArgumentException( "currently only support one predicted field, got: " + dataFields );

    addToDictionary( dataFields );

    for( DataField dataField : dataFields )
      predictedFields.add( dataField.getName() );
    }

  public void setPredictedFields( DataField predictedFields )
    {
    setPredictedFields( Arrays.asList( predictedFields ) );
    }

  public void setPredictedCategories( String fieldName, String... categories )
    {
    if( !predictedFields.contains( fieldName ) )
      throw new IllegalArgumentException( "predicted field does not exist: " + fieldName );

    DataField dataField = dictionary.get( fieldName );

    if( dataField instanceof ContinuousDataField )
      dataField = new CategoricalDataField( fieldName, String.class, categories );
    else
      ( (CategoricalDataField) dataField ).setCategories( categories );

    dictionary.put( fieldName, dataField );
    }

  public List<String> getPredictedCategories( String fieldName )
    {
    DataField dataField = dictionary.get( fieldName );

    if( dataField instanceof ContinuousDataField )
      throw new IllegalArgumentException( "field is not categorical: " + fieldName );

    return Collections.unmodifiableList( ( (CategoricalDataField) dataField ).getCategories() );
    }

  public List<String> getExpectedFieldNames()
    {
    return expectedFields;
    }

  public DataField getExpectedField( String name )
    {
    if( expectedFields.contains( name ) )
      return dictionary.get( name );

    return null;
    }

  public void addExpectedFields( Fields fields )
    {
    List<DataField> dataFields = toDataFields( fields );

    addToDictionary( dataFields );

    for( DataField dataField : dataFields )
      expectedFields.add( dataField.getName() );
    }

  public void addExpectedFields( Fields fields, String[][] categories )
    {
    List<DataField> dataFields = toDataFields( fields, categories );

    addToDictionary( dataFields );

    for( DataField dataField : dataFields )
      expectedFields.add( dataField.getName() );
    }

  public void addExpectedField( DataField expectedField )
    {
    expectedFields.add( expectedField.name );
    addToDictionary( expectedField );
    }

  private void addToDictionary( List<DataField> dataFields )
    {
    for( DataField dataField : dataFields )
      addToDictionary( dataField );
    }

  private void addToDictionary( DataField dataField )
    {
    if( dictionary.containsKey( dataField.getName() ) )
      throw new IllegalArgumentException( "data field already exists: " + dataField.getName() );

    dictionary.put( dataField.getName(), dataField );
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

  public Fields getPredictedFields()
    {
    if( predictedFields.isEmpty() )
      return new Fields( "predict", String.class );

    return createField( predictedFields.get( 0 ) );
    }

  public Fields getKeyFields()
    {
    return createFields( keyFields );
    }

  /**
   * Returns a Fields data structure naming the input tuple fields.
   *
   * @return Fields
   */
  public Fields getInputFields()
    {
    return createFields( expectedFields );
    }

  public Fields getDeclaredFields()
    {
    Fields fields = getPredictedFields();

    if( isIncludePredictedCategories() )
      {
      DataField dataField = dictionary.get( fields.get( 0 ).toString() );

      if( dataField instanceof CategoricalDataField )
        {
        for( String category : ( (CategoricalDataField) dataField ).getCategories() )
          fields = fields.append( new Fields( category, boolean.class ) );
        }
      }

    return fields;
    }

  private Fields createFields( Collection<String> fieldsList )
    {
    Fields fields = Fields.NONE;

    for( String name : fieldsList )
      fields = fields.append( createField( name ) );

    return fields;
    }

  private Fields createField( String field )
    {
    return new Fields( field, dictionary.get( field ).type );
    }
  }
