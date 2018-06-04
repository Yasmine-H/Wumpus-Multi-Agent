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
import mas.agents.BFSExploAgent;;

/**************************************
 * 
 * 
 * 				BEHAVIOUR
 * 
 * 
 **************************************/


public class BFSWalkBehaviour extends SimpleBehaviour{
	/**
	 * When an agent choose to move
	 *  
	 */



	private static final long serialVersionUID = 9088209402507795290L;

	//	public static final int MOVED = 1;
	//	public static final int BLOCKED = 0;
	//private ArrayList<Node> graph;
	private Graph graph;
	private boolean fullyExplored = false;
	private boolean moved = false;
	private StringBuilder moveToGoal;
	private StringBuilder moveToNext;
	private StringBuilder previousState;

	public BFSWalkBehaviour (final mas.abstractAgent myagent, Graph graph, StringBuilder moveTo, StringBuilder moveToGoal, StringBuilder previousState) {
		super(myagent);
		this.graph = graph;
		this.moveToNext = moveTo;
		this.moveToGoal = moveToGoal;
		this.previousState = previousState;
	}

	@Override
	public void action() {
		System.out.println(myAgent.getLocalName()+"************************ExploWalkBehaviour**************************** ::"+fullyExplored);

		//Example to retrieve the current position
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();

		if(fullyExplored) {
			//List of observable from the agent's current position
			List<Couple<String,List<Attribute>>> lobs=((mas.abstractAgent)this.myAgent).observe();//myPosition
			System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);

			String id = lobs.get(0).getLeft();
			graph.getNode(id).setContent((List<Attribute>)lobs.get(0).getRight());

			if(moveToGoal.toString().equals("")) {
				ArrayList<Node> neighbours = graph.getNode(myPosition).getNeighbours();
				String randomNeighbour = getNeighbourNotInLine(neighbours);

				moveToGoal.replace(0, moveToGoal.length(),randomNeighbour);
				moveToNext.replace(0, moveToNext.length(),randomNeighbour);
				moved = ((mas.abstractAgent)this.myAgent).moveTo(moveToNext.toString()); //we visit the first next node on the path

				//System.out.println("Node to visit : "+pathToTheClosest.get(0).getId());

			}
			else { //we know where we want to move
				//System.out.println(" ................ MOVE TO : "+moveTo);
				//System.out.println(graph.getNode(id));

				ArrayList<Node> pathToMoveTo = graph.getPathToGivenNode(graph.getNode(id), moveToGoal.toString());
				
				moveTo(pathToMoveTo, id);
				//System.out.println(pathToMoveTo);
//				try {
//					if(pathToMoveTo.get(0).getId().equalsIgnoreCase(id)) {
//						pathToMoveTo.remove(graph.getNode(id));
//					}
//					if(pathToMoveTo.size() == 1) { //we can reach directly the desired state
//						moved = ((mas.abstractAgent)this.myAgent).moveTo(moveToGoal.toString());
//						//moveTo.replace(0, moveTo.length(), "");
//					}
//					else {
//						moved = ((mas.abstractAgent)this.myAgent).moveTo(pathToMoveTo.get(0).getId());
//					}
//				}
//				catch(Exception e) {
//					if(!moveToGoal.toString().equalsIgnoreCase(graph.getNode(id).getId()))
//						moved = false;
//					else
//						moved = true;
//				}
			}
		}

		else { //not fully explored !

			if (myPosition!=""){
				//List of observable from the agent's current position
				List<Couple<String,List<Attribute>>> lobs=((mas.abstractAgent)this.myAgent).observe();//myPosition
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
					else {
						graph.getNode(id).setContent((List<Attribute>)lobs.get(0).getRight());
					}
				}

				//Finally, we add the explored node to the list of neighbours of its each neighbour
				for(Node neighbour: listNeighbours){
					neighbour.addNeighbour(graph.getNode(id));
				}

				//System.out.println("Before calling nexttovisit, the neighbours are : "+graph.getNode(id).getNeighbours().toString()+" and listNeighbours : "+listNeighboursId.toString());
				//String idToVisit = graph.getClosestUnvisited(id, new ArrayList<Couple<String,String>>()); 
				//System.out.println("MOVE TO AU DEBUT : "+moveTo);
				if(moveToGoal.toString().equals("")) {
					ArrayList<Node> pathToTheClosest = graph.getPathToClosestUnvisited(graph.getNode(id));
					if(pathToTheClosest == null){
						System.out.println("The graph has been fully explored ! List of nodes : \n");
						fullyExplored = true ;
						graph.sort();
						//graph.printNodes();
					}
					else{
						
						moveTo(pathToTheClosest, id);
						
//						if(pathToTheClosest.get(0).getId().equalsIgnoreCase(id)){ //if the first node of the path is the current node, which is normally the case 
//							//TODO 26.2.: it shouldn't be the case, it is not very proper like this!
//							pathToTheClosest.remove(0);
//						}
//						//System.out.println("Exploration de "+myAgent.getLocalName());
//						//graph.printNodes();
//						moveToGoal = moveToGoal.replace(0, moveToGoal.length(),pathToTheClosest.get(0).getId());
//						moved = ((mas.abstractAgent)this.myAgent).moveTo(moveToGoal.toString()); //we visit the first next node on the path
//
						//System.out.println("Node to visit : "+pathToTheClosest.get(0).getId());

					}
				}
				else { //we know where we want to move
					//System.out.println(" ................ MOVE TO : "+moveTo);
					//System.out.println(graph.getNode(id));

					ArrayList<Node> pathToMoveTo = graph.getPathToGivenNode(graph.getNode(id), moveToGoal.toString());
					//System.out.println(pathToMoveTo);
					moveTo(pathToMoveTo, id);
					
//					try {
//						if(pathToMoveTo.get(0).getId().equalsIgnoreCase(id)) {
//							pathToMoveTo.remove(graph.getNode(id));
//						}
//						if(pathToMoveTo.size() == 1) { //we can reach directly the desired state
//							moved = ((mas.abstractAgent)this.myAgent).moveTo(moveToGoal.toString());
//							//moveTo.replace(0, moveTo.length(), "");
//						}
//						else {
//							moved = ((mas.abstractAgent)this.myAgent).moveTo(pathToMoveTo.get(0).getId());
//						}
//					}
//					catch(Exception e) {
//						if(!moveToGoal.toString().equalsIgnoreCase(graph.getNode(id).getId()))
//							moved = false;
//						else
//							moved = true;
//					}
				}
			}
		}

	}

	@Override
	public boolean done() {
		// TODO 
		//return fullyExplored;

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
		// TODO 28.2 : FSMBehaviour start moving to inform the other agents
		if(moved) {
			if(moveToGoal.toString().equalsIgnoreCase(((abstractAgent) myAgent).getCurrentPosition())) {
				moveToGoal.replace(0, moveToGoal.length(), "");
				if(((BFSExploAgent)myAgent).getInterblocageInCours()) {
					((BFSExploAgent)myAgent).setInterblocageInCours(false);
				}
			}
			//
			return Constants.MOVED;
		}
		else {
			return Constants.BLOCKED;
		}

	}

	public void moveTo(ArrayList<Node> pathToMoveTo, String currentNode){

		System.out.print(myAgent.getLocalName()+" MOVING TOOOOOOOO ::: "+moveToGoal.toString()+ "path is :: ");
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


	public String getNeighbourNotInLine(ArrayList<Node> neighbourhood) {
		ArrayList<Node> adepts = new ArrayList();
		for(Node node : neighbourhood) {
			if(node.getNeighbours().size() > 2) {
				adepts.add(node);
			}
		}
		if (adepts.size() > 0) {
			int index = (int)(Math.random()*adepts.size());
			return adepts.get(index).getId();	
		}
		else {
			int index = (int)(Math.random()*neighbourhood.size());
			return neighbourhood.get(index).getId();
		}
	}

	public String getRandomNode() {
		ArrayList<Node> adepts = new ArrayList();
		for(Node node : graph.getAllNodes()) {
			if(node.getNeighbours().size() > 2) {
				adepts.add(node);
			}
		}
		int index = (int)(Math.random()*adepts.size());
		return adepts.get(index).getId();	
	}
}
