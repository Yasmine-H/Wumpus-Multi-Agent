package mas.behaviours;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import mas.abstractAgent;
import mas.graph.Graph;
import mas.graph.Node;

public class InterblocageResolutionBehaviour extends Behaviour{
	
	private Graph graph;
	private ACLMessage interblocageMessage;
	private AID sender;
	private String senderType;
	private String senderPosition;
	private String senderDesiredPosition;
	private String myPosition;
	private int unexploredAtExtremities; 

	public InterblocageResolutionBehaviour(final mas.abstractAgent myAgent, Graph graph, ACLMessage interblocageMessage) {
		super(myAgent);
		this.graph = graph;
		this.sender = interblocageMessage.getSender();
		this.myPosition = myAgent.getCurrentPosition();
		analyzeMessage(interblocageMessage);
		
		this.unexploredAtExtremities = 0;
	}
	
	@Override
	public void action() {
		System.out.println("INTERBLOCAGE RESOLUTION Bevahivour*************************");
		
		//If I'm at the end of a blind lane (i.e., if my current node has only 1 neighbour), I can't give a priority!
		if(graph.getNode(myPosition).getNeighbours().size() == 1) {
			ACLMessage response =new ACLMessage(ACLMessage.REFUSE);
			response.setSender(myAgent.getAID());
			response.addReceiver(sender);
			response.setContent("INTERBLOCAGE: I'm in the blind line, I can't make way.");
			((abstractAgent) myAgent).sendMessage(response);
		}
		
		//If I'm not blocking him anymore - I've moved before reading his message:
		if(!senderDesiredPosition.contains(myPosition)) { //TODO: 18.4.: I don't use equals because as senderDesiredPosition was retrieved from the message, I'm not sure if the string contains only 
													      //what we want or also some spaces (or that kind of stuff) at its beginning/end.
			ACLMessage response =new ACLMessage(ACLMessage.AGREE);
			response.setSender(myAgent.getAID());
			response.addReceiver(sender);
			response.setContent("INTERBLOCAGE: I'm not blocking you anymore, you can move!"); //TODO 18.4.: - is it AGREE or inform? I think it should be inform - 
																							  //with AGREE, we just let him know that we would move, once the way is free, we should confirm it
																							  //to him in GivePriorityBehaviour with an inform message - or not? It's to discuss!  
			((abstractAgent) myAgent).sendMessage(response);
		}
		
		//TODO 18.4.: If I want to move to another node than the sender? F.e., I am in the node he wants to move to, but I want to move nto another one - I can move here and make him a place!
		
		//else - we are really in interblocage
		if(senderType.contains("EXPLO")) {
			twoExplorersResolution();
		}
		else if(senderType.contains("COLL")) { //TODO 18.4.: When the collector is created
			explorerCollectorResolution();
		}
	}

	@Override
	public boolean done() {
		return true;
	}
	
	private void analyzeMessage(ACLMessage msg) {
		String[] lines = msg.getContent().split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] lineParts = line.split(":");
			
			if(lineParts[0].contains("Type")){
				this.senderType = lineParts[1];
			}
			else if(lineParts[0].contains("Blocked")) {
				this.senderPosition = lineParts[1];
			}
			else if(lineParts[0].contains("move to")) {
				this.senderDesiredPosition = lineParts[1];
			}
		}
	}
	
	//TODO 18.4.: This function should be in the class graph maybe instead of here? 
	private boolean graphFullyExplored() {
		for(Node node: graph.getAllNodes()) {
			if(!node.getVisited()) {
				return false;
			}
		}
		return true;
	}
	
	private boolean meInCorridor() {
		//If my node has only two neighbours, I'm in the corridor 
		if(graph.getNode(myPosition).getNeighbours().size() == 2) {
			return true;
		}
		return false;
	}
	
	private boolean himInCorridor() {
		if(graph.getNode(senderDesiredPosition).getNeighbours().size() == 2) {
			return true;
		}
		return false;
	}
	
	private int distanceToCrossroad(String positionOfOtherAgent) {
		boolean crossroadReached = false;
		int distance = 1;
		//The list of already visited nodes - to follow only one direction and do not oscillate in the corridor!
		ArrayList<String> onThePath = new ArrayList<>();
		//To fix our initial direction (the direction in which another agent wants to move), we will say that the senders's actual position has already been visited
		onThePath.add(positionOfOtherAgent);
		String actualPosition = myPosition;
		
		while(!crossroadReached) {
			onThePath.add(actualPosition);
			ArrayList<Node> neighbours = graph.getNode(actualPosition).getNeighbours();
			
			boolean stillInCorridor = false;
			for(Node neighbour : neighbours) {
				//If the corridor continues
				if(neighbour.getNeighbours().size() == 2 && !onThePath.contains(neighbour.getId())) { //TODO 18.4. - cf. TODO 2 in twoExplorersResolution() - 
																									  //should we suppose that there can be an unvisited node inside the corridor??? Normally there shouldn't
					distance++;
					actualPosition = neighbour.getId();
					stillInCorridor = true;
					break;
				}
			}
			
			//If there is no unvisited node of the degree 2, we have sorted from the corridor
			crossroadReached = !stillInCorridor;
			
		}
		
		//We check if there exists an unexplored node at the corridor's extremity:
		for(Node neighbour : graph.getNode(actualPosition).getNeighbours()) {
			//Once we find the unexplored node at the reached extremity :
			if(!neighbour.getVisited()) {
				unexploredAtExtremities++;
				break;
			}
		}
		
		return distance;
	}
	
	private void twoExplorersResolution() {
		//If the graph is not fully explored:
		//TODO 18.4.: We will start by graph exchange!
		
		//we check if someone is in the corridor:
		if(meInCorridor() && himInCorridor()) {
			//TODO 18.4.: The agent who is nearer the KNOWN crossroad has priority (in theory, both crossroads should be known as the agents are in conflits - they've come normally from 
			//opposite sides)
			unexploredAtExtremities = 0;
			int myDistance = distanceToCrossroad(senderPosition);
			int hisDistance = distanceToCrossroad(myPosition);
			
			if(unexploredAtExtremities == 2) {
				//TODO 18.4.: Agents should share the information and go away in opposites directions to explore the graph at both extremities
			}
			else { //There will be someone who has priority
				if(myDistance > hisDistance) {
					ACLMessage response =new ACLMessage(ACLMessage.REFUSE);
					response.setSender(myAgent.getAID());
					response.addReceiver(sender);
					response.setContent("INTERBLOCAGE: You are nearer the crossroad - I have priority.");
					((abstractAgent) myAgent).sendMessage(response);
				}
				if(myDistance <= hisDistance) { //TODO 18.4: If egality? We can be egoistic, gentle or employer the probability/ look if there is someone else approaching...
					ACLMessage response =new ACLMessage(ACLMessage.AGREE);
					response.setSender(myAgent.getAID());
					response.addReceiver(sender);
					response.setContent("INTERBLOCAGE: I'm nearer the crossroad - you have priority, I will make you a way.");
					((abstractAgent) myAgent).sendMessage(response);
				}
			}
		}
		//If it is only me who is in the corridor, I won't allow him to push me inside!
		else if(meInCorridor()) {
				ACLMessage response =new ACLMessage(ACLMessage.REFUSE);
				response.setSender(myAgent.getAID());
				response.addReceiver(sender);
				response.setContent("INTERBLOCAGE: You are nearer the crossroad - I have priority.");
				((abstractAgent) myAgent).sendMessage(response);
		}
		//If it is only him who is in the corridor, I let him go away...
		else if(himInCorridor()) {
				ACLMessage response =new ACLMessage(ACLMessage.AGREE);
				response.setSender(myAgent.getAID());
				response.addReceiver(sender);
				response.setContent("INTERBLOCAGE: I'm nearer the crossroad - you have priority, I will make you a way.");
				((abstractAgent) myAgent).sendMessage(response);
		}
		else { //no one in the corridor
			//TODO 18.4. !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		}
	}
	
	private void explorerCollectorResolution() {
		
		//If agents are in the corridor:
		//TODO 18. 4: The agent who is nearer the known crossroad has priority  
		
		//If agents aren't in the corridor:
		//If the graph is not fully explored, it is the explorer who has priority
		if(!graphFullyExplored()) {
			ACLMessage response =new ACLMessage(ACLMessage.REFUSE);
			response.setSender(myAgent.getAID());
			response.addReceiver(sender);
			response.setContent("INTERBLOCAGE: The graph is not fully explored and I am explorer - I have priority.");
			((abstractAgent) myAgent).sendMessage(response);
		}
		else { //Otherwise, it's collector who has priority
			ACLMessage response =new ACLMessage(ACLMessage.AGREE);
			response.setSender(myAgent.getAID());
			response.addReceiver(sender);
			response.setContent("INTERBLOCAGE: The graph is fully explored and I am explorer - you have priority, I make you a way.");
			((abstractAgent) myAgent).sendMessage(response);
			
			//TODO : we will go to the state GivePriorityBehaviour
		}
		
	}

}
