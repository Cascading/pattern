/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package pattern;

import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class XPathReader
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( XPathReader.class );

  private Document xmlDocument;
  private XPath xPath;

  /** @param xmlFile  */
  public XPathReader( String xmlFile )
    {
    try
      {
      xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( xmlFile );
      xPath = XPathFactory.newInstance().newXPath();
      }
    catch( IOException exception )
      {
      LOG.error( "could not read PMML file", exception );
      throw new PatternException( " could not read PMML file", exception );
      }
    catch( SAXException exception )
      {
      LOG.error( "could not parse PMML file", exception );
      throw new PatternException( " could not parse PMML file", exception );
      }
    catch( ParserConfigurationException exception )
      {
      LOG.error( "could not configure parser for PMML file", exception );
      throw new PatternException( " could not configure parser for PMML file", exception );
      }
    }

  /**
   * @param expression
   * @param returnType
   * @return
   */
  public Object read( String expression, QName returnType )
    {
    Object result = null;

    try
      {
      XPathExpression xPathExpression = xPath.compile( expression );
      result = xPathExpression.evaluate( xmlDocument, returnType );
      }
    catch( XPathExpressionException exception )
      {
      String message = String.format( "could not evaluate XPath [ %s ] from doc root", expression );
      LOG.error( message, exception );
      throw new PatternException( message, exception );
      }
    finally {
      return result;
      }
    }

  /**
   * @param item
   * @param expression
   * @param returnType
   * @return
   */
  public Object read( Object item, String expression, QName returnType )
    {
    Object result = null;

    try
      {
      XPathExpression xPathExpression = xPath.compile( expression );
      result = xPathExpression.evaluate( item, returnType );
      }
    catch( XPathExpressionException exception )
      {
      String message = String.format( "could not evaluate XPath [ %s ] from %s", expression, item.toString() );
      LOG.error( message, exception );
      throw new PatternException( message, exception );
      }
    finally {
      return result;
      }
    }
  }
