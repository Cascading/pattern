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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CategoricalDataField extends DataField
  {
  protected List<String> categories = new ArrayList<String>();

  public CategoricalDataField( String name, Type dataType, String... categories )
    {
    this( name, dataType, Arrays.asList( categories ) );
    }

  public CategoricalDataField( String name, Type dataType, List<String> categories )
    {
    this.name = name;
    this.type = dataType;
    this.categories.addAll( categories );
    }

  public List<String> getCategories()
    {
    return categories;
    }
  }
