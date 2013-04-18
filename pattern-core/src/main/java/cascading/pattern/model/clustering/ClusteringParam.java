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

package cascading.pattern.model.clustering;

import java.util.List;

import cascading.pattern.model.MiningSchemaParam;
import cascading.pattern.model.Param;


public class ClusteringParam extends Param
  {
  public List<Exemplar> exemplars;

  public ClusteringParam( MiningSchemaParam schemaParam, List<Exemplar> exemplars )
    {
    super( schemaParam );
    this.exemplars = exemplars;
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "ClusteringParam{" );
    sb.append( "exemplars=" ).append( exemplars );
    sb.append( '}' );
    return sb.toString();
    }
  }
