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

import cascading.scheme.util.FieldTypeResolver;

/**
 *
 */
class PMMLTypeResolver implements FieldTypeResolver
  {
  private transient PMMLPlanner pmmlPlanner;
  private Map<String, Type> pmmlDictionary;

  public PMMLTypeResolver( PMMLPlanner pmmlPlanner )
    {
    this.pmmlPlanner = pmmlPlanner;
    this.pmmlDictionary = pmmlPlanner.getPMMLModel().getDictionary();
    }

  protected synchronized Map<String, Type> getPmmlDictionary()
    {
    // intentionally build a new one in case new values are added
    // after serialization, when the pmmlPlanner is null, use the cached version
    if( pmmlPlanner != null )
      this.pmmlDictionary = pmmlPlanner.getPMMLModel().getDictionary();

    return pmmlDictionary;
    }

  @Override
  public Type inferTypeFrom( int ordinal, String fieldName )
    {
    Map<String, Type> pmmlDictionary = getPmmlDictionary();
    Type type = pmmlDictionary.get( fieldName );

    if( type == null && pmmlDictionary.containsKey( fieldName ) )
      throw new UnsupportedOperationException( "requested type is unsupported for fieldName: " + fieldName );

    if( type == null )
      return String.class;

    return type;
    }

  @Override
  public String cleanField( int ordinal, String fieldName, Type type )
    {
    return fieldName;
    }

  @Override
  public String prepareField( int i, String fieldName, Type type )
    {
    return fieldName;
    }
  }
