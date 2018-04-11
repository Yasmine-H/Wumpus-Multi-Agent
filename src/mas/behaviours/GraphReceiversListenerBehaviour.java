package mas.behaviours;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.graph.Graph;

/**
 * Waits for replies from the graph proposal sent before and registers every new receiver to send him the complete graph later.
 * 
 */

public class GraphReceiversListenerBehaviour extends SimpleBehaviour {

	
	private static final long serialVersionUID = -8495714732465971286L;
	
	public static final int RECEIVERS_EMPTY = 0;
	public static final int RECEIVERS_NOT_EMPTY = 1;
	
	private static final int TIME_LIMIT = 3;
	
	private int timer = 0;
	private ArrayList<AID> receivers; 
	
	
	public GraphReceiversListenerBehaviour(final mas.abstractAgent myagent, Graph graph, ArrayList<AID> receivers) {
		super(myagent);
		this.receivers=receivers;
	}
	
	@Override
	public void action() {
		System.out.println("************************GraphReceiversListenerBehaviour****************************");
		
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
		ACLMessage msg = myAgent.receive(mt);

		if(msg!=null)
		{
			receivers.add(msg.getSender());
			System.out.println("new receiver added! "+msg.getSender().getLocalName());
			System.out.println("new receivers list size : "+receivers.size());
			timer=0;
		}
		else
		{
			timer++;
		}
		


	}

	@Override
	public boolean done() {
		return timer==TIME_LIMIT;
	}
	
	@Override
	public int onEnd() {
		timer=0;
		if(receivers.isEmpty())
			return RECEIVERS_EMPTY;
		else
			return RECEIVERS_NOT_EMPTY;
	}

}
