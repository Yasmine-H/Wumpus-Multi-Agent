package mas.behaviours;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import mas.abstractAgent;
import mas.graph.Graph;
import mas.graph.Node;
import mas.agents.BFSExploAgent;
import mas.agents.CollectorAgent;

public class SiloInterblocageResolutionBehaviour extends Behaviour{
	
	private Graph graph;
	private ACLMessage interblocageMessage;
	private AID sender;
	private String senderType;
	private String senderPosition;
	private String senderDesiredPosition;
	private String myPosition;
	private int unexploredAtExtremities; 
	private StringBuilder moveTo;
	private String myExtremity = "";
	private String hisExtremity = "";

	public SiloInterblocageResolutionBehaviour(final mas.abstractAgent myAgent, Graph graph, ACLMessage interblocageMessage, StringBuilder moveTo) {
		super(myAgent);
		this.graph = graph;
		this.sender = interblocageMessage.getSender();
		this.myPosition = myAgent.getCurrentPosition();
		this.moveTo = moveTo;
		analyzeMessage(interblocageMessage);
		
		this.unexploredAtExtremities = 0;
	}
	
	@Override
	public void action() {
		this.interblocageMessage = ((CollectorAgent)myAgent).getInterblocageMessage();
		this.sender = interblocageMessage.getSender();
		this.myPosition = ((abstractAgent) myAgent).getCurrentPosition();
		System.out.println("INTERBLOCAGE msg : "+interblocageMessage.getContent());
		analyzeMessage(interblocageMessage);
		System.out.println(myAgent.getLocalName()+" : INTERBLOCAGE RESOLUTION Bevahivour*************************");
		
		//If I'm at the end of a blind lane (i.e., if my current node has only 1 neighbour), I can't give a priority!
		if(graph.getNode(myPosition).getNeighbours().size() == 1) {
			ACLMessage response =new ACLMessage(ACLMessage.REFUSE);
			response.setSender(myAgent.getAID());
			response.addReceiver(sender);
			response.setContent("INTERBLOCAGE: I'm in the blind line, I can't make way.");
			((abstractAgent) myAgent).sendMessage(response);
		}
		
		//If I'm not blocking him anymore - I've moved before reading his message:
		//System.out.println("msg: "+interblocageMessage);
		else if(!senderDesiredPosition.contains(myPosition)) { //TODO: 18.4.: I don't use equals because as senderDesiredPosition was retrieved from the message, I'm not sure if the string contains only 
													      //what we want or also some spaces (or that kind of stuff) at its beginning/end.
			System.out.println(" xxxxxxxxxxxxxxx"+myAgent.getLocalName()+"  My position : "+myPosition+" sdp : "+senderDesiredPosition);
			
			ACLMessage response =new ACLMessage(ACLMessage.AGREE);
			response.setSender(myAgent.getAID());
			response.addReceiver(sender);
			response.setContent("INTERBLOCAGE: I'm not blocking you anymore, you can move!"); //TODO 18.4.: - is it AGREE or inform? I think it should be inform - 
																							  //with AGREE, we just let him know that we would move, once the way is free, we should confirm it
																							  //to him in GivePriorityBehaviour with an inform message - or not? It's to discuss!  
			((abstractAgent) myAgent).sendMessage(response);
			System.out.println(";;;;;;;;;;;;;;;;;;; >>Agent : "+myAgent.getLocalName()+"  msg "+response+" sent to "+sender+";;;;;;;;;;;;;;;;;;;;;;");
		}
		//If I don't know his desired position - I can't be blocking him ! (this should not appear though ... )
		else if(graph.getNode(senderDesiredPosition) == null) {
			ACLMessage response =new ACLMessage(ACLMessage.AGREE);
			response.setSender(myAgent.getAID());
			response.addReceiver(sender);
			response.setContent("INTERBLOCAGE: I'm not blocking you anymore, you can move!");
			((abstractAgent) myAgent).sendMessage(response);
		}
		
		//TODO 18.4.: If I want to move to another node than the sender? F.e., I am in the node he wants to move to, but I want to move nto another one - I can move here and make him a place!
		
		//else - we are really in interblocage
		else if(senderType.contains("COLL")) {
			siloSomeoneResolution();
		}
		else if(senderType.contains("EXPLO")) { //TODO 18.4.: When the collector is created
			siloSomeoneResolution();
		}
		else if(senderType.contains("SILO")) { //is not possible if there is only one silo
			siloSomeoneResolution();
		}
	}


	@Override
	public boolean done() {
		return true;
	}
	
	private void analyzeMessage(ACLMessage msg) {
		try {		
		System.out.println("********************** IN ANALYSE MESSAGE **********************");	
		String[] lines = msg.getContent().split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] lineParts = line.split(":");
			
			if(lineParts[0].contains("Type")){
				this.senderType = lineParts[1].replaceAll("\\s", "");
			}
			else if(lineParts[0].contains("Blocked")) {
				this.senderPosition = lineParts[1].replaceAll("\\s", "");
			}
			else if(lineParts[0].contains("move to")) {
				this.senderDesiredPosition = lineParts[1].replaceAll("\\s", "");
			}
		}
		} catch (Exception e) {
			// TODO : NullPointerException when FSM is created
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
		System.out.println("sender desired : "+senderDesiredPosition+" graph :"+graph.getAllNodes().toString());
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
		
		String actualPosition = myPosition; //if it's me for whom we are doing the calcul
		
		if(positionOfOtherAgent.equalsIgnoreCase(myPosition)) { //if it's other agent
			actualPosition = senderPosition;
		}
		
		
		while(!crossroadReached && distance < graph.size()) { //if the corridor is a blind line, the boucle would be infinite, that's why graph.size()
			onThePath.add(actualPosition);
			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxx"+actualPosition+"xxxxxxxxxxxxxxxxxxxxxxxxx");
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
			//TODO : Possiblement the infinite cycle !!!
			
		}
		
		//We remember the extremity position
		if(positionOfOtherAgent.equalsIgnoreCase(myPosition)) {
			hisExtremity = actualPosition;
		}
		else {
			myExtremity = actualPosition;
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
	
	private void twoCollectorsResolution() {
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
				ACLMessage response =new ACLMessage(ACLMessage.REFUSE);
				response.setSender(myAgent.getAID());
				response.addReceiver(sender);
				response.setContent("INTERBLOCAGE: \n"
						+ "you move to : "+hisExtremity);
				((abstractAgent) myAgent).sendMessage(response);
				
				moveTo.replace(0, moveTo.length(), myExtremity);
				
			}
			else { //There will be someone who has priority
				if(myDistance > hisDistance) {
					ACLMessage response =new ACLMessage(ACLMessage.REFUSE);
					response.setSender(myAgent.getAID());
					response.addReceiver(sender);
					response.setContent("INTERBLOCAGE: \n"
							+ "you move to : "+hisExtremity);
					((abstractAgent) myAgent).sendMessage(response);
					System.out.println(";;;;;;;;;;;;;;;;;;; >>Agent : "+myAgent.getLocalName()+"  msg "+response+" sent to "+sender+";;;;;;;;;;;;;;;;;;;;;;");
				}
				if(myDistance <= hisDistance) { //TODO 18.4: If egality? We can be egoistic, gentle or employer the probability/ look if there is someone else approaching...
					ACLMessage response =new ACLMessage(ACLMessage.AGREE);
					response.setSender(myAgent.getAID());
					response.addReceiver(sender);
					response.setContent("INTERBLOCAGE: I'm nearer the crossroad - you have priority, I will make you a way.");
					((abstractAgent) myAgent).sendMessage(response);
					System.out.println("sjasljsaoùdskdùoksdosa myExtremity: "+myExtremity);
					moveTo.replace(0, moveTo.length(), myExtremity);
					System.out.println(";;;;;;;;;;;;;;;;;;; >>Agent : "+myAgent.getLocalName()+"  msg "+response+" sent to "+sender+";;;;;;;;;;;;;;;;;;;;;;");
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
				ArrayList<Node>  neighbours = graph.getNode(myPosition).getNeighbours();
				//neighbours.remove(senderDesiredPosition);
				//int index = (int)(Math.random()*neighbours.size());
				//moveTo.replace(0, moveTo.length(), neighbours.get(index).getId());
				
				/*
				for(Node node :neighbours) {
					if(!node.getId().equalsIgnoreCase(senderDesiredPosition)) {
						moveTo.replace(0, moveTo.length(), node.getId());
					}
				}
				*/
				ACLMessage response =new ACLMessage(ACLMessage.AGREE);
				response.setSender(myAgent.getAID());
				response.addReceiver(sender);
				response.setContent("INTERBLOCAGE: I'm nearer the crossroad - you have priority, I will make you a way.");
				((abstractAgent) myAgent).sendMessage(response);
				
				for(Node node :neighbours) {
					if(!node.getId().equalsIgnoreCase(senderDesiredPosition)) {
						boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(node.getId()); //moveTo.replace(0, moveTo.length(), node.getId());
						if(moved) {
							break;
						}
					}
				}
		}
		else { //no one in the corridor
			//TODO 18.4. !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			//Pour l'instant : on est gentil :
			ArrayList<Node>  neighbours = graph.getNode(myPosition).getNeighbours();
			
			//neighbours.remove(senderDesiredPosition);
			//int index = (int)(Math.random()*neighbours.size());
			//moveTo.replace(0, moveTo.length(), neighbours.get(index).getId());
						
			ACLMessage response =new ACLMessage(ACLMessage.AGREE);
			response.setSender(myAgent.getAID());
			response.addReceiver(sender);
			response.setContent("I am nice - go on !");
			((abstractAgent) myAgent).sendMessage(response);
			
			for(Node node :neighbours) {
				if(!node.getId().equalsIgnoreCase(senderDesiredPosition)) {
					boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(node.getId()); //moveTo.replace(0, moveTo.length(), node.getId());
					if(moved) {
						break;
					}
				}
			}
			
		}
	}
	

	
	private void siloSomeoneResolution() {
		// TODO Auto-generated method stub
		//If agents are in the corridor:
				//TODO 18. 4: The agent who is nearer the known crossroad has priority  
				
				//we check if someone is in the corridor:
						if(meInCorridor() && himInCorridor()) {
							//TODO 18.4.: The agent who is nearer the KNOWN crossroad has priority (in theory, both crossroads should be known as the agents are in conflits - they've come normally from 
							//opposite sides)
							unexploredAtExtremities = 0;
							int myDistance = distanceToCrossroad(senderPosition);
							int hisDistance = distanceToCrossroad(myPosition);
							
							if(unexploredAtExtremities == 2) {
								//TODO 18.4.: Agents should share the information and go away in opposites directions to explore the graph at both extremities
								ACLMessage response =new ACLMessage(ACLMessage.REFUSE);
								response.setSender(myAgent.getAID());
								response.addReceiver(sender);
								response.setContent("INTERBLOCAGE: \n"
										+ "you move to : "+hisExtremity);
								((abstractAgent) myAgent).sendMessage(response);
								
								moveTo.replace(0, moveTo.length(), myExtremity);
								
							}
							else { //There will be someone who has priority
								if(myDistance > hisDistance) {
									ACLMessage response =new ACLMessage(ACLMessage.REFUSE);
									response.setSender(myAgent.getAID());
									response.addReceiver(sender);
									response.setContent("INTERBLOCAGE: \n"
											+ "you move to : "+hisExtremity);
									((abstractAgent) myAgent).sendMessage(response);
									System.out.println(";;;;;;;;;;;;;;;;;;; >>Agent : "+myAgent.getLocalName()+"  msg "+response+" sent to "+sender+";;;;;;;;;;;;;;;;;;;;;;");
								}
								if(myDistance <= hisDistance) { //TODO 18.4: If egality? We can be egoistic, gentle or employer the probability/ look if there is someone else approaching...
									ACLMessage response =new ACLMessage(ACLMessage.AGREE);
									response.setSender(myAgent.getAID());
									response.addReceiver(sender);
									response.setContent("INTERBLOCAGE: I'm nearer the crossroad - you have priority, I will make you a way.");
									((abstractAgent) myAgent).sendMessage(response);
									System.out.println("sjasljsaoùdskdùoksdosa myExtremity: "+myExtremity);
									moveTo.replace(0, moveTo.length(), myExtremity);
									System.out.println(";;;;;;;;;;;;;;;;;;; >>Agent : "+myAgent.getLocalName()+"  msg "+response+" sent to "+sender+";;;;;;;;;;;;;;;;;;;;;;");
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
								ArrayList<Node>  neighbours = graph.getNode(myPosition).getNeighbours();
								//neighbours.remove(senderDesiredPosition);
								//int index = (int)(Math.random()*neighbours.size());
								//moveTo.replace(0, moveTo.length(), neighbours.get(index).getId());
								
								/*
								for(Node node :neighbours) {
									if(!node.getId().equalsIgnoreCase(senderDesiredPosition)) {
										moveTo.replace(0, moveTo.length(), node.getId());
									}
								}
								*/
								ACLMessage response =new ACLMessage(ACLMessage.AGREE);
								response.setSender(myAgent.getAID());
								response.addReceiver(sender);
								response.setContent("INTERBLOCAGE: I'm nearer the crossroad - you have priority, I will make you a way.");
								((abstractAgent) myAgent).sendMessage(response);
								
								for(Node node :neighbours) {
									if(!node.getId().equalsIgnoreCase(senderDesiredPosition)) {
										boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(node.getId()); //moveTo.replace(0, moveTo.length(), node.getId());
										if(moved) {
											break;
										}
									}
								}
						}
					
				
						//If agents aren't in the corridor:
						//If the graph is not fully explored, it is the explorer who has priority
						else if(!graphFullyExplored()) {
							ACLMessage response =new ACLMessage(ACLMessage.AGREE);
							response.setSender(myAgent.getAID());
							response.addReceiver(sender);
							response.setContent("INTERBLOCAGE: The graph is not fully explored and I am a silo - you have a priority.");
							((abstractAgent) myAgent).sendMessage(response);
							ArrayList<Node>  neighbours = graph.getNode(myPosition).getNeighbours();
							
							for(Node node :neighbours) {
								if(!node.getId().equalsIgnoreCase(senderDesiredPosition)) {
									boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(node.getId()); //moveTo.replace(0, moveTo.length(), node.getId());
									if(moved) {
										break;
									}
								}
							}
							
						}
						else { //Otherwise, it's collector who has priority
							ACLMessage response =new ACLMessage(ACLMessage.REFUSE);
							response.setSender(myAgent.getAID());
							response.addReceiver(sender);
							response.setContent("INTERBLOCAGE: The graph is fully explored and I am a silo - I have priority.");
							((abstractAgent) myAgent).sendMessage(response);
							
							
					
							//TODO : we will go to the state GivePriorityBehaviour
						}
				
				}
		
	

}
