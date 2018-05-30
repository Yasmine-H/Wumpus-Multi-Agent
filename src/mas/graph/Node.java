//<<<<<<< HEAD
//package mas.graph;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//
//import env.Attribute;
//
//public class Node implements Comparable, Serializable{
//	
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 4070241869411316876L;
//	private String id;
//	private ArrayList<Node> neighbours; //TODO 26.2. : ArrayList<String> changed to ArrayList<Node> - do all the modifications in related functions! 
//	private List<Attribute> contentList; //TODO 21.2. : trouver les types 
//	private boolean visited;
//	private long time;
//
//	public Node(String id, ArrayList<Node> neighbours, List<Attribute> contentList, boolean visited){
//		this.id = id;
//		this.neighbours = neighbours;
//		this.contentList = contentList;
//		this.visited = visited;
//		this.time=System.currentTimeMillis();
//		
//	}
//        
//        public String toString(){
//            return id;
//        }
//        
//	public String getId(){
//		return id;
//	}
//	
//	public ArrayList<Node> getNeighbours(){
//		return neighbours;
//	}
//	
//	public List<Attribute> getContentList(){
//		return contentList;
//	}
//	
//	public boolean getVisited(){
//		return visited;
//	}
//	
//	public long getTime()
//	{
//		return time;
//	}
//	
//	public void setVisited(boolean visited){
//		this.visited = visited;
//		updateTime();
//	}
//	
//	public void addNeighbours(ArrayList<Node> list){
//		for(Node neighbour : list){
//			addNeighbour(neighbour);
//		}
//	}
//	
//	public void addNeighbour(Node neighbour){
//		if (!neighbours.contains(neighbour)){
//			neighbours.add(neighbour);
//			neighbour.addNeighbour(this); //TODO: 13.3. tell to Yasmine
//		}
//		updateTime();
//		//Collections.sort(neighbours); //normaly we should not nedd this function
//	}
//
//	public void clearNeighbours() {
//		this.neighbours.clear();
//	}
//	
//	private void updateTime()
//	{
//		this.time=System.currentTimeMillis();
//	}
//	
//		
//	public void setContent(List<Attribute> contentList)
//	{
//		this.contentList=contentList;
//		updateTime();
//	}
//
//	@Override
//	public int compareTo(Object node2) {
//		// TODO Auto-generated method stub
//		return this.getId().compareTo(((Node)node2).getId());
//	}
//        
//        public Node clone(){
//            return new Node(this.id, this.neighbours, this.contentList, this.visited);
//        }
//}
//=======
package mas.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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

	public boolean isBetterThan(Node node2, String treasureType) {

		if(node2.getContentList().isEmpty()){ // the other node has no content (no treasure nor diamonds)
			return true;
		}

		if(this.contentList.isEmpty()){ // the current node has no content while the other node has
			return false;
		}

		//both have some content
		int myContent = 0;
		int node2Content = 0;

		for(Attribute content : getContentList())
		{
			if(content.getName().equals(treasureType)){
				myContent = (int)content.getValue();
				break;
			}
		}
		for(Attribute content : node2.getContentList())
		{
			if(content.getName().equals(treasureType)){
				node2Content = (int)content.getValue();
				break;
			}
		}

		return (myContent >= node2Content);
	}

	
	
	public int getTreasureValue(String treasureType) {

		for(Attribute content : getContentList())
		{
			if(content.getName().equals(treasureType)){
				return (int)content.getValue();
				
			}
		}
		
		return 0;
	}

	
	
	

	public boolean hasChanged(List<Attribute> newContentList){


		List<Attribute> myContentList = getContentList();


		//they don't have the same size, so one of them has more treasure types than the other
		if(myContentList.size()!=newContentList.size()){
			return true;
		}

		//they have the same treasure types, we must compare their value
		for(Attribute myContent : myContentList)
		{
			for(Attribute newContent : newContentList)
			{
				if(myContent.getName().equals(newContent.getName())){
					if(myContent.getValue()!=newContent.getValue()){
						return true;
					}
					else{
						break;
					}
				}
			}
		}

		return false;

	}
	/*
	public ArrayList<Node> getShortestPathTo(Node goalNode){
		ArrayList<Node> neighbourList = getNeighbours();
		HashMap<Node, Node> node_parent_Start = new HashMap<Node, Node>();
		HashMap<Node, Node> node_parent_End = new HashMap<Node, Node>();
		
		node_parent_Start.put(this, null);
		node_parent_End.put(null, goalNode);
		
		ArrayList<Node> toExplore = (ArrayList<Node>) getNeighbours().clone(); 

		
		while(!node_parent_Start.containsValue(goalNode)){
			
			for(Node node : )
		}
	}
	*/

	public int getNeighbourhoodValue(List<Node> considered) {
		considered.add(this);
		int nb_neighbours = getNeighbours().size();
		if(nb_neighbours < 2){
//			System.out.println(this.getId()+" : value : 0");
			return 0;
		}
		
		else{
			if(nb_neighbours == 2){
//				System.out.println(this.getId()+" : value : 1");
				return 1;
			}
			else{
				
				
				int value = nb_neighbours;
				if(considered.size()==1){
					value *= 2;
//					System.out.println(getId()+" is a parent : "+value);
				}
				
				for(Node neighbour : getNeighbours()){
					if(!considered.contains(neighbour))
					{
						value += neighbour.getNeighbourhoodValue(considered)-1;
					}
				}
				
//				System.out.println(this.getId()+" : value : "+value);
				return value;
			}
		}
	}
}
//>>>>>>> master
