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

import java.io.Serializable;
import java.lang.reflect.Type;

import cascading.pattern.PatternException;
import cascading.tuple.Tuple;
import org.dmg.pmml.Predicate;


public abstract class DataField implements Serializable
  {
  public String name;
  public Type dataType;

  public String getName()
    {
    return name;
    }

  /** @return  */
  public abstract Class getClassType();

  /**
   * @return Object
   * @throws PatternException
   */
  public abstract Object getValue( Tuple values, int i ) throws PatternException;

  /** @return Object */
  @Override
  public String toString()
    {
    return name + ":" + getClass().getSimpleName() + ":" + dataType;
    }

  public abstract String getExpression( Predicate predicate );
  }
