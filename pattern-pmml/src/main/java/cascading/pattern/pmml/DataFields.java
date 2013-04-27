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

import cascading.pattern.PatternException;
import cascading.pattern.datafield.CategoricalDataField;
import cascading.pattern.datafield.ContinuousDataField;
import cascading.pattern.datafield.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.OpType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cascading.pattern.pmml.PMMLUtil.asStrings;


public class DataFields
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( DataFields.class );

  public static DataField createDataFields( org.dmg.pmml.DataField dataField )
    {
    String name = dataField.getName().getValue();

    OpType optype = dataField.getOptype();

    if( optype == OpType.CATEGORICAL )
      return createCategoricalDataField( name, dataField );
    else if( optype == OpType.CONTINUOUS )
      return createContinuousDataField( name, dataField );

    throw new UnsupportedOperationException( "unsupported optype: " + optype );
    }

  public static ContinuousDataField createContinuousDataField( String name, org.dmg.pmml.DataField dataField )
    {
    if( !dataField.getIntervals().isEmpty() )
      throw new UnsupportedOperationException( "intervals not supported" );

    DataType dataType = dataField.getDataType();
    Type type = DataTypes.getPmmlToType( dataType );

    return new ContinuousDataField( name, type );
    }

  public static CategoricalDataField createCategoricalDataField( String name, org.dmg.pmml.DataField dataField )
    {
    DataType dataType = dataField.getDataType();
    Type type = DataTypes.getPmmlToType( dataType );

    if( type == String.class )
      return new CategoricalDataField( name, type, asStrings( dataField.getValues() ) );

    String message = String.format( "unsupported data type: %s", dataType );
    LOG.error( message );

    throw new PatternException( message );
    }
  }
