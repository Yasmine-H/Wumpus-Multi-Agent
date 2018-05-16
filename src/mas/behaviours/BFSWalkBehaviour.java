package mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import env.Attribute;
import env.Couple;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import mas.abstractAgent;
import mas.agents.Constants;
import mas.graph.Graph;
import mas.graph.Node;

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

	/*
	public static final int MOVED = 1;
	public static final int BLOCKED = 0;
	
	*/
	//private ArrayList<Node> graph;
	private Graph graph;
	private boolean fullyExplored = false;
	private boolean moved = false;
	private ACLMessage interblocageMessage;
	private String moveTo;

	public BFSWalkBehaviour (final mas.abstractAgent myagent, Graph graph /*ArrayList<Node> graph*/, ACLMessage interblocageMessage) {
		super(myagent);
		this.graph = graph;
		//super(myagent);
		this.interblocageMessage = interblocageMessage;
		this.moveTo = "";
	}

	@Override
	public void action() {
		//Example to retrieve the current position
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();

		System.out.println(myAgent.getLocalName()+"************************BFSWalkBehaviour****************************");

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


			if(!fullyExplored){ //
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
				}
			}
			else{// go to center
				
			}
		}

	}

	@Override
	public boolean done() {
		try {
			System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public int onEnd(){
		// TODO 28.2 : FSMBehaviour start moving to inform the other agents
		if(moved)
			return Constants.MOVED;
		else {
			interblocageMessage.setSender(this.myAgent.getAID());
			interblocageMessage.setContent("INTERBLOCAGE: \n Agent: "+myAgent.getLocalName()+"\n blocked at:" +((abstractAgent) myAgent).getCurrentPosition()+"\n want move to :"+moveTo);
			return Constants.BLOCKED;
		}

	}

}