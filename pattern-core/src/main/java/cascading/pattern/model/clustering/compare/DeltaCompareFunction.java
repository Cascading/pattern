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

/**
 * PMML Delta comparison function. 0 if the two sides are equal, 1 if not
 * 'c(x,y) = 0 if x=y, 1 else'
 */
public class DeltaCompareFunction extends CompareFunction
  {
  @Override
  public double result( double lhs, double rhs )
    {
    return ( lhs == rhs ) ? 0 : 1;
    }

  @Override
  public int hashCode()
    {
    return 37;
    }

  @Override
  public boolean equals( Object obj )
    {
    if( this == obj )
      return true;
    if( obj == null )
      return false;
    if( getClass() != obj.getClass() )
      return false;
    return true;
    }

  @Override
  public String toString()
    {
    return "DeltaCompareFunction []";
    }

  }
