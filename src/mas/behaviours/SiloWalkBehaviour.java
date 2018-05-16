package mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.graphstream.ui.util.CubicCurve.MyCanvas;

import env.Attribute;
import env.Couple;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
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
	private ACLMessage interblocageMessage;
	private String moveTo;
	private boolean inMeetingPosition = false;


	public SiloWalkBehaviour(final mas.abstractAgent myagent, Graph graph) {
		super(myagent);
		this.graph=graph;
	}

	@Override
	public void action() {

		System.out.println(myAgent.getLocalName()+"************************SiloWalkBehaviour****************************");
		//Example to retrieve the current position
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();


		if (myPosition!=""){
			
			//List of observable from the agent's current position
			List<Couple<String,List<Attribute>>> lobs=((mas.abstractAgent)this.myAgent).observe();//myPosition
			
			if(!fullyExplored){ //performs exploration behaviour
				explorationWalk(lobs);
			}
			//System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
			
			else{ //go to meeting point
				
				if(inMeetingPosition){
					System.out.println(myAgent.getLocalName()+" meeting point reached");
				}
				else{
				
					Node meetingPosition = graph.getMeetingPosition();
					System.out.println(myAgent.getLocalName()+" : Going to meetingPoint "+meetingPosition.getId());
					ArrayList<Node> path = graph.getPath(graph.getNode(myPosition), meetingPosition);
					if(path!=null){
					String moveTo = path.get(1).getId();
					boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(moveTo); //
					System.out.println("!!!!!!!!!!!!!!!!!!SILO MOVING TOOOOOO :: "+moveTo+" bc position is : "+path.get(1).getId()+
								"\n and complete path is "+path.toString()+" and result is : "+moved);
				}
				
				
				
				/*
				String id = lobs.get(0).getLeft();

				Node goalNode = graph.getSiloPosition();
				ArrayList<Node> path = graph.getPath(graph.getNode(id), goalNode);
				if(path!=null){
				String moveTo = path.get(1).getId();
				//boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(moveTo); //
				System.out.println("!!!!!!!!!!!!!!!!!!SILO MOVING TOOOOOO :: "+moveTo+" bc position is : "+goalNode.getId()+
							"\n and complete path is "+path.toString());
				*/	
					
				}				
			}
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
		}

	}
	

	
	
	@Override
	public boolean done() {
		//Little pause to allow you to follow what is going on
				try {
					System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
					System.in.read();
				} catch (IOException e) {
					e.printStackTrace();
				}

		return true;
	}

}
