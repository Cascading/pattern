import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;

 
public class XPathReader
{
  private String xmlFile;
  private Document xmlDocument;
  private XPath xPath;

 
  public XPathReader( String xmlFile ) {
    this.xmlFile = xmlFile;
    initObjects();
  }

 
  private void initObjects() {
      try {
        xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        xPath = XPathFactory.newInstance().newXPath();
      } catch ( IOException ex ) {
        ex.printStackTrace();
      } catch ( SAXException ex ) {
        ex.printStackTrace();
      } catch ( ParserConfigurationException ex ) {
        ex.printStackTrace();
      }
  }

 
  public Object read( String expression, QName returnType ) {
    try {
      XPathExpression xPathExpression = xPath.compile( expression );
      return xPathExpression.evaluate( xmlDocument, returnType );
    } catch( XPathExpressionException ex ) {
      ex.printStackTrace();
      return null;
    }
  }


  public Object read( Object item, String expression, QName returnType ) {
    try {
      XPathExpression xPathExpression = xPath.compile( expression );
      return xPathExpression.evaluate( item, returnType );
    } catch( XPathExpressionException ex ) {
      ex.printStackTrace();
      return null;
    }
  }


  public static void main( String[] argv ) throws Exception {
      // STAGE 1:
      // parse the XML file

      String xml_file = argv[0];
      XPathReader reader = new XPathReader( xml_file );
 
      // verify the model type

      String expr = "/PMML/MiningModel/@modelName";
      String model_type = (String) reader.read( expr, XPathConstants.STRING );
      System.out.println( "// model: " + model_type );

      // determine the data dictionary

      ArrayList<String> schema = new ArrayList<String>();

      expr = "/PMML/DataDictionary/DataField";
      NodeList node_list = (NodeList) reader.read( expr, XPathConstants.NODESET );

      for ( int i = 0; i < node_list.getLength(); i++ ) {
	  Node node = node_list.item( i );

	  if ( node.getNodeType() == Node.ELEMENT_NODE ) {
	      String name = ( (Element) node ).getAttribute( "name" );
	      String op_type = ( (Element) node ).getAttribute( "optype" );

	      if ( !schema.contains( name ) ) {
		  schema.add( name );
	      }

	      System.out.println( "// " + schema.indexOf( name ) + ", " + name  + ", " + op_type );
	  }
      }

      // determine the input schema

      expr = "/PMML/MiningModel/MiningSchema/MiningField";
      node_list = (NodeList) reader.read( expr, XPathConstants.NODESET );

      for ( int i = 0; i < node_list.getLength(); i++ ) {
	  Node node = node_list.item( i );

	  if ( node.getNodeType() == Node.ELEMENT_NODE ) {
	      String name = ( (Element) node ).getAttribute( "name" );
	      String usage_type = ( (Element) node ).getAttribute( "usageType" );

	      if ( !schema.contains( name ) ) {
		  schema.add( name );
	      }

	      System.out.println( "// " + schema.indexOf( name ) + ", " + name  + ", " + usage_type );
	  }
      }

      // STAGE 2:
      // generate code for each tree

      ArrayList<String> predicates = new ArrayList<String>();
      ArrayList<DirectedGraph<String, DefaultEdge>> forest = new ArrayList<DirectedGraph<String, DefaultEdge>>();

      expr = "/PMML/MiningModel/Segmentation/Segment";
      node_list = (NodeList) reader.read( expr, XPathConstants.NODESET );

      for ( int i = 0; i < node_list.getLength(); i++ ) {
	  Node node = node_list.item( i );

	  if ( node.getNodeType() == Node.ELEMENT_NODE ) {
	      String id = ( (Element) node ).getAttribute( "id" );
	      String tree_name = "tree_" + id;

	      expr = "./TreeModel/Node[1]";
	      NodeList root_node = (NodeList) reader.read( node, expr, XPathConstants.NODESET );
	      traverseTree( (Element) root_node.item( 0 ), tree_name, predicates, forest );
	  }
      }

      // STAGE 3:
      // enumerate the predicates

      System.out.println( "---------" );

      for ( String predicate : predicates ) {
	  System.out.println( "expr[ " + predicates.indexOf( predicate ) + " ]: " + predicate );
      }

      // evaluate the predicates

      try {
	  ExpressionEvaluator exprEval = new ExpressionEvaluator();
	  exprEval.cook("3 + 4");
	  System.out.println(exprEval.evaluate(null));
      } catch( CompileException ce ) {
	  ce.printStackTrace();
      } catch( InvocationTargetException ite ) {
	  ite.printStackTrace();
      }
  }


  private static String spacer( int depth ) {
      String pad = "";

      for (int i = 0; i < depth; i++) {
	  pad += " ";
      }

      return pad;
  }


  private static void traverseTree( Element tree_root, String tree_name, ArrayList<String> predicates, ArrayList<DirectedGraph<String, DefaultEdge>> forest ) throws Exception {
      DirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
      forest.add( graph );

      System.out.println( tree_name );

      traverseNode( tree_root, 0, predicates, graph );

      System.out.println( "// " + graph.toString() );
  }


  private static String traverseNode( Element node, Integer depth, ArrayList<String> predicates, DirectedGraph<String, DefaultEdge> graph ) throws Exception {
      String pad = spacer( depth );

      String id = ( node ).getAttribute( "id" );
      graph.addVertex( id );

      System.out.println( pad + "// node " + id + ", " + depth );

      NodeList child_nodes = node.getChildNodes();

      for ( int i = 0; i < child_nodes.getLength(); i++ ) {
	  Node child = child_nodes.item( i );

	  if ( child.getNodeType() == Node.ELEMENT_NODE ) {
	      if ( child.getNodeName().equals( "SimplePredicate" ) ) {
		  int position = composePredicate( (Element) child, predicates );
		  System.out.println( pad + "if expr[ " + position + " ]" );

		  if ( node.hasAttribute( "score" ) ) {
		      String score = ( node ).getAttribute( "score" );
		      System.out.println( pad + " score " + score );
		  }
	      }
	      else if ( child.getNodeName().equals( "Node" ) ) {
		  String child_id = traverseNode( (Element) child, depth + 1, predicates, graph );
		  graph.addEdge( id, child_id );
	      }
	  }
      }

      return id;
  }


  private static int composePredicate( Element node, ArrayList<String> predicates ) throws Exception {
      String field = node.getAttribute( "field" );
      String operator = node.getAttribute( "operator" );
      String value = node.getAttribute( "value" );

      String eval = null;

      if ( operator.equals( "greaterThan" ) ) {
	  eval = field + " > " + value;
      }
      else if ( operator.equals( "lessOrEqual" ) ) {
	  eval = field + " <= " + value;
      }
      else {
	  throw new Exception( "unknown operator: " + operator );
      }

      if ( !predicates.contains( eval ) ) {
	  predicates.add( eval );
      }

      int position = predicates.indexOf( eval );

      return position;
  }
}
