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
import java.util.Map;

import cascading.tuple.coerce.Coercions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.primitives.Primitives;
import org.dmg.pmml.DataType;

/**
 *
 */
class DataTypes
  {
  private static Map<DataType, Type> pmmlToType = HashBiMap.create();
  private static Map<Type, DataType> typeToPmml = ( (BiMap<DataType, Type>) pmmlToType ).inverse();

  static
    {
    pmmlToType.put( DataType.BOOLEAN, Boolean.class );
    pmmlToType.put( DataType.INTEGER, Integer.class );
    pmmlToType.put( DataType.FLOAT, Float.class );
    pmmlToType.put( DataType.DOUBLE, Double.class );
    pmmlToType.put( DataType.STRING, String.class );
    }

  public static Type getPmmlToType( DataType dataType )
    {
    return pmmlToType.get( dataType );
    }

  public static DataType getTypeToPmml( Type type )
    {
    if( type instanceof Class )
      type = Primitives.wrap( (Class<?>) type );

    return typeToPmml.get( type );
    }

  public static Object coerceTo( Object value, DataType dataType )
    {
    return Coercions.coerce( value, getPmmlToType( dataType ) );
    }

  }
