/*
 * Copyright (c) 2014 Concurrent, Inc. All Rights Reserved.
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

package cascading.pattern.model.clustering.compare;

import org.junit.Test;

import static org.junit.Assert.*;

public class EqualCompareFunctionTest
  {
  @Test
  public void testCompare()
    {
    CompareFunction fun = new EqualCompareFunction();
    assertEquals( 0.0, fun.result( 4.0, 3.0 ), 0.0 );
    assertEquals( 0.0, fun.result( 3.0, 4.0 ), 0.0 );
    assertEquals( 1.0, fun.result( 4.0, 4.0 ), 0.0 );
    }
  }