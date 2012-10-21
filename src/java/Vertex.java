public class Vertex
{
    public String id;
    public String score = null;


    public Vertex ( String id ) {
	this.id = id;
    }


    public void setScore ( String score ) {
	this.score = score;
    }


    public String getScore () {
	return score;
    }


    public String toString () {
	if ( score != null ) {
	    return id + ":" + score;
	}
	else {
	    return id;
	}
    }
}
