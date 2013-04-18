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

import java.io.Serializable;

/** This Class represents PPCell inside PPMatrix */
public class PPCell implements Serializable
  {
  private String parameterName;
  private String predictorName;
  private String value;

  public PPCell( String parameterName, String predictorName, String value )
    {
    this.predictorName = predictorName;
    this.value = value;
    this.parameterName = parameterName;
    }

  public String getPredictorName()
    {
    return predictorName;
    }

  public void setPredictorName( String predictorName )
    {
    this.predictorName = predictorName;
    }

  public String getValue()
    {
    return value;
    }

  public void setValue( String value )
    {
    this.value = value;
    }

  public String getParameterName()
    {
    return parameterName;
    }

  public void setParameterName( String parameterName )
    {
    this.parameterName = parameterName;
    }

  /** @return String */
  public String toString()
    {
    StringBuilder buf = new StringBuilder();
    buf.append( "parameterName = " );
    buf.append( parameterName + "," );
    buf.append( "predictorName = " );
    buf.append( predictorName + "," );
    buf.append( "value = " );
    buf.append( value );

    return buf.toString();
    }
  }
