//<<<<<<< HEAD
package mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;

import env.EntityType;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import mas.abstractAgent;
import mas.agents.BFSExploAgent;
import mas.agents.CollectorAgent;
import mas.agents.Constants;
import mas.agents.SiloAgent;
import mas.graph.Graph;
import mas.graph.Node;

public class CheckMailBoxBehaviour extends Behaviour{
	
	private StringBuilder nextState;
	private static final int TIME_OUT = 3;
	private int result;
	private Graph graph;
	private ACLMessage interblocageMessage;
	private int timer;
	private StringBuilder previousState;
	
	private StringBuilder moveTo;
	
	
	public CheckMailBoxBehaviour(mas.abstractAgent myAgent, Graph graph, StringBuilder nextState, StringBuilder previousState,/* ArrayList<AID> graph_subscribers,*/ ACLMessage interblocageMessage, StringBuilder moveTo) {
		super(myAgent);
		this.graph = graph;
		this.nextState = nextState;
//		this.graph_subscribers = graph_subscribers;
		this.previousState = previousState;
		this.interblocageMessage = interblocageMessage;
		this.moveTo = moveTo;
		
		result = -1;
		timer = 0;
	}
	
	@Override
	public void action() {
		// TODO Auto-generated method stub
		System.out.println(myAgent.getLocalName()+"************************CheckMailBoxBehaviour****************************");
		ACLMessage msg = myAgent.receive();
		
		if(msg != null) {
			System.out.println(myAgent.getLocalName()+"*******New message from *******"+msg.getSender().toString()+" content :"+msg.getContent());
			switch(msg.getPerformative()){
			case ACLMessage.AGREE : //INTERBLOCAGE RESOLUTION - we have a priority
				//System.out.println(myAgent.getLocalName()+" has priority! It will move.");
				result = Constants.GOTO_STATE_WALK;
				break;
			case ACLMessage.REFUSE : //
				//System.out.println(myAgent.getLocalName()+" has to give priority! It will go to the state GivePriorityBehaviour.");
				if(msg.getContent().contains("move to :")) {
					String[] lineParts = msg.getContent().split(":");
					moveTo.replace(0, moveTo.length(), lineParts[lineParts.length - 1].trim());	
				}
				else {
					ArrayList<Node> neighbours = graph.getAllNodes();
					for(Node node :neighbours) {
						//System.out.println("node to try to move : "+node.getId());
						boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(node.getId()); //moveTo.replace(0, moveTo.length(), node.getId());
						if(moved) {
							//System.out.println("move successfull");
							break;
						}
					}
				}
				result = Constants.GOTO_STATE_WALK;
				break;
			case ACLMessage.REQUEST :
				if(msg.getContent().contains("INTERBLOCAGE")) {
					//ACLMessage waitMeMsg = new ACLMessage(ACLMessage.CONFIRM);
					//waitMeMsg.setSender(myAgent.getAID());
					//waitMeMsg.addReceiver(msg.getSender());
					//waitMeMsg.setContent("INTERBLOCAGE : I've received your message. Wait me please, I will find a solution.");
					//((abstractAgent) myAgent).sendMessage(waitMeMsg);
					
					interblocageMessage = msg;
					if(((EntityType)myAgent.getArguments()[1]).getName().equalsIgnoreCase(EntityType.AGENT_COLLECTOR.getName())) {
						((CollectorAgent) myAgent).setInterblocageMessage(msg);
					}
					else if(((EntityType)myAgent.getArguments()[1]).getName().equalsIgnoreCase(EntityType.AGENT_EXPLORER.getName())) {
						((BFSExploAgent) myAgent).setInterblocageMessage(msg);
					}
					else if(((EntityType)myAgent.getArguments()[1]).getName().equalsIgnoreCase(EntityType.AGENT_TANKER.getName())) {
						((SiloAgent) myAgent).setInterblocageMessage(msg);
					}
					//((BFSExploAgent) myAgent).setInterblocageMessage(msg);
					result = Constants.GOTO_STATE_INTERBLOCAGE_RESOLUTION;
				}
				
				break;
			case ACLMessage.CONFIRM : // Graph reception acknowledgement
				//add user to history ?
				
				//Interblocage information reception acknowledgement, the agent will wait the response 
				if(msg.getContent().contains("INTERBLOCAGE")) {
					result = Constants.GOTO_STATE_INTERBLOCAGE_LISTENER;
				}
				break;
				
			case ACLMessage.INFORM : // New graph reception
				System.out.println(myAgent.getLocalName()+"*******New Graph received !!");
				graphReception(msg);
				break;
			
			}
			
		}
		else
		{
			System.out.println(myAgent.getLocalName()+"No more msgs :(");
			result = Constants.GOTO_STATE_WALK;
		}
		
		
	}

	private void graphReception(ACLMessage msg) {
		try {
			//Graph fusion
			//System.out.println(myAgent.getLocalName()+"******************MON GRAPHE AVANT FUSION");
			graph.printNodes();
			//System.out.println(myAgent.getLocalName()+"******************GRAPHE RECU");
			((Graph)msg.getContentObject()).printNodes();
			graph.fusion(((Graph)msg.getContentObject()));
			//System.out.println(myAgent.getLocalName()+"******************NOUVEAU GRAPHE APRES FUSION");
			graph.printNodes();
			
			//reply with an acknowledgement to the sender
			
			ACLMessage ackn=new ACLMessage(ACLMessage.CONFIRM);
			ackn.setSender(this.myAgent.getAID());
			ackn.addReceiver(msg.getSender());
			ackn.setContent(Constants.MESSAGE_GRAPH_RECEIVED);
			((mas.abstractAgent)this.myAgent).sendMessage(ackn);
			//System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
			moveTo.replace(0, moveTo.length(), randomUnexplored());
			
		} catch (UnreadableException e) {
			e.printStackTrace();
		}
		
	}
	
	public String randomUnexplored() {
		ArrayList<Node> adepts = new ArrayList<>();
		ArrayList<Node> adepts2 = new ArrayList<>();
		
		for(Node node : graph.getAllNodes()) {
			if (!node.getVisited()) {
				adepts.add(node);
			}
			if(node.getNeighbours().size() > 2) {
				adepts2.add(node);
			}
		}
		
		if(adepts.size() > 0) {
			int index = (int)(Math.random()*adepts.size());
			return adepts.get(index).getId();
		}
		
		else {
			int index = (int)(Math.random()*adepts2.size());
			return adepts2.get(index).getId();
		}
	}

	@Override
	public boolean done() {
		
		boolean done = false;
		timer++;
		if(timer == TIME_OUT || result != Constants.GOTO_STATE_WALK)
		{
			done = true;
			timer = 0;
		}
		else if(timer == TIME_OUT && previousState.toString().equalsIgnoreCase(Constants.STATE_START_INTERBLOCAGE)) {
				//System.out.println(myAgent.getLocalName()+" We don't have the response to the INTERBLOCAGE request, we go to the Listener to wait some more time...");
				done = true;
				result = Constants.GOTO_STATE_INTERBLOCAGE_LISTENER;
		}
		
//		try {
//			System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
//			System.in.read();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		
		return done;
	}
	
	public int onEnd() {
		this.previousState.replace(0, this.previousState.length(), Constants.STATE_CHECK_MAILBOX);
		return result;
	}

}
