package mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import mas.agents.BFSExploAgent;
import mas.graph.Graph;

public class GraphRequestBehaviour extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3774372518726847984L;
	

	public GraphRequestBehaviour(final Agent myagent) {
		super(myagent);
	}
	
	@Override
	public void action() {
		
		System.out.println(myAgent.getLocalName()+"************************GraphRequestBehaviour****************************");
		
		ACLMessage msg=new ACLMessage(ACLMessage.SUBSCRIBE);
		msg.setSender(this.myAgent.getAID());
		
		
		
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(BFSExploAgent.SERVICE_EXP);
		dfd.addServices(sd);
		
		try {
			DFAgentDescription[] result;
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
			
			msg.setContent("Send me your graph");
			((mas.abstractAgent)this.myAgent).sendMessage(msg);
			System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		
	}

	@Override
	public boolean done() {
		return true;
	}

}
