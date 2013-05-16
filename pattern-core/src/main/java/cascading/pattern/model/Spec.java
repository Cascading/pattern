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
import java.util.List;

import cascading.pattern.datafield.CategoricalDataField;
import cascading.pattern.datafield.DataField;


public abstract class Spec implements Serializable
  {
  protected ModelSchema modelSchema = null;

  protected Spec()
    {
    }

  protected Spec( ModelSchema modelSchema )
    {
    this.modelSchema = modelSchema;
    }

  public void setModelSchema( ModelSchema modelSchema )
    {
    this.modelSchema = modelSchema;
    }

  public ModelSchema getModelSchema()
    {
    if( modelSchema == null )
      modelSchema = new ModelSchema();

    return modelSchema;
    }

  public boolean isPredictedCategorical()
    {
    DataField predictedField = getModelSchema().getPredictedField( getModelSchema().getPredictedFieldNames().get( 0 ) );

    return predictedField instanceof CategoricalDataField;
    }

  public List<String> getCategories()
    {
    DataField predictedField = getModelSchema().getPredictedField( getModelSchema().getPredictedFieldNames().get( 0 ) );

    List<String> categories = new ArrayList<String>();

    if( predictedField instanceof CategoricalDataField )
      categories.addAll( ( (CategoricalDataField) predictedField ).getCategories() );

    return categories;
    }

  public String[] getCategoriesArray()
    {
    List<String> categories = getCategories();

    return categories.toArray( new String[ categories.size() ] );
    }
  }
