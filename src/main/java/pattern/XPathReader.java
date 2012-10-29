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

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class XPathReader
  {
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
    catch( IOException e )
      {
      e.printStackTrace();
      }
    catch( SAXException e )
      {
      e.printStackTrace();
      }
    catch( ParserConfigurationException e )
      {
      e.printStackTrace();
      }
    }

  /**
   * @param expression
   * @param returnType
   * @return
   */
  public Object read( String expression, QName returnType )
    {
    try
      {
      XPathExpression xPathExpression = xPath.compile( expression );
      return xPathExpression.evaluate( xmlDocument, returnType );
      }
    catch( XPathExpressionException e )
      {
      e.printStackTrace();
      return null;
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
    try
      {
      XPathExpression xPathExpression = xPath.compile( expression );
      return xPathExpression.evaluate( item, returnType );
      }
    catch( XPathExpressionException e )
      {
      e.printStackTrace();
      return null;
      }
    }
  }
