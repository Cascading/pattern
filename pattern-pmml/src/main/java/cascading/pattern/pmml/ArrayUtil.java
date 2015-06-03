/**
 * Backport of ArrayUtil from https://github.com/jpmml/jpmml at 8a8b2fcb867b4fa3cde85f75916f8d23fbd972a3 (Apache Licensed)
 * */

/*
 * Copyright (c) 2012 University of Tartu
 */

package cascading.pattern.pmml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import cascading.pattern.PatternException;
import org.dmg.pmml.Array;

public class ArrayUtil
  {

  static public List<String> parse( Array array )
    {
    List<String> result;

    Array.Type type = array.getType();
    switch( type )
      {
      case INT:
      case REAL:
        result = tokenize( array.getValue(), false );
        break;
      case STRING:
        result = tokenize( array.getValue(), true );
        break;
      default:
        throw new PatternException( "unsupported feature " + array + " " + type );
      }

    Integer n = array.getN();
    if( n != null && n.intValue() != result.size() )
      throw new PatternException( "invalid feature " + array );

    return result;
    }

  static public List<String> tokenize( String string, boolean enableQuotes )
    {
    List<String> result = new ArrayList<String>();

    StringBuilder sb = new StringBuilder();

    boolean quoted = false;

    tokens:
    for( int i = 0; i < string.length(); i++ )
      {
      char c = string.charAt( i );

      if( quoted )
        {

        if( c == '\\' && i < ( string.length() - 1 ) )
          {
          c = string.charAt( i + 1 );

          if( c == '\"' )
            {
            sb.append( '\"' );

            i++;
            }
          else
            sb.append( '\\' );

          continue tokens;
          } // End if

        sb.append( c );

        if( c == '\"' )
          {
          result.add( createToken( sb, enableQuotes ) );
          quoted = false;
          }
        }
      else
        {
        if( c == '\"' && enableQuotes )
          {
          if( sb.length() > 0 )
            result.add( createToken( sb, enableQuotes ) );

          sb.append( '\"' );

          quoted = true;
          }
        else if( Character.isWhitespace( c ) )
          {
          if( sb.length() > 0 )
            result.add( createToken( sb, enableQuotes ) );
          }
        else
          sb.append( c );
        }
      }

    if( sb.length() > 0 )
      result.add( createToken( sb, enableQuotes ) );

    return Collections.unmodifiableList( result );
    }

  static
  private String createToken( StringBuilder sb, boolean enableQuotes )
    {
    String result;

    if( sb.length() > 1 && ( sb.charAt( 0 ) == '\"' && sb.charAt( sb.length() - 1 ) == '\"' ) && enableQuotes )
      result = sb.substring( 1, sb.length() - 1 );
    else
      result = sb.substring( 0, sb.length() );

    sb.setLength( 0 );
    return result;
    }

  }