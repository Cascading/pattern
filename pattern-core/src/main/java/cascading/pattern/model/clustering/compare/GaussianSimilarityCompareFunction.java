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
 * PMML gaussSim comparison function. 
 * 'c(x,y) = exp(-ln(2)*z*z/(s*s)) where z=x-y, and s is the value of attribute similarityScale'
 */
public class GaussianSimilarityCompareFunction extends CompareFunction
  {
  private final Double similarityScaleSquared;
  private static final double negLn2 = -1 * Math.log(2);
  

  /**
   * If similarityScale is null then it will cause an exception if this instance
   * is ever used to compare two values. The reason for this is that PMML allows
   * GaussianSimilarity to be the default comparison function but if it is each
   * field must specify their own similarity scale. So a null GuassianSimilar is
   * only useful as the default and each field will end up overriding it with a
   * version instantiated with the right scale
   */
  public GaussianSimilarityCompareFunction( Double similarityScale )
    {
    super();
    this.similarityScaleSquared = similarityScale == null ? null : similarityScale * similarityScale;
    }
  
  @Override
  public double result( double lhs, double rhs )
    {
    if (similarityScaleSquared == null)
      {
      throw new RuntimeException("all fields that use Guassian Similarity must have a similarity scale set.");
      }
    double z = lhs - rhs;
    return Math.exp( negLn2 * z * z / similarityScaleSquared );
    }

  @Override
  public int hashCode()
    {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( similarityScaleSquared == null ) ? 0 : similarityScaleSquared.hashCode() );
    return result;
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
    GaussianSimilarityCompareFunction other = (GaussianSimilarityCompareFunction) obj;
    if( similarityScaleSquared == null )
      {
      if( other.similarityScaleSquared != null )
        return false;
      }
    else if( !similarityScaleSquared.equals( other.similarityScaleSquared ) )
      return false;
    return true;
    }

  @Override
  public String toString()
    {
    return "GaussianSimilarityCompareFunction [similarityScaleSquared=" + similarityScaleSquared + "]";
    }


  }
