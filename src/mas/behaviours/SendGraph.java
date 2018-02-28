package mas.behaviours;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import mas.graph.Graph;

public class SendGraph extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;

	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	
	Graph graph;
	
	public SendGraph (final Agent myagent, Graph graph) {
		super(myagent);
		this.graph=graph;
		//super(myagent);
	}

	@Override
	public void action() {
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();

		//send graph for the 2-neighbours
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		try {
			msg.setContentObject(graph);
			((mas.abstractAgent)this.myAgent).sendMessage(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}