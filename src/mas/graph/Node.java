package mas.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import env.Attribute;

public class Node implements Comparable, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4070241869411316876L;
	private String id;
	private ArrayList<Node> neighbours; //TODO 26.2. : ArrayList<String> changed to ArrayList<Node> - do all the modifications in related functions! 
	private List<Attribute> contentList; //TODO 21.2. : trouver les types 
	private boolean visited;
	private long time;

	public Node(String id, ArrayList<Node> neighbours, List<Attribute> contentList, boolean visited){
		this.id = id;
		this.neighbours = neighbours;
		this.contentList = contentList;
		this.visited = visited;
		this.time=System.currentTimeMillis();
		
	}
        
        public String toString(){
            return id;
        }
        
	public String getId(){
		return id;
	}
	
	public ArrayList<Node> getNeighbours(){
		return neighbours;
	}
	
	public List<Attribute> getContentList(){
		return contentList;
	}
	
	public boolean getVisited(){
		return visited;
	}
	
	public long getTime()
	{
		return time;
	}
	
	public void setVisited(boolean visited){
		this.visited = visited;
		updateTime();
	}
	
	public void addNeighbours(ArrayList<Node> list){
		for(Node neighbour : list){
			addNeighbour(neighbour);
		}
	}
	
	public void addNeighbour(Node neighbour){
		if (!neighbours.contains(neighbour)){
			neighbours.add(neighbour);
			neighbour.addNeighbour(this); //TODO: 13.3. tell to Yasmine
		}
		updateTime();
		//Collections.sort(neighbours); //normaly we should not nedd this function
	}

	public void clearNeighbours() {
		this.neighbours.clear();
	}
	
	private void updateTime()
	{
		this.time=System.currentTimeMillis();
	}
	
		
	public void setContent(List<Attribute> contentList)
	{
		this.contentList=contentList;
		updateTime();
	}

	@Override
	public int compareTo(Object node2) {
		// TODO Auto-generated method stub
		return this.getId().compareTo(((Node)node2).getId());
	}
        
        public Node clone(){
            return new Node(this.id, this.neighbours, this.contentList, this.visited);
        }
}
