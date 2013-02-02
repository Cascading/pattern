/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 *
 * @author girish.kathalagiri
 */

package pattern.model.glm;

/**
 * Enum for the LinkFunction in GLM
 */
public enum LinkFunction
  {
    NONE( "none" )
      {
      public String calc( double value )
        {
         return Double.toString( Double.NaN ); 
        }
      },

    LOGIT( "logit" )
      {
      public String calc( double value )
        {
        return Double.toString( 1.0 / ( 1.0 + Math.exp( -value ) ) );
        }
      },

   CLOGLOG( "cloglog" )
      {
      public String calc( double value )
        {
        return Double.toString( 1.0 - Math.exp( -Math.exp( value ) ) );
        }
      },

    LOGLOG( "loglog" )
      {
      public String calc( double value )
        {
        return Double.toString( Math.exp( -Math.exp( -value ) ) );
        }
      },

    CAUCHIT( "cauchit" )
      {
      public String calc( double value )
        {
        return Double.toString( 0.5 + ( 1.0 / Math.PI ) * Math.atan( value ) );
        }
      };

  public String function;

  private LinkFunction( String function )
    {
    this.function = function;
    }

    /**
     *  Returns the corresponding LinkFunction
     *
     * @param functionName String
     * @return LinkFunction
     */
  public static LinkFunction getFunction( String functionName )
    {

    for( LinkFunction lf : values() )
      if( lf.function.matches( functionName ) )
        return lf;

    return LinkFunction.NONE;
    }

  public abstract String calc( double value );
  }
