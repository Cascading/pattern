/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;


public class PatternException extends RuntimeException
  {
  /**
   *
   */
  public PatternException()
    {
    }

  /** @param message  */
  public PatternException( String message )
    {
    super( message );
    }

  /**
   * @param message
   * @param cause
   */
  public PatternException( String message, Throwable cause )
    {
    super( message, cause );
    }
  }
