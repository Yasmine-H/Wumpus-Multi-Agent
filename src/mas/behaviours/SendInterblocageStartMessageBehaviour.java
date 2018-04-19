package mas.behaviours;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import mas.abstractAgent;
import mas.agents.BFSExploAgent;
import mas.graph.Graph;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;

import jade.core.AID;

public class SendInterblocageStartMessageBehaviour extends Behaviour{
	
	private static final long serialVersionUID =8688081240099240575L;
	private Graph graph;
	private ArrayList<AID> receivers;
	private StringBuilder previousState;
	
	
	public SendInterblocageStartMessageBehaviour(final mas.abstractAgent myAgent, Graph graph, ArrayList<AID> recievers, StringBuilder previousState) {
		super(myAgent);
		this.graph = graph;
	    this.receivers = recievers;
	    this.previousState = previousState;
	    
	    
	    
	}
	
	@Override
	public void action() {
		// TODO Auto-generated method stub
		System.out.println("Send Interblocage Bevahivour*************************");
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(BFSExploAgent.SERVICE_EXP);
		dfd.addServices(sd);
		ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
		msg.setSender(this.myAgent.getAID());
		
		//private ArrayList<AID> receivers2;
		try {
			DFAgentDescription[] result;
			result = DFService.search(myAgent, dfd);
			
				for(int i=0; i<result.length; i++)
				{
					System.out.println("My AID is "+myAgent.getAID() +" and I want to send to "+result[i].getName());
					if(!result[i].getName().equals(myAgent.getAID()))
					{
						msg.addReceiver(result[i].getName());
						
					}
				}

			
			msg.setContent("INTERBLOCAGE DETECTED: \nAgent: "+myAgent.getLocalName()+"\nType: EXPLO \nBlocked at: "
							+((abstractAgent) myAgent).getCurrentPosition()+"\nWant move to: "+((BFSExploAgent) myAgent).getMoveTo());
				
			
			//we want the response in 3 seconds
			//TODO 10.4: Define "3 seconds" as a constant (should be the same for each message)
			msg.setReplyByDate(new Date(System.currentTimeMillis() + 3000));
			System.out.println(">>Agent : "+this.myAgent.getLocalName()+"  msg "+msg+" to be sent");
			//((mas.abstractAgent)this.myAgent).sendMessage(msg);
			((mas.abstractAgent)this.myAgent).sendMessage(msg);
			System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
			
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		this.previousState.replace(0, this.previousState.length(), BFSExploAgent.STATE_START_INTERBLOCAGE);
		return true;
	}
	
}
