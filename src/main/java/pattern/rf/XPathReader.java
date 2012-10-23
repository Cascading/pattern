/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
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

package pattern.rf;

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

 
public class
  XPathReader
  {
  private Document xmlDocument;
  private XPath xPath;

 
  public XPathReader( String xmlFile ) {
    try {
      xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( xmlFile );
      xPath = XPathFactory.newInstance().newXPath();
    } catch ( IOException e ) {
      e.printStackTrace();
    } catch ( SAXException e ) {
      e.printStackTrace();
    } catch ( ParserConfigurationException e ) {
      e.printStackTrace();
    }
  }

 
  public Object read( String expression, QName returnType ) {
    try {
      XPathExpression xPathExpression = xPath.compile( expression );
      return xPathExpression.evaluate( xmlDocument, returnType );
    } catch( XPathExpressionException e ) {
      e.printStackTrace();
      return null;
    }
  }


  public Object read( Object item, String expression, QName returnType ) {
    try {
      XPathExpression xPathExpression = xPath.compile( expression );
      return xPathExpression.evaluate( item, returnType );
    } catch( XPathExpressionException e ) {
      e.printStackTrace();
      return null;
    }
  }
}
