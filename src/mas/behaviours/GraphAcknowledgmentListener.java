package mas.behaviours;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.graph.Graph;

/**
 * Saves history of the graph he sent to the receivers that replied with an acknowledgement
 */

public class GraphAcknowledgmentListener extends Behaviour {

	
	private static final long serialVersionUID = -1455093756660285000L;

	public static final String MSG_GRAPH_RECEIVED = "Graph received";
	private static final int TIME_LIMIT = 2;
	
	private int timer = 0;
	

	
	public GraphAcknowledgmentListener(final mas.abstractAgent myagent, Graph graph) {
		super(myagent);
	}
	
	@Override
	public void action() {
		System.out.println("************************GraphAcknowledgementBehaviour****************************");
		
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
		ACLMessage msg = myAgent.receive(mt);

		if(msg!=null && msg.getContent().equals(MSG_GRAPH_RECEIVED))
		{
			//TODO 05/04/2018 : save info about what has been sent to the agent (the sender) for next time, and take it into consideration when sending a proposal?
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
		return super.onEnd();
	}
	
}
