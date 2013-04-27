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


public class MiningSpec<S extends Spec> extends Spec implements Serializable
  {
  public List<S> segments = new ArrayList<S>();

  public MiningSpec( ModelSchema modelSchema )
    {
    super( modelSchema );
    }

  public MiningSpec( ModelSchema modelSchema, List<S> segments )
    {
    super( modelSchema );
    this.segments = segments;
    }

  public void addSegments( List<S> segments )
    {
    this.segments.addAll( segments );
    }

  public void addSegment( S segment )
    {
    this.segments.add( segment );
    }

  public List<S> getSegments()
    {
    return segments;
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "MiningSpec{" );
    sb.append( "segments=" ).append( segments );
    sb.append( '}' );
    return sb.toString();
    }
  }
