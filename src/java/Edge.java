import org.jgrapht.graph.DefaultEdge;


public class Edge
    extends DefaultEdge
{
    public Integer predicate_id;


    public void setPredicateId ( Integer predicate_id ) {
	this.predicate_id = predicate_id;
    }


    public Integer getPredicateId () {
	return predicate_id;
    }

    
    public String toString () {
	String base = super.toString();

	return base + ":" + predicate_id;
    }
}
