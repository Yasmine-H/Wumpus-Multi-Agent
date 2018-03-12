package mas.behaviours;

import java.io.IOException;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import mas.agents.BFSExploAgent;
import mas.graph.Graph;

public class SendGraphBehaviour extends SimpleBehaviour{

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
	
	public SendGraphBehaviour (final Agent myagent, Graph graph) {
		super(myagent);
		this.graph=graph;
		//super(myagent);
	}

	@Override
	public void action() {
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();
		System.out.println("************************SendMessageBehaviour****************************");
		//send graph for the 2-neighbours
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		
		
		
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(BFSExploAgent.SERVICE_EXP);
		dfd.addServices(sd);
		DFAgentDescription[] result;
		try {
			result = DFService.search(myAgent, dfd);
			System.out.println("Number of agents : "+result.length);
			
			for(int i=0; i<result.length; i++)
			{
				System.out.println("My AID is "+myAgent.getAID() +" and I want to send to "+result[i].getName());
				if(!result[i].getName().equals(myAgent.getAID()))
				{
					msg.addReceiver(result[i].getName());
				}
			}
		} catch (FIPAException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		
		
		
		try {
			msg.setContentObject(graph);
			((mas.abstractAgent)this.myAgent).sendMessage(msg);
			//System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
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