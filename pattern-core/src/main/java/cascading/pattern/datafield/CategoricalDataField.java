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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cascading.tuple.Fields;


/**
 * Class CategoricalDataField represent a field with a fixed set of possible values.
 * <p/>
 * For example, if the field name is {@code SIZE}, it could have three possible categories,
 * {@code small}, {@code medium}, and {@code large}.
 * <p/>
 * Order of categories is retained so that indexes into the internal list of categories can be used
 * to speed up some operations.
 */
public class CategoricalDataField extends DataField
  {
  protected List<String> categories = new ArrayList<String>();

  public CategoricalDataField( CategoricalDataField dataField, String... categories )
    {
    this( dataField.name, dataField.getType(), categories );
    }

  public CategoricalDataField( Fields fields, String... categories )
    {
    this( fields.get( 0 ).toString(), fields.getType( 0 ), categories );
    }

  public CategoricalDataField( Fields fields, List<String> categories )
    {
    this( fields.get( 0 ).toString(), fields.getType( 0 ), categories );
    }

  public CategoricalDataField( String name, Type type, String... categories )
    {
    this( name, type, Arrays.asList( categories ) );
    }

  public CategoricalDataField( String name, Type type, List<String> categories )
    {
    super( name, type );
    this.categories.addAll( categories );
    }

  /**
   * Gets an unmodifiable list of the current categories.
   *
   * @return the categories
   */
  public List<String> getCategories()
    {
    return Collections.unmodifiableList( categories );
    }
  }
