package mas.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import env.Attribute;

public class Graph implements Serializable{



	/**
	 * 
	 */
	private static final long serialVersionUID = -157157695933610988L;
	private ArrayList<Node> graph;
	private long latest_update;


	public Graph()
	{
		graph=new ArrayList<>();
		latest_update = System.currentTimeMillis();
	}

	public ArrayList<Node> getAllNodes(){
		return graph;
	}


	public int getNodeIndex(String id)
	{
		for(int i=0; i< graph.size(); i++){
			if(graph.get(i).getId().equalsIgnoreCase(id)){
				return i;
			}
		}

		return -1 ; //this node hasn't been added to the graph yet
	}

	public boolean isInGraph(String id){
		for(Node node: graph){
			if(node.getId().equalsIgnoreCase(id))
				return true;
		}
		return false;
	}

	public void addNode(Node node){
		if(!isInGraph(node.getId())){
			graph.add(node);
		}
	}

	public int size(){
		return graph.size();
	}


	public Node getNode(int index)
	{
		return graph.get(index);
	}

	public Node getNode(String id){
		for(Node node: graph){
			if(node.getId().equalsIgnoreCase(id)){
				return node;
			}
		}
		return null;
	}
	
	
	public ArrayList<Node> getUnvisitedNodes(){
		ArrayList<Node> unvisited_nodes = new ArrayList<>();
		
		for(Node node : graph){
			if(!node.getVisited()){
				unvisited_nodes.add(node);
			}
		}
		
		return unvisited_nodes;
	}
	
	public Node getRandomUnvisited(){
		
		ArrayList<Node> unvisited_nodes = getUnvisitedNodes();
		if(unvisited_nodes.size()==0){
			return null;
		}
		
		Random r= new Random();
		int moveId=r.nextInt(unvisited_nodes.size());
		
		return unvisited_nodes.get(moveId);

	}
	
	
	
	/**
	 * @params currentNode : the node in which we are starting
	 * @return : the nearest not visited node from the currentNode. Returns null if all nodes have been already visited.            
	 */
	public Node getClosestUnvisited(Node currentNode){
		//toExplore is a FIFO to perform our BFS = parcours en largeur
		//at the beginning, we put each neighbour of the currentNode (= initial node) to the FIFO
		ArrayList<Node> toExplore = (ArrayList<Node>) currentNode.getNeighbours().clone(); 

		//The maintenance of the father relation - the key (1st element) 
		//is the node and the value (2nd element) is its father node on the path
		//we use it for the path reconstruction
		HashMap<Node, Node> cameFrom = new HashMap<Node, Node>();

		//the currentNode does not have a father, and it is the father of each of its neighbours	
		cameFrom.put(currentNode, null);
		for(Node node: toExplore){
			cameFrom.put(node, currentNode);
		}

		//while the FIFO is not empty (i.e. there is a node we didn't take into account in our BFS)	
		while(!toExplore.isEmpty()){
			//we pop up the first element of the FIFO    
			Node newVisited = toExplore.remove(0);

			//if this element is an unvisited node, we return it 
			//in fact, this function is not very useful - usually we don't need the node but the path to it
			if(!newVisited.getVisited()){
				return newVisited;
			}
			//else: we add all its neighbours to the FIFO if they aren't yet there, and 
			//for eachneighbour, we precise that its father is newVisited
			for(Node neighbour: newVisited.getNeighbours()){
				if(!cameFrom.containsKey(neighbour) && !toExplore.contains(neighbour)){
					toExplore.add(neighbour);
					cameFrom.put(neighbour, newVisited);
				}
			}
		}

		//else : all nodes have already been visited, there is nothing to visit
		return null;
	}

	/**
	 *  @param Node finalNode : the node we want to go into
	 *  @param HashMap<Node, Node> cameFrom : the key is the node, the value is its father in the arborescence 
	 *  @return ArrayList<Node> path: return the path from the initial node to the finalNode	
	 */
	private ArrayList<Node> pathReconstruction(Node finalNode, HashMap<Node, Node> cameFrom){

		ArrayList<Node> path = new ArrayList<>();
		path.add(finalNode);
		//System.out.println("The nearest node: "+finalNode.toString());

		//we find the father of the final node	
		Node father = cameFrom.get(finalNode);

		//while the node has a father, we can "remote the path"
		//the only node without the father is the initial node
		while(father != null){
			//System.out.println("Father: "+father.toString());

			//we add the father at the beginning of the path, as we are remoting the path     
			path.add(0, father);
			//and we find its father...    
			father = cameFrom.get(father);
		}

		return path;
	}

	/**
	 * @params currentNode : the node in which we are starting
	 * @return : the path (AttayList<Node>) to the nearest not visited node. Returns null if every node has been already visited.
	 */
	public ArrayList<Node> getPathToClosestUnvisited(Node currentNode){
		//toExplore is the FIFO of nodes to explore
		//System.out.println(currentNode.getNeighbours().toString());
		ArrayList<Node> toExplore = (ArrayList<Node>) currentNode.getNeighbours().clone(); 
		//the father relation: the first composant represents the node we have reached, the second represents its father on the path
		HashMap<Node, Node> cameFrom = new HashMap<Node, Node>();

		//the node in which we are starting doesn't have any father
		cameFrom.put(currentNode, null);

		//for each neighbour of currentNode, currentNode is its father
		for(Node node: toExplore)    
			cameFrom.put(node, currentNode);


		//while the FIFO is not empty (i.e. there is a node we didn't take into account in our BFS)	
		while(!toExplore.isEmpty()){
			//we pop up the first element of the FIFO
			Node newVisited = toExplore.remove(0);

			//if this element is an unvisited node, we return the path to it
			if(!newVisited.getVisited()){
				//System.out.println(cameFrom.toString());
				return pathReconstruction(newVisited, cameFrom);
			}
			//else:
			for(Node neighbour: newVisited.getNeighbours()){
				if(!toExplore.contains(neighbour) && !cameFrom.containsKey(neighbour)){
					toExplore.add(neighbour);
					cameFrom.put(neighbour, newVisited);
				}
			}
		}

		//else : all nodes have already been visited
		return null;
	}

	public void printNodes()
	{
		System.out.println("***********List of the "+graph.size()+" known Nodes : ");
		for(int i=0; i<graph.size(); i++)
		{
			System.out.println("Node id : "+graph.get(i).getId()+" --- visited : "+graph.get(i).getVisited()+"\nList of neighbours : "+graph.get(i).getNeighbours().toString()+"\n Treasor value "+graph.get(i).getContentList().toString());
		}
	}

	public void fusion(Graph graph2)
	{

		for(Node node2 : graph2.getAllNodes())//loop for adding the new nodes
		{

			int index = getNodeIndex((node2.getId()));
			if(index == -1) //node doesn't exist in the graph
			{
				Node myNode = new Node(node2.getId(), new ArrayList<Node>(), node2.getContentList(), node2.getVisited());
				//graph.add(node2.clone()); // WARNinG NEIGHBOUURSSS pointeurs
				graph.add(myNode);

				for(Node neighbour: node2.getNeighbours()) {

					int i = getNodeIndex(neighbour.getId());
					//System.out.println("==========node : "+getNode(node2.getId()).getId()+" neighbour :::: "+neighbour.getId()+" index is ======= "+i);
					if(i != -1) { //the node exists in the original graph 
						//getNode(node2.getId()).addNeighbour(getNode(i)); //we add it as a neighbour of if
						myNode.addNeighbour(getNode(i));
					}
					//else : we do nothing
				}
			}

		}

		for(Node node2 : graph2.getAllNodes()) //loop for adding/updating the neighbours pointers
		{	
			int index = getNodeIndex((node2.getId()));
			if(!graph.get(index).getVisited()) //node has not been already visited so not all the neighoburs are set//   
			{
				if(node2.getVisited())
				{
					graph.get(index).setContent(node2.getContentList());

					graph.get(index).setVisited(true);
					//adding neighbours
					for(Node neighbour2 : node2.getNeighbours()) // TODO : modification de addneighbour pour ajouter deux arcs : node - neighobur et neighobur-nde
					{
						int index_neigh = getNodeIndex(neighbour2.getId());
						//if(index != -1) //the node of neighbour2 already exists
						//{

						graph.get(index).addNeighbour(graph.get(index_neigh));
						//}

					}
				}

			}
			else // we visited the node
			{
				if(node2.getVisited() && graph.get(index).getTime() < node2.getTime()) //content may have been changed 
				{
					graph.get(index).setContent(node2.getContentList());
				}
			}

		}

	}

	public Node getBestNode(Node currentNode, String treasureType, int freeSpace){
		Node bestNode = null;
		printNodes();
		double bestValue = 0; //defined as treasure value/distance
		for(Node node : graph){
			if(!node.equals(currentNode))
			{
				double nodeValue = getUtility(currentNode, node, treasureType, freeSpace); 
				System.out.println("------->>Node : "+node.getId()+" value is : "+nodeValue);
				if(nodeValue != 0 && (bestNode == null || bestValue < nodeValue)){
					bestNode = node;
					bestValue = nodeValue;
//					System.out.println("!!!!! new best node : "+node.getId()+" with value :: "+nodeValue);
				}
			}
		}
		return bestNode;
	}
	
	
	public double getUtility(Node currentNode, Node goalNode, String treasureType, int freeSpace){ //utility = (freeSpace - treasureValue)/distance
//		System.out.println(goalNode.getTreasureValue(treasureType));
//		System.out.println(">>>current "+currentNode.getId());
//		System.out.println(">>>goal : "+goalNode);
//		System.out.println(getPath(currentNode, goalNode));
		int difference = freeSpace - goalNode.getTreasureValue(treasureType) + 1;
		System.out.println("difference is = "+difference);
		if(difference == freeSpace + 1)
		{
			return 0;
		}
		
		if(difference < 1){
			difference = freeSpace - 1;
		}
		return difference/getPath(currentNode, goalNode).size();
	}
	
	public ArrayList<Node> getPath(Node currentNode, Node goalNode){
		//toExplore is a FIFO to perform our BFS = parcours en largeur
		//at the beginning, we put each neighbour of the currentNode (= initial node) to the FIFO
		ArrayList<Node> toExplore = (ArrayList<Node>) currentNode.getNeighbours().clone(); 

		//The maintenance of the father relation - the key (1st element) 
		//is the node and the value (2nd element) is its father node on the path
		//we use it for the path reconstruction
		HashMap<Node, Node> cameFrom = new HashMap<Node, Node>();

		//the currentNode does not have a father, and it is the father of each of its neighbours	
		cameFrom.put(currentNode, null);
		for(Node node: toExplore){
			cameFrom.put(node, currentNode);
		}

		//while the FIFO is not empty (i.e. there is a node we didn't take into account in our BFS)	
		//while(!toExplore.isEmpty()){
		while(!toExplore.isEmpty()){	
			//we pop up the first element of the FIFO    
			Node newNode = toExplore.remove(0);

			//if this element is an unvisited node, we return it 
			//in fact, this function is not very useful - usually we don't need the node but the path to it
			if(newNode.equals(goalNode)){
				return pathReconstruction(newNode, cameFrom);
			}
			//else: we add all its neighbours to the FIFO if they aren't yet there, and 
			//for eachneighbour, we precise that its father is newVisited
			for(Node neighbour: newNode.getNeighbours()){
				if(!cameFrom.containsKey(neighbour) && !toExplore.contains(neighbour)){
					toExplore.add(neighbour);
					cameFrom.put(neighbour, newNode);
				}
			}
		}

		//else : all nodes have already been visited, there is nothing to visit
		return null;
	}

	public boolean isCompleted() {
		if(graph.size()==0){
			return false;
		}
		for(Node node : graph){
			if(!node.getVisited()){
				return false;
			}
		}
		return true;
	}

	
	public void sort(){
		graph.sort(new Comparator<Node>() {

			@Override
			public int compare(Node node1, Node node2) {
				
				return node1.compareTo(node2);
			}
		});
	}
	
//	public Node getSiloPosition() {
//		int position = graph.size()/2 - 1;
//		String id[] = graph.get(0).getId().split(",");
//		System.out.println(">>>>>>>>>>>position is : "+position);
//		if(id.length==1){ // id i
//			return getNode(Integer.toString(position));
//		}
//		else //matrix of id i_j 
//		{
//			return getNode(Integer.toString(position)+"_"+Integer.toString(position));
//		}
//		
//	}
	
	public Node getMeetingPosition(){
		sort();
		Node meetingNode = null;
		double bestValue = 0;
		
		for(Node node : graph){
			if(node.getContentList().isEmpty())
			{
				double value = node.getNeighbourhoodValue(new ArrayList<Node>(), 1);
//				System.out.println(">>>Meeting pos"+node.getId()+" final value is ::::::::::: "+value);
				if(meetingNode == null || value > bestValue){
					meetingNode = node;
					bestValue = value;
				}
			}
		}
		
		return meetingNode;
		
	}
	public ArrayList<Node> getPathToGivenNode(Node currentNode, String finalNodeId){
      //toExplore is the FIFO of nodes to explore
      //System.out.println(currentNode.getNeighbours().toString());
      ArrayList<Node> toExplore = (ArrayList<Node>) currentNode.getNeighbours().clone(); 
      //the father relation: the first composant represents the node we have reached, the second represents its father on the path
      HashMap<Node, Node> cameFrom = new HashMap<Node, Node>();
      
      //the node in which we are starting doesn't have any father
      cameFrom.put(currentNode, null);
      
      //for each neighbour of currentNode, currentNode is its father
      for(Node node: toExplore)    
          cameFrom.put(node, currentNode);
      
      
      //while the FIFO is not empty (i.e. there is a node we didn't take into account in our BFS)	
      while(!toExplore.isEmpty()){
          //we pop up the first element of the FIFO
      	Node newVisited = toExplore.remove(0);
          
      	//if we have found the node we are looking for :
          if(newVisited.getId().equalsIgnoreCase(finalNodeId)){
              //System.out.println(cameFrom.toString());
              return pathReconstruction(newVisited, cameFrom);
          }
          //else:
          for(Node neighbour: newVisited.getNeighbours()){
              if(!toExplore.contains(neighbour) && !cameFrom.containsKey(neighbour)){
                  toExplore.add(neighbour);
                  cameFrom.put(neighbour, newVisited);
              }
          }
      }
      
      //else : all nodes have already been visited
      return null;
  }


}
