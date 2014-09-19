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

package cascading.pattern.util;

/** LogUtil is an internal utility class for setting log levels. */
public class Logging
  {
  public static void setLogLevel( String level )
    {
    setLogLevel( Logging.class.getClassLoader(), "cascading", level );
    }

  public static void setLogLevel( Class<?> type, String log, String level )
    {
    setLogLevel( type.getClassLoader(), log, level );
    }

  public static void setLogLevel( ClassLoader loader, String log, String level )
    {
    Object loggerObject = getLoggerObject( loader, log );

    Object levelObject = Reflection.invokeStaticMethod( loader, "org.apache.log4j.Level", "toLevel",
      new Object[]{level}, new Class[]{String.class} );

    Reflection.invokeInstanceMethod( loggerObject, "setLevel", levelObject, levelObject.getClass() );
    }

  public static String getLogLevel( ClassLoader loader, String log )
    {
    Object loggerObject = getLoggerObject( loader, log );

    Object level = Reflection.invokeInstanceMethod( loggerObject, "getLevel" );

    if( level == null )
      return "";

    return level.toString();
    }

  private static Object getLoggerObject( ClassLoader loader, String log )
    {
    if( log == null || log.isEmpty() )
      return Reflection.invokeStaticMethod( loader, "org.apache.log4j.Logger", "getRootLogger", null, null );

    return Reflection.invokeStaticMethod( loader, "org.apache.log4j.Logger", "getLogger",
      new Object[]{log}, new Class[]{String.class} );
    }
  }
