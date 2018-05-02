package mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import mas.graph.Graph;

public class CheckMailBoxBehaviour extends Behaviour{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4296916477792239941L;
	private String nextState;
	private static final int TIME_OUT = 3;
	public static final int GOTO_STATE_WALK = 0;
	public static final int GOTO_STATE_GRAPH_TRANSMISSION = 1;
	public static final int GOTO_STATE_INTERBLOCAGE_RESOLUTION = 2;
	public  static String MESSAGE_GRAPH_RECEIVED = "Message Received";
	private int result;
	private ArrayList<AID> graph_subscribers;
	private Graph graph;
	private int timer;
	
	public CheckMailBoxBehaviour(mas.abstractAgent myAgent, Graph graph, String nextState, ArrayList<AID> graph_subscribers) {
		super(myAgent);
		this.graph = graph;
		this.nextState = nextState;
		this.graph_subscribers = graph_subscribers;
		result = GOTO_STATE_WALK;
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
					result = GOTO_STATE_INTERBLOCAGE_RESOLUTION;
				}
				
				break;
			case ACLMessage.SUBSCRIBE : // Graph request
				System.out.println(myAgent.getLocalName()+"New subscriber");
				if(!graph_subscribers.contains(msg.getSender()))
				graph_subscribers.add(msg.getSender());
				result = GOTO_STATE_GRAPH_TRANSMISSION;
				break;
				
			case ACLMessage.CONFIRM : // Graph reception acknowledgement
				//add user to history ?
				break;
				
			case ACLMessage.INFORM : // New graph reception
				System.out.println(myAgent.getLocalName()+"*******New Graph received !!");
				graphReception(msg);
				break;
			}
			
		}
		else
		{
			System.out.println(myAgent.getLocalName()+" No more msgs :(");
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
		System.out.println(">>>>>Agent : "+myAgent.getLocalName()+" time : "+timer+"  and result : "+result);
		if(timer == TIME_OUT || result != GOTO_STATE_WALK)
		{
			done = true;
			timer = 0;
			//graph_subscribers.clear();
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
