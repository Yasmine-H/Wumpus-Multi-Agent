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
//	public static final int GOTO_STATE_WALK = 0;
//	public static final int GOTO_STATE_GRAPH_TRANSMISSION = 1;
//	public static final int GOTO_STATE_INTERBLOCAGE_RESOLUTION = 2;
//	public static final int GOTO_STATE_INTERBLOCAGE_LISTENER = 4;
//	public static final int GOTO_GIVES_PRIORITY = 5;
//	public  static String MESSAGE_GRAPH_RECEIVED = "Message Received";
	private int result;
//	private ArrayList<AID> graph_subscribers;
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
					//if(((EntityType)myAgent.getArguments()[1]).getName().equalsIgnoreCase(EntityType.AGENT_COLLECTOR.getName())) {
					//	((CollectorAgent) myAgent).setInterblocageMessage(msg);
					//}
					//else if(((EntityType)myAgent.getArguments()[1]).getName().equalsIgnoreCase(EntityType.AGENT_EXPLORER.getName())) {
					//	((BFSExploAgent) myAgent).setInterblocageInCours(true);
					//}
					//else if(((EntityType)myAgent.getArguments()[1]).getName().equalsIgnoreCase(EntityType.AGENT_TANKER.getName())) {
					//	((SiloAgent) myAgent).setInterblocageMessage(msg);
					//}
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
//			case ACLMessage.SUBSCRIBE : // Graph request
//				System.out.println(myAgent.getLocalName()+"New subscriber");
//				graph_subscribers.add(msg.getSender());
//				result = Constants.GOTO_STATE_GRAPH_TRANSMISSION;
//				break;
//				
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
		 /*TODO 17.4.: If we have come from the SendInterblocageStateMessage, logically, we are requiring a response here
		 * 			   If we don't have this response, what should we do?  
		 * 			   For instance, I do to the listener to give the other agent even more time - cf. else if
		 * 			   Another propositions : 	- try to move 
		 * 									    - resend the message 
		 */
		else if(timer == TIME_OUT && previousState.toString().equalsIgnoreCase(Constants.STATE_START_INTERBLOCAGE)) {
				//System.out.println(myAgent.getLocalName()+" We don't have the response to the INTERBLOCAGE request, we go to the Listener to wait some more time...");
				done = true;
				result = Constants.GOTO_STATE_INTERBLOCAGE_LISTENER;
		}
		
		try {
			System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return done;
	}
	
	public int onEnd() {
		this.previousState.replace(0, this.previousState.length(), Constants.STATE_CHECK_MAILBOX);
		return result;
		/*
		if(result != -1)
			return result;
		else
			return GOTO_STATE_WALK;
		/*
		//else - there was no message, we continue to the nextState 
		if(nextState.equalsIgnoreCase(BFSExploAgent.STATE_GRAPH_PROPOSITION))
			return GOTO_STATE_GRAPH_PROPOSITION;
		else if(nextState.equalsIgnoreCase(BFSExploAgent.STATE_WALK))
			return GOTO_STATE_WALK;
		else if(nextState.equalsIgnoreCase(BFSExploAgent.STATE_INTERBLOCAGE_RESOLUTION))
			return GOTO_STATE_INTERBLOCAGE_RESOLUTION;
		//TODO 10.4.: Try to find nicer solution/default state
		else return -1;
		*/
	}

}
//=======
//package mas.behaviours;
//
//import java.io.IOException;
//import java.util.ArrayList;
//
//import jade.core.AID;
//import jade.core.behaviours.Behaviour;
//import jade.lang.acl.ACLMessage;
//import jade.lang.acl.UnreadableException;
//import mas.agents.Constants;
//import mas.graph.Graph;
//
//public class CheckMailBoxBehaviour extends Behaviour{
//	
//	/*
//	private static final int TIME_OUT = 3;
//	public static final int GOTO_STATE_WALK = 0;
//	public static final int GOTO_STATE_GRAPH_TRANSMISSION = 1;
//	public static final int GOTO_STATE_INTERBLOCAGE_RESOLUTION = 2;
//	public  static String MESSAGE_GRAPH_RECEIVED = "Message Received";
//	*/
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 4296916477792239941L;
//	private String nextState;
//	private int result;
//	private ArrayList<AID> graph_subscribers;
//	private Graph graph;
//	private int timer;
//	
//	public CheckMailBoxBehaviour(mas.abstractAgent myAgent, Graph graph, String nextState, ArrayList<AID> graph_subscribers) {
//		super(myAgent);
//		this.graph = graph;
//		this.nextState = nextState;
//		this.graph_subscribers = graph_subscribers;
//		result = Constants.GOTO_STATE_WALK;
//		timer = 0;
//	}
//	
//	@Override
//	public void action() {
//		// TODO Auto-generated method stub
//		System.out.println(myAgent.getLocalName()+"************************CheckMailBoxBehaviour****************************");
//		ACLMessage msg = myAgent.receive();
//		
//		if(msg != null) {
//			
//			System.out.println(myAgent.getLocalName()+"*******New message from *******"+msg.getSender().toString()+" content :"+msg.getContent());
//			switch(msg.getPerformative()){
//			case ACLMessage.REQUEST :
//				if(msg.getContent().contains("INTERBLOCAGE")) {
//					result = Constants.GOTO_STATE_INTERBLOCAGE_RESOLUTION;
//				}
//				
//				break;
//			case ACLMessage.SUBSCRIBE : // Graph request
//				System.out.println(myAgent.getLocalName()+"New subscriber");
//				if(!graph_subscribers.contains(msg.getSender()))
//				graph_subscribers.add(msg.getSender());
//				result = Constants.GOTO_STATE_GRAPH_TRANSMISSION;
//				break;
//				
//			case ACLMessage.CONFIRM : // Graph reception acknowledgement
//				//add user to history ?
//				break;
//				
//			case ACLMessage.INFORM : // New graph reception
//				System.out.println(myAgent.getLocalName()+"*******New Graph received !!");
//				graphReception(msg);
//				break;
//			}
//			
//		}
//		else
//		{
//			System.out.println(myAgent.getLocalName()+" No more msgs :(");
//			result = Constants.GOTO_STATE_WALK;
//		}
//		
//		
//	}
//
//	private void graphReception(ACLMessage msg) {
//		try {
//			//Graph fusion
//			System.out.println(myAgent.getLocalName()+"******************MON GRAPHE AVANT FUSION");
//			graph.printNodes();
//			System.out.println(myAgent.getLocalName()+"******************GRAPHE RECU");
//			((Graph)msg.getContentObject()).printNodes();
//			graph.fusion(((Graph)msg.getContentObject()));
//			System.out.println(myAgent.getLocalName()+"******************NOUVEAU GRAPHE APRES FUSION");
//			graph.printNodes();
//			
//			//reply with an acknowledgement to the sender
//			
//			ACLMessage ackn=new ACLMessage(ACLMessage.CONFIRM);
//			ackn.setSender(this.myAgent.getAID());
//			ackn.addReceiver(msg.getSender());
//			ackn.setContent(Constants.MESSAGE_GRAPH_RECEIVED);
//			((mas.abstractAgent)this.myAgent).sendMessage(ackn);
//			//System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
//			
//			
//		} catch (UnreadableException e) {
//			e.printStackTrace();
//		}
//		
//	}
//
//	@Override
//	public boolean done() {
//		
//		boolean done = false;
//		timer++;
//		System.out.println(">>>>>Agent : "+myAgent.getLocalName()+" time : "+timer+"  and result : "+result);
//		if(timer == Constants.TIME_OUT || result != Constants.GOTO_STATE_WALK)
//		{
//			done = true;
//			timer = 0;
//			//graph_subscribers.clear();
//		}
//		
//		try {
//			System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
//			System.in.read();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		
//		return done;
//	}
//	
//	public int onEnd() {
//		return result;
//		/*
//		if(result != -1)
//			return result;
//		else
//			return GOTO_STATE_WALK;
//		/*
//		//else - there was no message, we continue to the nextState 
//		if(nextState.equalsIgnoreCase(BFSExploAgent.STATE_GRAPH_PROPOSITION))
//			return GOTO_STATE_GRAPH_PROPOSITION;
//		else if(nextState.equalsIgnoreCase(BFSExploAgent.STATE_WALK))
//			return GOTO_STATE_WALK;
//		else if(nextState.equalsIgnoreCase(BFSExploAgent.STATE_INTERBLOCAGE_RESOLUTION))
//			return GOTO_STATE_INTERBLOCAGE_RESOLUTION;
//		//TODO 10.4.: Try to find nicer solution/default state
//		else return -1;
//		*/
//	}
//
//}
//>>>>>>> master
