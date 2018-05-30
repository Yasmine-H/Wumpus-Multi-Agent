package mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import env.Attribute;
import env.Couple;
import jade.core.behaviours.SimpleBehaviour;
import mas.abstractAgent;
import mas.agents.Constants;
import mas.graph.Graph;
import mas.graph.Node;

public class CollectorWalkBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4023577769135483172L;
	private Graph graph;
	private String treasureType;
	private StringBuilder moveTo;
	private boolean fullyExplored = false;
	private StringBuilder previousState;
	private boolean moved = false;
	
	
	
	public CollectorWalkBehaviour (final mas.abstractAgent myagent, Graph graph, StringBuilder moveTo, StringBuilder previousState) {
		super(myagent);
		this.graph = graph;
		this.treasureType = ((mas.abstractAgent)this.myAgent).getMyTreasureType();
		this.moveTo = moveTo;
		this.previousState = previousState;
	}
	
	@Override
	public void action() {
		System.out.println(myAgent.getLocalName()+"************************CollectorWalkBehaviour****************************");
		
		System.out.println(myAgent.getLocalName()+" : My current backpack capacity is:"+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
		System.out.println(myAgent.getLocalName()+" : My type is : "+((mas.abstractAgent)this.myAgent).getMyTreasureType());
		
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();
		

		if (myPosition!=""){
		
			// empty backpack if possible 
			System.out.println("result of empty : "+((mas.abstractAgent)this.myAgent).emptyMyBackPack("Silo"));
			System.out.println(myAgent.getLocalName()+" : the remaining backpack capacity after calling to empty is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
			
		
			List<Couple<String,List<Attribute>>> lobs=((mas.abstractAgent)this.myAgent).observe();
			
			if(moveTo.toString().equals(""))
			{
				if(!fullyExplored){ // the agent explores the graph and collects when possible
					explorationWalk(lobs);
				}
				
				else{ // the agent collects the treasures
					collectionWalk(lobs);
				}
			}
			else{
				String id = lobs.get(0).getLeft();
				System.out.println(" ................ MOVE TO : "+moveTo);
				System.out.println(graph.getNode(id));

				ArrayList<Node> pathToMoveTo = graph.getPathToGivenNode(graph.getNode(id), moveTo.toString());
				System.out.println(pathToMoveTo);
				try {
					if(pathToMoveTo.get(0).getId().equalsIgnoreCase(id)) {
						pathToMoveTo.remove(graph.getNode(id));
					}
					if(pathToMoveTo.size() == 1) { //we can reach directly the desired state
						moved = ((mas.abstractAgent)this.myAgent).moveTo(moveTo.toString());
						//moveTo.replace(0, moveTo.length(), "");
					}
					else {
						moved = ((mas.abstractAgent)this.myAgent).moveTo(pathToMoveTo.get(0).getId());
					}
				}
				catch(Exception e) {
					if(!moveTo.toString().equalsIgnoreCase(graph.getNode(id).getId()))
						moved = false;
					else
						moved = true;
				}
			}
		
		}
	
		
	}

	private void collectionWalk(List<Couple<String, List<Attribute>>> lobs) {
	
		System.out.println(myAgent.getLocalName()+"************************CollectionWalk****************************");
		
		String id = lobs.get(0).getLeft();
		List<Attribute> lattribute= lobs.get(0).getRight();
		Node currentNode = graph.getNode(id);
		
		if(((mas.abstractAgent)this.myAgent).getBackPackFreeSpace() > 0)
		{
			//collect the treasure
		
			boolean b = collectTreasure(lattribute);
		
			//update graph
			if(b){
			List<Couple<String,List<Attribute>>> new_lobs=((mas.abstractAgent)this.myAgent).observe(); 
			List<Attribute> newContentList = new_lobs.get(0).getRight();
			currentNode.setContent(newContentList);
			}
		}
		//next move planification	
		Node goalNode;
		if(((mas.abstractAgent)this.myAgent).getBackPackFreeSpace() < 10){
			goalNode = graph.getSiloPosition(); //we must empty the backpack
		}
		else //there is enough space
		{
			goalNode = graph.getBestNode(currentNode, treasureType, ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
		
			
			
			if(goalNode == null){ //if there is no more treasure to take, we go back to the silo
				goalNode = graph.getSiloPosition();
				System.out.println("no more treasure :(");
			}
			else
			{
				System.out.println("The best node is : "+goalNode.getId());
			}
		}
		ArrayList<Node> path = graph.getPath(currentNode, goalNode);
		
		
		if(path!=null){
			if(path.get(0).getId().equalsIgnoreCase(id)){ //if the first node of the path is the current node, which is normally the case 
				//TODO 26.2.: it shouldn't be the case, it is not very proper like this!
				path.remove(0);
			}
			moveTo = moveTo.replace(0, moveTo.length(),path.get(0).getId());
			moved = ((mas.abstractAgent)this.myAgent).moveTo(moveTo.toString()); //we visit the first next node on the path
			System.out.println(myAgent.getLocalName()+"!!!!!!!!!!!!!!!!!!MOVING TOOOOOO :: "+moveTo+" bc bestNode is : "+goalNode.getId()+
					"\n and complete path is "+path.toString());
		}
		else{ 
			System.out.println(myAgent.getLocalName()+" ERROR path to "+goalNode+" is null :o");
			
		}
		
	}

	
	
	
	public void explorationWalk(List<Couple<String, List<Attribute>>> lobs){
		
		System.out.println(myAgent.getLocalName()+"************************ExplorationWalk****************************");

		System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);

		String id = lobs.get(0).getLeft();

		// saving the neighbours
		ArrayList<String> listNeighboursId = new ArrayList<>();
		ArrayList<Node> listNeighbours = new ArrayList<>(); //TODO: added 26.2. just to fix bugs and can perform tests, to treat properly [1]

		for(int i=1; i<lobs.size(); i++){
			listNeighboursId.add(lobs.get(i).getLeft());
			if(!graph.isInGraph(lobs.get(i).getLeft())){ //TODO 26.2: cf [1]
				graph.addNode(new Node(lobs.get(i).getLeft(), new ArrayList<Node>(), new ArrayList<Attribute>(), false)); //TODO 26.2: cf [1]
			}
			listNeighbours.add(graph.getNode(lobs.get(i).getLeft())); //TODO 26.2: cf [1]
		}

		System.out.println("Before creating the node, the neighbours are : "+listNeighboursId.toString());
		
		//add node to the graph or update it if it already exists
		if(!graph.isInGraph(id)){ //check if the node already exists in the graph 
			graph.addNode(new Node(id, listNeighbours, lobs.get(0).getRight(), true));
			//System.out.println("New node added, the neighbours are : "+graph.getNode(id).getNeighbours().toString());
		}
		else{
			if(!graph.getNode(id).getVisited()){
				graph.getNode(id).setVisited(true);
				graph.getNode(id).addNeighbours(listNeighbours);
				graph.getNode(id).setContent((List<Attribute>)lobs.get(0).getRight());  
				//TODO 26.2.: setContent
				//TODO 26.2.: store somewhere the node instead of repeating all the time graph.getNode(id)
			}
		}

		//Finally, we add the explored node to the list of neighbours of its each neighbour
		for(Node neighbour: listNeighbours){
			neighbour.addNeighbour(graph.getNode(id));
		}


		//list of attribute associated to the currentPosition
		List<Attribute> lattribute= lobs.get(0).getRight();

		//collect the treasure
		boolean b = collectTreasure(lattribute);
		
		//update the graph if the agent picked (part of) the treasure
		if (b){
			List<Couple<String,List<Attribute>>> new_lobs=((mas.abstractAgent)this.myAgent).observe(); 
			List<Attribute> newContentList = new_lobs.get(0).getRight();
			graph.getNode(id).setContent(newContentList);
		}
		
		//next move plan
		ArrayList<Node> pathToTheClosest = graph.getPathToClosestUnvisited(graph.getNode(id));
		if(pathToTheClosest == null){
			System.out.println("The graph has been fully explored ! List of nodes : \n");
			fullyExplored = true ;
			graph.sort();
			//graph.printNodes();
		}
		else{
			if(pathToTheClosest.get(0).getId().equalsIgnoreCase(id)){ //if the first node of the path is the current node, which is normally the case 
				//TODO 26.2.: it shouldn't be the case, it is not very proper like this!
				pathToTheClosest.remove(0);
				//System.out.println("Exploration de "+myAgent.getLocalName());
				//graph.printNodes();
				moveTo = moveTo.replace(0, moveTo.length(),pathToTheClosest.get(0).getId());
				moved = ((mas.abstractAgent)this.myAgent).moveTo(moveTo.toString()); //we visit the first next node on the path
				
				
			}
		}

	}
	
	public boolean collectTreasure(List<Attribute> lattribute){
		
		boolean b = false;
		
		for(Attribute a:lattribute){
			System.out.println(myAgent.getLocalName()+" : Type of the treasure on the current position: "+a.getName());
			System.out.println(myAgent.getLocalName()+" : Value of the treasure on the current position: "+a.getValue());
			if(a.getName().equals(treasureType)) // same treasure type 
			{
				System.out.println(myAgent.getLocalName()+" : The agent grabbed :"+((mas.abstractAgent)this.myAgent).pick());
				System.out.println(myAgent.getLocalName()+" : the remaining backpack capacity is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
				//Empty backpack if silo agent nearby
				((mas.abstractAgent)this.myAgent).emptyMyBackPack("Silo");
				System.out.println(myAgent.getLocalName()+" : the remaining backpack capacity after calling to empty is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
				
				b = true;
				/*
				if((int) a.getValue()<((mas.abstractAgent)this.myAgent).getBackPackFreeSpace()) //enough space in the backpack
				{
					//((mas.abstractAgent)this.myAgent).pick();
					//Node node = graph.getNode(lobs.get(0).getLeft());
					//node.setContent(contentList); TODO 13.4.2018 : update content
					//System.out.println(myAgent.getLocalName()+" : The agent grabbed :"+((mas.abstractAgent)this.myAgent).pick());
					//System.out.println(myAgent.getLocalName()+" : the remaining backpack capacity is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
					//System.out.println(myAgent.getLocalName()+" : The value of treasure on the current position: (unchanged before a new call to observe()): "+a.getValue());
					
				}
				else // not enough space
				{
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!Not enough place in the backpack :(");
				}*/
			}
			else // not same treasure type
			{
				System.out.println(myAgent.getLocalName()+" not my type :(");
			}

		}
		
		return b;

	}
	
	@Override
	public boolean done() {
		
		try {
			System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.previousState.replace(0, this.previousState.length(), Constants.STATE_START_INTERBLOCAGE);
		
		return true;
	}
	
	public int onEnd(){
		if(moved) {
			if(moveTo.toString().equalsIgnoreCase(((abstractAgent) myAgent).getCurrentPosition())) {
				moveTo.replace(0, moveTo.length(), "");
			}
			//
			return Constants.MOVED;
		}
		else {
			return Constants.BLOCKED;
		}
	}

}
