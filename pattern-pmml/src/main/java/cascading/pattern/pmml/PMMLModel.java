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

package cascading.pattern.pmml;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cascading.tuple.Fields;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.Model;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PMMLModel
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( PMMLModel.class );

  private final PMML pmml;

  public PMMLModel( PMML pmml )
    {
    this.pmml = pmml;
    }

  public List<Model> getModels()
    {
    List<Model> content = pmml.getContent();

    if( content == null )
      return Collections.EMPTY_LIST;

    return content;
    }

  public Fields getActiveFields()
    {
    return getAllFieldsWithUsage( FieldUsageType.ACTIVE );
    }

  public Fields getPredictedFields()
    {
    return getAllFieldsWithUsage( FieldUsageType.PREDICTED );
    }

  private Fields getAllFieldsWithUsage( FieldUsageType usageType )
    {
    Map<String, Type> fields = new LinkedHashMap<String, Type>();

    for( Model model : getModels() )
      {
      for( MiningField miningField : model.getMiningSchema().getMiningFields() )
        {
        if( miningField.getUsageType() != usageType )
          continue;

        DataField dataField = getDataField( miningField.getName() );

        fields.put( miningField.getName().getValue(), DataTypes.getPmmlToType( dataField.getDataType() ) );
        }
      }

    Fields results = Fields.NONE;

    for( String name : fields.keySet() )
      results = results.append( new Fields( name, fields.get( name ) ) );

    return results;
    }

  public Map<String, Type> getDictionary()
    {
    Map<String, Type> map = new LinkedHashMap<String, Type>();

    for( DataField dataField : pmml.getDataDictionary().getDataFields() )
      map.put( dataField.getName().getValue(), DataTypes.getPmmlToType( dataField.getDataType() ) );

    return map;
    }

  public DataField getDataField( FieldName name )
    {
    List<DataField> dataFields = pmml.getDataDictionary().getDataFields();

    for( DataField dataField : dataFields )
      {
      if( ( dataField.getName() ).equals( name ) )
        return dataField;
      }

    return null;
    }

  public DataField addDataField( FieldName name, OpType opType, DataType dataType )
    {
    return addDataField( name, null, opType, dataType );
    }

  public DataField addDataField( FieldName name, String displayName, OpType opType, DataType dataType )
    {
    DataField dataField = new DataField( name, opType, dataType );
    dataField.setDisplayName( displayName );

    List<DataField> dataFields = pmml.getDataDictionary().getDataFields();
    dataFields.add( dataField );

    return dataField;
    }
  }
