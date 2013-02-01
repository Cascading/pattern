/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 *
 * @author girish.kathalagiri
 */

package pattern.model.glm;

import java.io.Serializable;

/**
 * This Class represents PCell inside ParamMatrix
 */
public class PCell implements Serializable
  {
  private String df;
  private String beta;
  private String parameterName;

  public String getDf()
    {
    return df;
    }

  public void setDf( String df )
    {
    this.df = df;
    }

  public String getBeta()
    {
    return beta;
    }

  public void setBeta( String beta )
    {
    this.beta = beta;
    }

  public String getParameterName()
    {
    return parameterName;
    }

  public void setParameterName( String parameterName )
    {
    this.parameterName = parameterName;
    }

  /** @return String  */
  public String toString()
    {
    StringBuilder buf = new StringBuilder();
    buf.append( "parameterName = " );
    buf.append( parameterName ).append("," );
    buf.append( "df = " );
    buf.append( df ).append("," );
    buf.append( "beta = " );
    buf.append( beta );

    return buf.toString();
    }
  }
