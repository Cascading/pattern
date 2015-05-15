/*
 * Copyright (c) 2007-2015 Concurrent, Inc. All Rights Reserved.
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

import java.io.Serializable;
import java.lang.reflect.Type;

import cascading.tuple.Fields;


public abstract class DataField implements Serializable
  {
  public String name;
  public Type type;

  protected DataField( Fields fields )
    {
    this( fields.get( 0 ).toString(), fields.getType( 0 ) );
    }

  protected DataField( String name, Type type )
    {
    if( name == null || name.isEmpty() )
      throw new IllegalArgumentException( "name may not be null or empty" );

    if( type == null )
      throw new IllegalArgumentException( "type may not be null" );

    this.name = name;
    this.type = type;
    }

  public String getName()
    {
    return name;
    }

  public Type getType()
    {
    return type;
    }

  @Override
  public String toString()
    {
    return name + ":" + getClass().getSimpleName() + ":" + type;
    }
  }
