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

import java.io.IOException;

import cascading.tuple.Fields;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class SimplePMMLPlatformTest extends PMMLPlatformTestCase
  {
  public SimplePMMLPlatformTest()
    {
    }

  @Test
  public void testKMeans() throws IOException
    {
    pmmlTest( "kmeans", Fields.NONE, Fields.LAST );
    }

  @Test
  public void testRandomforest() throws IOException
    {
    pmmlTest( "randomforest", Fields.FIRST, Fields.LAST );
    }

  @Test
  public void testIrisGLM() throws IOException
    {
    pmmlTest( "iris.glm", Fields.NONE, new Fields( "predict", double.class ) );
    }

  @Test
  @Ignore
  public void testIrisHC() throws IOException
    {
    pmmlTest( "iris.hc", Fields.NONE, Fields.LAST );
    }

  @Test
  public void testIrisKMeans() throws IOException
    {
    pmmlTest( "iris.kmeans", Fields.NONE, Fields.LAST );
    }

  @Test
  public void testIrisLM() throws IOException
    {
    pmmlTest( "iris.lm_p", Fields.FIRST, new Fields( "predict", double.class ) );
    }

  @Test
  public void testIrisMultiNom() throws IOException
    {
    pmmlTest( "iris.multinom", new Fields( "species" ), Fields.LAST );
    }

  @Test
  @Ignore
  public void testIrisNN() throws IOException
    {
    pmmlTest( "iris.nn", Fields.NONE, Fields.LAST );
    }

  @Test
  public void testIrisRandomForest() throws IOException
    {
    pmmlTest( "iris.rf", new Fields( "species" ), Fields.LAST );
    }

  @Test
  public void testIrisRPart() throws IOException
    {
    pmmlTest( "iris.rpart", new Fields( "species" ), Fields.LAST );
    }

  @Test
  @Ignore
  public void testIrisSVM() throws IOException
    {
    pmmlTest( "iris.svm", Fields.NONE, Fields.LAST );
    }

  @Test
  public void testSampleRandomForest() throws IOException
    {
    pmmlTest( "sample.rf", Fields.FIRST, Fields.LAST );
    }
  }
