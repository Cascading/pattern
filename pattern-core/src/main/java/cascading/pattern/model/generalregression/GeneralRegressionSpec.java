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

package cascading.pattern.model.generalregression;

import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.Spec;


public class GeneralRegressionSpec extends Spec
  {
  GeneralRegressionTable generalRegressionTable;
  LinkFunction linkFunction;

  public GeneralRegressionSpec( ModelSchema modelSchema, GeneralRegressionTable generalRegressionTable, LinkFunction linkFunction )
    {
    super( modelSchema );
    this.generalRegressionTable = generalRegressionTable;
    this.linkFunction = linkFunction;
    }

  public GeneralRegressionSpec( ModelSchema modelSchema )
    {
    super( modelSchema );
    }

  public void setGeneralRegressionTable( GeneralRegressionTable generalRegressionTable )
    {
    this.generalRegressionTable = generalRegressionTable;
    }

  public GeneralRegressionTable getGeneralRegressionTable()
    {
    return generalRegressionTable;
    }

  public LinkFunction getLinkFunction()
    {
    return linkFunction;
    }

  public void setLinkFunction( LinkFunction linkFunction )
    {
    this.linkFunction = linkFunction;
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "GeneralRegressionSpec{" );
    sb.append( "generalRegressionMatrix=" ).append( generalRegressionTable );
    sb.append( ", linkFunction=" ).append( linkFunction );
    sb.append( '}' );
    return sb.toString();
    }
  }
