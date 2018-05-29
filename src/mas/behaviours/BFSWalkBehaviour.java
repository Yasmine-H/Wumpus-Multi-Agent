package mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import env.Attribute;
import env.Couple;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import mas.abstractAgent;
import mas.agents.BFSExploAgent;
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
	
	public static final int MOVED = 1;
	public static final int BLOCKED = 0;
	//private ArrayList<Node> graph;
	private Graph graph;
	private boolean fullyExplored = false;
	private boolean moved = false;
	private StringBuilder moveTo;
	private StringBuilder previousState;
	
	public BFSWalkBehaviour (final mas.abstractAgent myagent, Graph graph /*ArrayList<Node> graph*/, StringBuilder moveTo, StringBuilder previousState) {
		super(myagent);
		this.graph = graph;
		//super(myagent);
		this.moveTo = moveTo;
		this.previousState = previousState;
	}

	@Override
	public void action() {
		//Example to retrieve the current position
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();
		if(fullyExplored) {
			
		}
		else {
		
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
            
			
			//			//Little pause to allow you to follow what is going on
			try {
				System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			

			//list of attribute associated to the currentPosition
			List<Attribute> lattribute= lobs.get(0).getRight();
			
			//example related to the use of the backpack for the treasure hunt
			Boolean b=false;
			
			for(Attribute a:lattribute){
				switch (a) {
				case TREASURE:
					/*
					System.out.println("My type is : "+((mas.abstractAgent)this.myAgent).getMyTreasureType());
					System.out.println("My current backpack capacity is:"+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
					System.out.println("Value of the treasure on the current position: "+a.getValue());
					System.out.println("The agent grabbed :"+((mas.abstractAgent)this.myAgent).pick());
					System.out.println("the remaining backpack capacity is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
					System.out.println("The value of treasure on the current position: (unchanged before a new call to observe()): "+a.getValue());
					*/
					b=true;
					
					//Little pause to allow you to follow what is going on
					try {
						System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
						System.in.read();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					break;
			case DIAMONDS:
				/*
				System.out.println("My type is : "+((mas.abstractAgent)this.myAgent).getMyTreasureType());
				System.out.println("My current backpack capacity is:"+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
				System.out.println("Value of the diamonds on the current position: "+a.getValue());
				System.out.println("The agent grabbed :"+((mas.abstractAgent)this.myAgent).pick());
				System.out.println("the remaining backpack capacity is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
				System.out.println("The value of treasure on the current position: (unchanged before a new call to observe()): "+a.getValue());
				*/
				b=true;
				
				//Little pause to allow you to follow what is going on
				try {
					System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
					System.in.read();
				} catch (IOException e) {
					e.printStackTrace();
				}
				default:
					break;
				}
				
			}

			//If the agent picked (part of) the treasure
			if (b){
				List<Couple<String,List<Attribute>>> lobs2=((mas.abstractAgent)this.myAgent).observe();//myPosition
				//System.out.println("list of observables after picking "+lobs2);
			}
			/*
			//Random move from the current position
			Random r= new Random();
			//1) get a couple <Node ID,list of percepts> from the list of observables
			int moveId=r.nextInt(lobs.size());
			//2) Move to the picked location. The move action (if any) MUST be the last action of your behaviour
			((mas.abstractAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
			*/
			
			//Choosing the next move
			//case a) if there is a neighbour who hasn't been visited yet
			
			
			
			
			
			
			//System.out.println("Before calling nexttovisit, the neighbours are : "+graph.getNode(id).getNeighbours().toString()+" and listNeighbours : "+listNeighboursId.toString());
			//String idToVisit = graph.getClosestUnvisited(id, new ArrayList<Couple<String,String>>()); 
						System.out.println("MOVE TO AU DEBUT : "+moveTo);
						if(moveTo.toString().equals("")) {
							ArrayList<Node> pathToTheClosest = graph.getPathToClosestUnvisited(graph.getNode(id));
							if(pathToTheClosest == null){
								System.out.println("The graph has been fully explored ! List of nodes : \n");
								fullyExplored = true ;
								//graph.printNodes();
							}
							else{
								if(pathToTheClosest.get(0).getId().equalsIgnoreCase(id)){ //if the first node of the path is the current node, which is normally the case 
									//TODO 26.2.: it shouldn't be the case, it is not very proper like this!
									pathToTheClosest.remove(graph.getNode(id));
								}
								//System.out.println("Exploration de "+myAgent.getLocalName());
								//graph.printNodes();
								moveTo = moveTo.replace(0, moveTo.length(),pathToTheClosest.get(0).getId());
								moved = ((mas.abstractAgent)this.myAgent).moveTo(moveTo.toString()); //we visit the first next node on the path
                               
								//System.out.println("Node to visit : "+pathToTheClosest.get(0).getId());
								
							}
						}
						else { //we know where we want to move
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

	}

	@Override
	public boolean done() {
		// TODO 
		//return fullyExplored;
		this.previousState.replace(0, this.previousState.length(), BFSExploAgent.STATE_START_INTERBLOCAGE);
		return true;
	}
	
	public int onEnd(){
		// TODO 28.2 : FSMBehaviour start moving to inform the other agents
		if(moved) {
			if(moveTo.toString().equalsIgnoreCase(((abstractAgent) myAgent).getCurrentPosition())) {
				moveTo.replace(0, moveTo.length(), "");
			}
			//
			return MOVED;
		}
		else {
			return BLOCKED;
		}
			
	}

}