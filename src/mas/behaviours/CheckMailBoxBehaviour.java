package mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import mas.abstractAgent;
import mas.agents.BFSExploAgent;
import mas.graph.Graph;

public class CheckMailBoxBehaviour extends Behaviour{
	
	private StringBuilder nextState;
	private static final int TIME_OUT = 3;
	public static final int GOTO_STATE_WALK = 0;
	public static final int GOTO_STATE_GRAPH_TRANSMISSION = 1;
	public static final int GOTO_STATE_INTERBLOCAGE_RESOLUTION = 2;
	public static final int GOTO_STATE_INTERBLOCAGE_LISTENER = 4;
	public  static String MESSAGE_GRAPH_RECEIVED = "Message Received";
	private int result;
	private ArrayList<AID> graph_subscribers;
	private Graph graph;
	private ACLMessage interblocageMessage;
	private int timer;
	private StringBuilder previousState;
	
	
	public CheckMailBoxBehaviour(mas.abstractAgent myAgent, Graph graph, StringBuilder nextState, StringBuilder previousState, ArrayList<AID> graph_subscribers, ACLMessage interblocageMessage) {
		super(myAgent);
		this.graph = graph;
		this.nextState = nextState;
		this.graph_subscribers = graph_subscribers;
		this.previousState = previousState;
		this.interblocageMessage = interblocageMessage;
		
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
			case ACLMessage.REQUEST :
				if(msg.getContent().contains("INTERBLOCAGE")) {
					ACLMessage waitMeMsg = new ACLMessage(ACLMessage.CONFIRM);
					waitMeMsg.setSender(myAgent.getAID());
					waitMeMsg.addReceiver(msg.getSender());
					waitMeMsg.setContent("INTERBLOCAGE : I've received your message. Wait me please, I will find a solution.");
					((abstractAgent) myAgent).sendMessage(waitMeMsg);
					
					interblocageMessage = msg;
					result = GOTO_STATE_INTERBLOCAGE_RESOLUTION;
				}
				
				break;
			case ACLMessage.SUBSCRIBE : // Graph request
				System.out.println(myAgent.getLocalName()+"New subscriber");
				graph_subscribers.add(msg.getSender());
				result = GOTO_STATE_GRAPH_TRANSMISSION;
				break;
				
			case ACLMessage.CONFIRM : // Graph reception acknowledgement
				//add user to history ?
				
				//Interblocage information reception acknowledgement, the agent will wait the response 
				if(msg.getContent().contains("INTERBLOCAGE")) {
					result = GOTO_STATE_INTERBLOCAGE_LISTENER;
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
			result = GOTO_STATE_WALK;
		}
		
		
	}

	private void graphReception(ACLMessage msg) {
		try {
			//Graph fusion
			System.out.println(myAgent.getLocalName()+"******************MON GRAPHE AVANT FUSION");
			graph.printNodes();
			System.out.println(myAgent.getLocalName()+"******************GRAPHE RECU");
			((Graph)msg.getContentObject()).printNodes();
			graph.fusion(((Graph)msg.getContentObject()));
			System.out.println(myAgent.getLocalName()+"******************NOUVEAU GRAPHE APRES FUSION");
			graph.printNodes();
			
			//reply with an acknowledgement to the sender
			
			ACLMessage ackn=new ACLMessage(ACLMessage.CONFIRM);
			ackn.setSender(this.myAgent.getAID());
			ackn.addReceiver(msg.getSender());
			ackn.setContent(MESSAGE_GRAPH_RECEIVED);
			((mas.abstractAgent)this.myAgent).sendMessage(ackn);
			//System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
			
			
		} catch (UnreadableException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean done() {
		
		boolean done = false;
		timer++;
		if(timer == TIME_OUT || result != GOTO_STATE_WALK)
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
		else if(timer == TIME_OUT && previousState.toString().equalsIgnoreCase(BFSExploAgent.STATE_START_INTERBLOCAGE)) {
				System.out.println(myAgent.getLocalName()+" We don't have the response to the INTERBLOCAGE request, we go to the Listener to wait some more time...");
				done = true;
				result = GOTO_STATE_INTERBLOCAGE_LISTENER;
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
		this.previousState.replace(0, this.previousState.length(), BFSExploAgent.STATE_CHECK_MAILBOX);
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
