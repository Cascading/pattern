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

package cascading.pattern.model.regression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cascading.pattern.model.ModelSchema;
import cascading.pattern.model.Spec;
import cascading.pattern.model.normalization.Normalization;

public class RegressionSpec extends Spec
  {
  List<RegressionTable> regressionTables = new ArrayList<RegressionTable>();
  private Normalization normalization;

  public RegressionSpec( ModelSchema modelSchema )
    {
    super( modelSchema );
    }

  public List<RegressionTable> getRegressionTables()
    {
    return Collections.unmodifiableList( regressionTables );
    }

  public void addRegressionTable( RegressionTable regressionTable )
    {
    regressionTables.add( regressionTable );
    }

  public void setNormalization( Normalization normalization )
    {
    this.normalization = normalization;
    }

  public Normalization getNormalization()
    {
    return normalization;
    }
  }
