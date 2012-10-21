import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.DirectedGraph;

 
public class Tree
{
  protected String tree_name;
  protected Vertex root;
  protected DirectedGraph<Vertex, Edge> graph = new DefaultDirectedGraph<Vertex, Edge>(Edge.class);


  public Tree ( String id ) throws Exception {
      tree_name = "tree_" + id;

      /* */
      System.out.println( tree_name );
      /* */
  }


  public void setRoot ( Vertex root ) {
      this.root = root;
  }


  public Vertex getRoot () {
      return root;
  }


  public DirectedGraph<Vertex, Edge> getGraph () {
      return graph;
  }


  public String toString () {
      return tree_name + ": " + graph;
  }


  public void traverse () {
      traverseVertex( root );
  }


  protected String traverseVertex ( Vertex vertex ) {
      String score = vertex.getScore();

      if ( score != null ) {
	  System.out.println( "  then " + score );
	  return null; // score
      }

      for ( Edge edge : graph.outgoingEdgesOf( vertex ) ) {
	  System.out.println( edge );
	  System.out.println( " if pred[ " + edge.getPredicateId() + " ]:" );

	  score = traverseVertex( graph.getEdgeTarget( edge ) );

	  if ( score != null ) {
	      return score;
	  }
      }

      return null;
  }
}
