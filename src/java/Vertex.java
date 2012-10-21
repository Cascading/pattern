public class Vertex
{
    public String id;
    public String score;


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
	return id;
    }
}
