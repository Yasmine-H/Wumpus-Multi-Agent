package mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.graph.Graph;

public class ReceiveGraph extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;

	private Graph graph;
	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	
	public ReceiveGraph (final Agent myagent, Graph graph) {
		super(myagent);
		this.graph=graph;
		//super(myagent);
	}

	@Override
	public void action() {
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();

		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage msg = myAgent.receive(mt);

		if(msg!=null)
		{
			// TODO 28.2 : fuse the current graph with the one received
		}
		else
		{
			// stop the conversation and do something else
		}
		
	}

	

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}