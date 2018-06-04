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
	private StringBuilder moveToNext;
	private boolean fullyExplored = false;
	private StringBuilder previousState;
	private boolean moved = false;
	private StringBuilder moveToGoal;
	private boolean missionCompleted = false;
//	private boolean noTreasure = false;
	private boolean goingToMeetingPoint = false;
	
	
	public CollectorWalkBehaviour (final mas.abstractAgent myagent, Graph graph, StringBuilder moveTo, StringBuilder moveToGoal, StringBuilder previousState) {
		super(myagent);
		this.graph = graph;
		this.treasureType = ((mas.abstractAgent)this.myAgent).getMyTreasureType();
		this.moveToNext = moveTo;
		this.previousState = previousState;
		this.moveToGoal = moveToGoal;
	}
	
	@Override
	public void action() {
		System.out.println(myAgent.getLocalName()+"************************CollectorBehaviour****************************"+fullyExplored);
		
		System.out.println(myAgent.getLocalName()+" : My current backpack capacity is:"+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
		System.out.println(myAgent.getLocalName()+" : My type is : "+((mas.abstractAgent)this.myAgent).getMyTreasureType());
		System.out.println(myAgent.getLocalName()+" : My old goal is : "+moveToGoal.toString());
		
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();
		
		if (myPosition!=""){
			
			// empty backpack if possible 
			System.out.println("result of empty : "+((mas.abstractAgent)this.myAgent).emptyMyBackPack("Tank"));
			System.out.println(myAgent.getLocalName()+" : the remaining backpack capacity after calling to empty is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
			
		
			List<Couple<String,List<Attribute>>> lobs=((mas.abstractAgent)this.myAgent).observe();
			String id = lobs.get(0).getLeft();
			
			//list of attribute associated to the currentPosition
			List<Attribute> lattribute= lobs.get(0).getRight();

			missionCompleted(graph.getNode(id));
			
			if(goingToMeetingPoint && ((mas.abstractAgent)this.myAgent).emptyMyBackPack("Tank")){
				goingToMeetingPoint = false;
				moveToGoal.replace(0, moveToGoal.length(), "");
			}

			if(!fullyExplored){ // the agent explores the graph and collects when possible
				updateGraph(lobs);
			}
			else
			{
				updateNodeContent(id);
			}

			//collect the treasure
			if(((mas.abstractAgent)this.myAgent).getBackPackFreeSpace() != 0)
			{
				boolean b = collectTreasure(lattribute);
				
				//update the graph if the agent picked (part of) the treasure
				if (b){
					System.out.println("!!!!!NODE CONTENT UPDATED OF "+id);
					updateNodeContent(id);
				}
			}
//			emptyBackPackMissionCompleted();
			if(moveToGoal.toString().equals(""))// || missionCompleted)
			{
				if(!fullyExplored /*|| missionCompleted*/)// || missionCompleted)
				{
					System.out.println(myAgent.getLocalName()+" : not fully explored or missioncopleted ");//+missionCompleted);
					explorationWalk(id);
				}
				else
				{
					System.out.println();
					collectionWalk(graph.getNode(id));
				}
			}
			else{
				
				System.out.println(" ................ MOVE TO : "+moveToNext);
				System.out.println(graph.getNode(id));

				ArrayList<Node> pathToMoveTo = graph.getPathToGivenNode(graph.getNode(id), moveToGoal.toString());
				moveTo(pathToMoveTo, id);
				
			}
		
		}
		
	}

//	private boolean emptyBackPackMissionCompleted() {
//		
//		if(noTreasure && moveToGoal.toString().equals(graph.getMeetingPosition().getId()) 
//				&& ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace()>0 && ((mas.abstractAgent)this.myAgent).emptyMyBackPack("Tank"))
//		{
//			missionCompleted = true;
//			System.out.println(myAgent.getLocalName()+" I'm done !");
//			return true;
//		}
//			
//		
//		
//		return false;
//	}

	
	private void missionCompleted(Node currentNode){
		if(((mas.abstractAgent)this.myAgent).emptyMyBackPack("Tank") && graph.getBestNode(currentNode, treasureType, ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace())==null){
			missionCompleted=true;
//			moveToGoal.replace(0, moveToGoal.length(), "");
			System.out.println(myAgent.getLocalName()+" DONEEEEE");
		}
	}
	
	private void collectionWalk(Node currentNode) {
	
		System.out.println(myAgent.getLocalName()+"************************CollectionWalk****************************"+fullyExplored);
		
		
		//next move planification	
			Node goalNode;
			if(((mas.abstractAgent)this.myAgent).getBackPackFreeSpace()==0){
				goalNode = graph.getMeetingPosition(); //we must empty the backpack
				moveToGoal.replace(0, moveToGoal.length(), goalNode.getId());
				goingToMeetingPoint = true;
			}
			else //there is enough space
			{
				goalNode = graph.getBestNode(currentNode, treasureType, ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
				
				if(goalNode == null){ //if there is no more treasure to take, we go back to the silo
					goalNode = graph.getMeetingPosition();
					moveToGoal.replace(0, moveToGoal.length(), goalNode.getId());
					System.out.println("no more treasure :(");
//					noTreasure = true;
					goingToMeetingPoint = true;
				}
				else //we have a new goal
				{
					goingToMeetingPoint = false;
					System.out.println("The best node is : "+goalNode.getId());
				}
			}
			moveToGoal.replace(0, moveToGoal.length(), goalNode.getId());
			ArrayList<Node> path = graph.getPath(currentNode, goalNode);
			moveTo(path, currentNode.getId());
			
		
	}


	public void updateGraph(List<Couple<String, List<Attribute>>> lobs){

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

	}
	
	
	public void explorationWalk(String id){
		
		System.out.println(myAgent.getLocalName()+"************************ExplorationWalk****************************");

		
//		System.out.println("MOVE TO AU DEBUT : "+moveToNext);
		
		Node goalNode = graph.getRandomUnvisited();
		ArrayList<Node> pathToTheClosest = graph.getPath(graph.getNode(id), goalNode);
		
//			ArrayList<Node> pathToTheClosest = graph.getPathToClosestUnvisited(graph.getNode(id));
			if(pathToTheClosest == null){
				System.out.println("The graph has been fully explored ! List of nodes : \n");
				fullyExplored = true ;
				graph.sort();
				moveToNext.replace(0, moveToNext.length(), "");
				moveToGoal.replace(0, moveToGoal.length(), "");
				//graph.printNodes();
			}
			else{
				
				moveToGoal.replace(0, moveToGoal.length(), pathToTheClosest.get(pathToTheClosest.size()-1).getId());
				moveTo(pathToTheClosest, id);
				
			}

	}
	
	
	private void updateNodeContent(String id) {
		List<Couple<String,List<Attribute>>> new_lobs=((mas.abstractAgent)this.myAgent).observe(); 
		List<Attribute> newContentList = new_lobs.get(0).getRight();
//		System.out.println(id+"================oldcontent : "+graph.getNode(id).getContentList().toString());
		graph.getNode(id).setContent(newContentList);
//		System.out.println(id+"================newcontent : "+graph.getNode(id).getContentList().toString());
	}

	public void moveTo(ArrayList<Node> pathToMoveTo, String currentNode){
		
		
		System.out.print(myAgent.getLocalName()+" MOVING TOOOOOOOO ::: "+moveToGoal.toString()+ " path is :: ");
		System.out.println(pathToMoveTo);
		try {
			if(pathToMoveTo.get(0).getId().equalsIgnoreCase(currentNode)) {
				pathToMoveTo.remove(graph.getNode(currentNode));
			}
			if(pathToMoveTo.size() == 1) { //we can reach directly the desired state
				moveToNext.replace(0, moveToNext.length(), pathToMoveTo.get(0).getId());
				moved = ((mas.abstractAgent)this.myAgent).moveTo(moveToNext.toString());
			
				//moveTo.replace(0, moveTo.length(), "");
			}
			else {
				moved = ((mas.abstractAgent)this.myAgent).moveTo(pathToMoveTo.get(0).getId());
				moveToNext.replace(0, moveToNext.length(), pathToMoveTo.get(0).getId());
			}
			
		}
		catch(Exception e) {
			if(!moveToNext.toString().equalsIgnoreCase(graph.getNode(currentNode).getId())){
				moved = false;
				//moveToNext.replace(0, moveToNext.length(), pathToMoveTo.get(0).getId());
			}
				
			else
				moved = true;
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
				((mas.abstractAgent)this.myAgent).emptyMyBackPack("Tank");
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
	
	public boolean golemIsNear(){
		List<Couple<String,List<Attribute>>> lobs=((mas.abstractAgent)this.myAgent).observe();
		String id = lobs.get(0).getLeft();
		
		//list of attribute associated to the currentPosition
		List<Attribute> lattribute= lobs.get(0).getRight();
		
		for(Attribute attr : lattribute){
			if(attr.getName().contains("stench")){
				return true;
			}
		}

		return false;
	}
	
	@Override
	public boolean done() {
		
//		try {
//			System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
//			System.in.read();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		this.previousState.replace(0, this.previousState.length(), Constants.STATE_START_INTERBLOCAGE);
		
		return true;
	}
	
	public int onEnd(){
		if(moved) {
			if(moveToGoal.toString().equalsIgnoreCase(((abstractAgent) myAgent).getCurrentPosition())) {
				moveToGoal.replace(0, moveToGoal.length(), "");
			}
			moveToNext.replace(0, moveToNext.length(), "");
			//
			return Constants.MOVED;
		}
		else {
			return Constants.BLOCKED;
		}
	}

}
