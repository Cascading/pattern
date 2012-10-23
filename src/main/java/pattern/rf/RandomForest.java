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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.xpath.XPathConstants;
import org.codehaus.janino.CompileException;
import org.codehaus.janino.ExpressionEvaluator;
import org.codehaus.janino.Parser.ParseException;
import org.codehaus.janino.Scanner.ScanException;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pattern.Classifier;
import pattern.XPathReader;

 
public class RandomForest extends Classifier implements Serializable
{
  public ArrayList<String> predicates = new ArrayList<String>();
  public ArrayList<Tree> forest = new ArrayList<Tree>();


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


  public String scoreTuple( String[] fields ) {
    Boolean[] pred = evalTuple( fields );
    String score = tallyVotes( pred );

    return score;
  }


  public String toString () {
      StringBuilder buf = new StringBuilder();

      buf.append( "---------" );
      buf.append( schema );
      buf.append( "---------" );
      buf.append( forest );
      buf.append( "---------" );

      for ( Tree tree : forest ) {
	  buf.append( tree );
	  buf.append( tree.getRoot() );

	  for ( Edge edge : tree.getGraph().edgeSet() ) {
	      buf.append( edge );
	  }
      }

      buf.append( "---------" );

      for ( String predicate : predicates ) {
	  buf.append( "expr[ " + predicates.indexOf( predicate ) + " ]: " + predicate );
      }

      return buf.toString();
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
	  }
      }
  }


  private static String spacer( int depth ) {
      String pad = "";

      for (int i = 0; i < depth; i++) {
	  pad += " ";
      }

      return pad;
  }


  protected Vertex makeVertex( Element node, Integer depth, DirectedGraph<Vertex, Edge> graph ) {
      String pad = spacer( depth );
      String id = ( node ).getAttribute( "id" );
      Vertex vertex = new Vertex( id );
      graph.addVertex( vertex );

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

		  if ( node.hasAttribute( "score" ) ) {
		      String score = ( node ).getAttribute( "score" );
		      vertex.setScore( score );
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

      Integer predicate_id = predicates.indexOf( eval );

      return predicate_id;
  }


  protected Boolean[] evalTuple( String[] fields ) {
      // map from input tuple to forest predicate values

      Boolean[] pred = new Boolean[ predicates.size() ];
      int predicate_id = 0;

      for ( String predicate : predicates ) {
	  try {
	      Object[] param_values = new Object[ fields.length ];
	      String[] param_names = new String[ fields.length ];
	      Class[] param_types = new Class[ fields.length ];
	      int i = 0;

	      for ( String name : schema.keySet() ) {
		  param_values[ i ] = new Double( fields[ i ] );
		  param_names[ i ] = name;
		  param_types[ i ] = double.class;
		  i++;
	      }

	      ExpressionEvaluator ee = new ExpressionEvaluator( predicate, boolean.class, param_names, param_types, new Class[0], null );
	      Object res = ee.evaluate( param_values );
	      pred[ predicate_id ] = new Boolean( res.toString() );
	  } catch( CompileException e ) {
	      e.printStackTrace();
	  } catch( InvocationTargetException e ) {
	      e.printStackTrace();
	  } catch( ParseException e ) {
	      e.printStackTrace();
	  } catch( ScanException e ) {
	      e.printStackTrace();
	  }

	  predicate_id += 1;
      }

      return pred;
  }


  protected String tallyVotes( Boolean[] pred ) {
      HashMap<String, Integer> tally = new HashMap<String, Integer>();
      tally.put( "0", 0 );
      tally.put( "1", 0 );

      for ( Tree tree : forest ) {
	  String score = tree.traverse( pred );
	  tally.put( score, tally.get( score ) + 1 );
      }

      if ( tally.get( "1" ) >= tally.get( "0" ) ) {      
	  return "1";
      }
      else {
	  return "0";
      }
  }


  //////////////////////////////////////////////////////////////////////
  // TODO: refactor into unit tests

  public static void main( String[] argv ) throws Exception {
      String pmml_file = argv[0];
      RandomForest rf = new RandomForest( pmml_file );

      // evaluate the sample data from a TSV file

      String tsv_file = argv[1];
      eval_data( tsv_file, rf );
  }


  private static void eval_data( String tsv_file, RandomForest rf ) throws Exception {
      /* */
      System.out.println( rf );
      /* */

      FileReader fr = new FileReader( tsv_file );
      BufferedReader br = new BufferedReader( fr );
      String line;
      int count = 0;

      HashMap<String, Integer> confuse = new HashMap<String, Integer>();
      confuse.put( "TN", 0 );
      confuse.put( "TP", 0 );
      confuse.put( "FN", 0 );
      confuse.put( "FP", 0 );

      while ( ( line = br.readLine() ) != null ) {
	  if ( count++ > 0 ) {
	      // tally votes for each tree in the forest

	      String[] fields = line.split( "\\t" );
	      Boolean[] pred = rf.evalTuple( fields );
	      String score = rf.tallyVotes( pred );

	      // update tallies into the confusion matrix

	      if ( "1".equals( fields[ 0 ] ) ) {
		  if ( "1".equals( score ) ) {
		      confuse.put( "TP", confuse.get( "TP" ) + 1 );
		  }
		  else {
		      confuse.put( "FN", confuse.get( "FN" ) + 1 );
		  }
	      }
	      else {
		  if ( "0".equals( score ) ) {
		      confuse.put( "TN", confuse.get( "TN" ) + 1 );
		  }
		  else {
		      confuse.put( "FP", confuse.get( "FP" ) + 1 );
		  }
	      }
	  }
      }

      fr.close(); 
      System.out.println( confuse );
  }
}
