import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javax.xml.xpath.XPathConstants;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.DirectedGraph;

 
public class RandomForest
{
  protected XPathReader reader;
  public ArrayList<String> schema = new ArrayList<String>();
  public ArrayList<String> predicates = new ArrayList<String>();
  public ArrayList<Tree> forest = new ArrayList<Tree>();


  public static void main( String[] argv ) throws Exception {
      String pmml_file = argv[0];
      RandomForest rf = new RandomForest( pmml_file );

      //////////////////////////////////////////////////////////////////////
      // enumerate the predicates

      System.out.println( "---------" );
      System.out.println( rf.schema );
      System.out.println( "---------" );
      System.out.println( rf.forest );
      System.out.println( "---------" );

      for ( Tree tree : rf.forest ) {
	  System.out.println( tree );
	  System.out.println( tree.getRoot() );

	  for ( Edge edge : tree.getGraph().edgeSet() ) {
	      System.out.println( edge );
	  }

	  System.out.println( "---------" );
	  tree.traverse();
      }

      System.out.println( "---------" );

      for ( String predicate : rf.predicates ) {
	  System.out.println( "expr[ " + rf.predicates.indexOf( predicate ) + " ]: " + predicate );
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


  public RandomForest ( String pmml_file ) throws Exception {
      // parse the PMML file and verify the model type

      reader = new XPathReader( pmml_file );

      String expr = "/PMML/MiningModel/@modelName";
      String model_type = (String) reader.read( expr, XPathConstants.STRING );

      if ( !"randomForest_Model".equals(model_type) ) {
	  throw new Exception( "incorrect model type: " + model_type );
      }

      // build the serializable model

      buildSchema();
      buildForest();
  }


  protected void buildSchema () {
      // build the data dictionary

      String expr = "/PMML/DataDictionary/DataField";
      NodeList node_list = (NodeList) reader.read( expr, XPathConstants.NODESET );

      for ( int i = 0; i < node_list.getLength(); i++ ) {
	  Node node = node_list.item( i );

	  if ( node.getNodeType() == Node.ELEMENT_NODE ) {
	      String name = ( (Element) node ).getAttribute( "name" );
	      String op_type = ( (Element) node ).getAttribute( "optype" );

	      if ( !schema.contains( name ) ) {
		  schema.add( name );
	      }

	      /* */
	      System.out.println( "// " + schema.indexOf( name ) + ", " + name  + ", " + op_type );
	      /* */
	  }
      }

      // determine the active tuple fields for the input schema

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

	      /* */
	      System.out.println( "// " + schema.indexOf( name ) + ", " + name  + ", " + usage_type );
	      /* */
	  }
      }
  }


  protected void buildForest () throws Exception {
      // generate code for each tree

      String expr = "/PMML/MiningModel/Segmentation/Segment";
      NodeList node_list = (NodeList) reader.read( expr, XPathConstants.NODESET );

      for ( int i = 0; i < node_list.getLength(); i++ ) {
	  Node node = node_list.item( i );

	  if ( node.getNodeType() == Node.ELEMENT_NODE ) {
	      String id = ( (Element) node ).getAttribute( "id" );
	      String node_expr = "./TreeModel/Node[1]";
	      NodeList root_node = (NodeList) reader.read( node, node_expr, XPathConstants.NODESET );

	      Tree tree = new Tree( id );
	      forest.add( tree );

	      Element root = (Element) root_node.item( 0 );
	      Vertex vertex = makeVertex( root, 0, tree.getGraph() );
	      tree.setRoot( vertex );
	      buildNode( root, vertex, 0, tree.getGraph() );

	      /* */
	      System.out.println( "// " + tree.getGraph().toString() );
	      /* */
	  }
      }
  }


  protected Vertex makeVertex( Element node, Integer depth, DirectedGraph<Vertex, Edge> graph ) {
      String pad = spacer( depth );
      String id = ( node ).getAttribute( "id" );
      Vertex vertex = new Vertex( id );
      graph.addVertex( vertex );

      /* */
      System.out.println( pad + "// node " + id + ", " + depth );
      /* */

      return vertex;
  }


  protected void buildNode( Element node, Vertex vertex, Integer depth, DirectedGraph<Vertex, Edge> graph ) throws Exception {
      String pad = spacer( depth );
      NodeList child_nodes = node.getChildNodes();

      for ( int i = 0; i < child_nodes.getLength(); i++ ) {
	  Node child = child_nodes.item( i );

	  if ( child.getNodeType() == Node.ELEMENT_NODE ) {
	      if ( child.getNodeName().equals( "SimplePredicate" ) ) {
		  Integer predicate_id = makePredicate( (Element) child );

		  /* */
		  System.out.println( pad + "if expr[ " + predicate_id + " ]" );
		  /* */

		  if ( node.hasAttribute( "score" ) ) {
		      String score = ( node ).getAttribute( "score" );
		      vertex.setScore( score );

		      /* */
		      System.out.println( pad + " score " + score );
		      /* */
		  }

		  for (Edge e: graph.edgesOf( vertex ) ) {
		      e.setPredicateId( predicate_id );
		  }
	      }
	      else if ( child.getNodeName().equals( "Node" ) ) {
		  Vertex child_vertex = makeVertex( (Element) child, depth + 1, graph );
		  Edge edge = graph.addEdge( vertex, child_vertex );

		  buildNode( (Element) child, child_vertex, depth + 1, graph );
	      }
	  }
      }
  }


  protected Integer makePredicate( Element node ) throws Exception {
      String field = node.getAttribute( "field" );
      String operator = node.getAttribute( "operator" );
      String value = node.getAttribute( "value" );

      String eval = null;

      if ( operator.equals( "greaterThan" ) ) {
	  eval = "fields[ " + schema.indexOf( field ) + " ] > " + value;
      }
      else if ( operator.equals( "lessOrEqual" ) ) {
	  eval = "fields[ " + schema.indexOf( field ) + " ] <= " + value;
      }
      else {
	  throw new Exception( "unknown operator: " + operator );
      }

      if ( !predicates.contains( eval ) ) {
	  predicates.add( eval );
      }

      Integer predicate_id = predicates.indexOf( eval );

      return predicate_id;
  }


  private static String spacer( int depth ) {
      String pad = "";

      for (int i = 0; i < depth; i++) {
	  pad += " ";
      }

      return pad;
  }
}
