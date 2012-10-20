import java.io.IOException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

public class XPathExample
{
  public static void main(String[] argv)
	throws ParserConfigurationException, SAXException, IOException, XPathExpressionException
  {
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    domFactory.setNamespaceAware(true); // never forget this!

    DocumentBuilder builder = domFactory.newDocumentBuilder();
    Document doc = builder.parse(argv[0]);

    XPath xpath = XPathFactory.newInstance().newXPath();

    XPathExpression expr = xpath.compile("//DataDictionary/DataField/");

    Object result = expr.evaluate(doc, XPathConstants.NODESET);
    NodeList nodes = (NodeList) result;

    for (int i = 0; i < nodes.getLength(); i++) {
	System.out.println("foo");
	System.out.println(nodes.item(i).getNodeName()); 
    }
  }
}
