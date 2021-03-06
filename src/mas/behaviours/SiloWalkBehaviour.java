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

public class SiloWalkBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1830274726141667787L;

	private Graph graph;
	private boolean fullyExplored = false;
	private boolean moved = false;
	private StringBuilder moveToNext;
	private StringBuilder moveToGoal;
	private StringBuilder previousState;
	private boolean inMeetingPosition = false;


	public SiloWalkBehaviour(final mas.abstractAgent myagent, Graph graph, StringBuilder moveTo, StringBuilder moveToGoal, StringBuilder previousState) {
		super(myagent);
		this.graph=graph;
		this.moveToNext = moveTo;
		this.previousState = previousState;
		this.moveToGoal = moveToGoal;
	}

	@Override
	public void action() {

		System.out.println(myAgent.getLocalName()+"************************SiloWalkBehaviour**************************** ::"+fullyExplored);
		//Example to retrieve the current position
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();


		if (myPosition!=""){
			System.out.println(myAgent.getLocalName()+" position = "+myPosition);
			//List of observable from the agent's current position
			List<Couple<String,List<Attribute>>> lobs=((mas.abstractAgent)this.myAgent).observe();
			String id = lobs.get(0).getLeft();
			
			//list of attribute associated to the currentPosition
			List<Attribute> lattribute= lobs.get(0).getRight();

			
			
			if(!fullyExplored){ // the agent explores the graph and collects when possible
				updateGraph(lobs);
			}
			updateNodeContent(id);
			
			if(moveToGoal.toString().equals("") && !inMeetingPosition)
			{
				if(!fullyExplored)
				{
					explorationWalk(id);
				}
				else
				{
					goToMeetingPointWalk(graph.getNode(id));
				}
			}
			else
			{
				if(!inMeetingPosition)			
				{
					
					System.out.println(" ................ MOVE TO : "+moveToNext);
					System.out.println(graph.getNode(id));
	
					ArrayList<Node> pathToMoveTo = graph.getPathToGivenNode(graph.getNode(id), moveToGoal.toString());
					moveTo(pathToMoveTo, id);
					
				}
				else
				{
					System.out.println("Silo in meeting position !");
				}
			
			}
			
			
//			if(!fullyExplored){ //performs exploration behaviour
//				explorationWalk(lobs);
//				System.out.println("Silo : Not fully explored");
//			}
//			//System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
//			
//			else{ //go to meeting point
//				
//				if(inMeetingPosition){
//					System.out.println(myAgent.getLocalName()+" meeting point reached");
//				}
//				else{
//				
//					Node meetingPosition = graph.getMeetingPosition();
//					System.out.println(myAgent.getLocalName()+" : Going to meetingPoint "+meetingPosition.getId());
//					ArrayList<Node> path = graph.getPath(graph.getNode(myPosition), meetingPosition);
//					if(path!=null){
//					String moveTo = path.get(1).getId();
//					boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(moveTo); //
//					System.out.println("!!!!!!!!!!!!!!!!!!SILO MOVING TOOOOOO :: "+moveTo+" bc position is : "+path.get(1).getId()+
//								"\n and complete path is "+path.toString()+" and result is : "+moved);
//					if(moved && moveTo.equals(myPosition)){
//						inMeetingPosition = true;
//					}
//				}
//				
//				
//				
//				/*
//				String id = lobs.get(0).getLeft();
//
//				Node goalNode = graph.getSiloPosition();
//				ArrayList<Node> path = graph.getPath(graph.getNode(id), goalNode);
//				if(path!=null){
//				String moveTo = path.get(1).getId();
//				//boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(moveTo); //
//				System.out.println("!!!!!!!!!!!!!!!!!!SILO MOVING TOOOOOO :: "+moveTo+" bc position is : "+goalNode.getId()+
//							"\n and complete path is "+path.toString());
//				*/	
//					
//				}				
//			}
		}
	}

	private void goToMeetingPointWalk(Node node) {
		Node meetingPosition = graph.getMeetingPosition();
		System.out.println(myAgent.getLocalName()+" : Going to meetingPoint "+meetingPosition.getId());
		ArrayList<Node> path = graph.getPath(node, meetingPosition);
		moveTo(path, node.getId());
		
//		if(path!=null){
//			String moveTo = path.get(1).getId();
//			boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(moveTo); //
//			System.out.println("!!!!!!!!!!!!!!!!!!SILO MOVING TOOOOOO :: "+moveTo+" bc position is : "+path.get(1).getId()+
//					"\n and complete path is "+path.toString()+" and result is : "+moved);
//			if(moved && moveTo.equals(node.getId())){
//				inMeetingPosition = true;
//			}
//
//		}
	}

	public void explorationWalk(String id){

		System.out.println(myAgent.getLocalName()+"************************ExplorationWalk****************************");


		//		System.out.println("MOVE TO AU DEBUT : "+moveToNext);
		ArrayList<Node> pathToTheClosest = graph.getPathToClosestUnvisited(graph.getNode(id));
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

		//System.out.println("Before creating the node, the neighbours are : "+listNeighboursId.toString());
		
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
		
		/*
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
				pathToTheClosest.remove(graph.getNode(id));
				//System.out.println("Exploration de "+myAgent.getLocalName());
				//graph.printNodes();
				moveTo = pathToTheClosest.get(0).getId();
				moved = ((mas.abstractAgent)this.myAgent).moveTo(moveTo); //we visit the first next node on the path

				System.out.println(myAgent.getLocalName()+" Node to visit : "+pathToTheClosest.get(0).getId());
			}
		}*/
		
		//System.out.println("MOVE TO AU DEBUT : "+moveTo);
		if(moveToNext.toString().equals("")) {
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
				}
				//System.out.println("Exploration de "+myAgent.getLocalName());
				//graph.printNodes();
				moveToNext = moveToNext.replace(0, moveToNext.length(),pathToTheClosest.get(0).getId());
				moved = ((mas.abstractAgent)this.myAgent).moveTo(moveToNext.toString()); //we visit the first next node on the path

				//System.out.println("Node to visit : "+pathToTheClosest.get(0).getId());

			}
		}
		else { //we know where we want to move
			//System.out.println(" ................ MOVE TO : "+moveTo);
			//System.out.println(graph.getNode(id));

			ArrayList<Node> pathToMoveTo = graph.getPathToGivenNode(graph.getNode(id), moveToNext.toString());
			//System.out.println(pathToMoveTo);
			try {
				if(pathToMoveTo.get(0).getId().equalsIgnoreCase(id)) {
					pathToMoveTo.remove(graph.getNode(id));
				}
				if(pathToMoveTo.size() == 1) { //we can reach directly the desired state
					moved = ((mas.abstractAgent)this.myAgent).moveTo(moveToNext.toString());
					//moveTo.replace(0, moveTo.length(), "");
				}
				else {
					moved = ((mas.abstractAgent)this.myAgent).moveTo(pathToMoveTo.get(0).getId());
				}
			}
			catch(Exception e) {
				if(!moveToNext.toString().equalsIgnoreCase(graph.getNode(id).getId()))
					moved = false;
				else
					moved = true;
			}
		}

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
	
	


	public void moveTo(ArrayList<Node> pathToMoveTo, String currentNode){
		
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
	

	private void updateNodeContent(String id) {
		List<Couple<String,List<Attribute>>> new_lobs=((mas.abstractAgent)this.myAgent).observe(); 
		List<Attribute> newContentList = new_lobs.get(0).getRight();
//		System.out.println(id+"================oldcontent : "+graph.getNode(id).getContentList().toString());
		graph.getNode(id).setContent(newContentList);
//		System.out.println(id+"================newcontent : "+graph.getNode(id).getContentList().toString());
	}
	
	@Override
	public boolean done() {
		//Little pause to allow you to follow what is going on
//				try {
//					System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
//					System.in.read();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}

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
//		// TODO 28.2 : FSMBehaviour start moving to inform the other agents
//		//System.out.println("Silo walk on end ...........................; :"+moved);
//		if(moved) {
//			
//			if(moveToNext.toString().equalsIgnoreCase(((abstractAgent) myAgent).getCurrentPosition())) {
//				moveToNext.replace(0, moveToNext.length(), "");
//			}
//			//
//			return Constants.MOVED;
//		}
//		else {
//			return Constants.BLOCKED;
//		}

	}

}
