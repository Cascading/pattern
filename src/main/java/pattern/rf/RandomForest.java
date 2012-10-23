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
import pattern.ClassifierFactory;
import pattern.PatternException;
import pattern.XPathReader;

 
public class RandomForest extends Classifier implements Serializable
{
  public ArrayList<String> predicates = new ArrayList<String>();
  public ArrayList<Tree> forest = new ArrayList<Tree>();


  public RandomForest ( XPathReader reader ) throws PatternException {
      this.reader = reader;
      buildSchema();
      buildForest();
  }


  public String toString () {
      StringBuilder buf = new StringBuilder();

      buf.append( "---------" );
      buf.append( "\n" );
      buf.append( schema );
      buf.append( "\n" );
      buf.append( "---------" );
      buf.append( "\n" );
      buf.append( forest );
      buf.append( "\n" );
      buf.append( "---------" );
      buf.append( "\n" );

      for ( Tree tree : forest ) {
	  buf.append( tree );
	  buf.append( tree.getRoot() );

	  for ( Edge edge : tree.getGraph().edgeSet() ) {
	      buf.append( edge );
	  }

	  buf.append( "\n" );
      }

      buf.append( "---------" );
      buf.append( "\n" );

      for ( String predicate : predicates ) {
	  buf.append( "expr[ " + predicates.indexOf( predicate ) + " ]: " + predicate );
	  buf.append( "\n" );
      }

      return buf.toString();
    }


  protected void buildForest () throws PatternException {
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


  protected void buildNode( Element node, Vertex vertex, Integer depth, DirectedGraph<Vertex, Edge> graph ) throws PatternException {
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


  protected Integer makePredicate( Element node ) throws PatternException {
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
	  throw new PatternException( "unknown operator: " + operator );
      }

      if ( !predicates.contains( eval ) ) {
	  predicates.add( eval );
      }

      Integer predicate_id = predicates.indexOf( eval );

      return predicate_id;
  }


  public String classifyTuple( String[] fields ) {
    Boolean[] pred_eval = evalPredicates( fields );
    HashMap<String, Integer> votes = new HashMap<String, Integer>();
    String label = tallyVotes( pred_eval, votes );

    return label;
  }


  public Boolean[] evalPredicates( String[] fields ) {
      // map from input tuple to an array of predicate values for the forest

      Boolean[] pred_eval = new Boolean[ predicates.size() ];
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
	      pred_eval[ predicate_id ] = new Boolean( res.toString() );
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

      return pred_eval;
  }


  public String tallyVotes( Boolean[] pred_eval, HashMap<String, Integer> votes ) {
      String label = null;
      Integer winning_vote = 0;

      // tally the vote for each tree in the forest

      for ( Tree tree : forest ) {
	  label = tree.traverse( pred_eval );

	  if ( !votes.containsKey( label ) ) {
	      winning_vote = 1;
	  }
	  else {
	      winning_vote = votes.get( label ) + 1;
	  }

	  votes.put( label, winning_vote );
      }

      // determine the winning label

      for ( String key : votes.keySet() ) {
	  if ( votes.get( key ) > winning_vote ) {      
	      label = key;
	      winning_vote = votes.get( key );
	  }
      }

      return label;
  }


  //////////////////////////////////////////////////////////////////////
  // command line testing

  public static void main( String[] argv ) throws Exception {
      String pmml_file = argv[0];
      RandomForest model = (RandomForest) ClassifierFactory.getClassifier( pmml_file );

      // evaluate sample data from a TSV file

      String tsv_file = argv[1];
      eval_data( tsv_file, model );
  }


  public static void eval_data( String tsv_file, RandomForest model ) throws Exception {
      System.out.println( model );

      FileReader fr = new FileReader( tsv_file );
      BufferedReader br = new BufferedReader( fr );
      String line;
      int count = 0;

      while ( ( line = br.readLine() ) != null ) {
	  if ( count++ > 0 ) {
	      // compare classifier label vs. predicted for each line in the TSV file

	      String[] fields = line.split( "\\t" );
	      String predicted = fields[ fields.length - 1 ];

	      System.out.println( line );

	      Boolean[] pred_eval = model.evalPredicates( fields );
	      HashMap<String, Integer> votes = new HashMap<String, Integer>();
	      String label = model.tallyVotes( pred_eval, votes );

	      if ( !predicted.equals( label ) ) {
		  System.err.println( "regression: classifier label does not match [ " + label + " ]" );
		  System.exit( -1 );
	      }

	      System.out.println( "label: " + label + " votes: " + votes );
	  }
      }

      fr.close(); 
  }
}
