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
	private ACLMessage msg;
	
	
	public SendInterblocageStartMessageBehaviour(final mas.abstractAgent myAgent, Graph graph, ArrayList<AID> recievers, ACLMessage interblocageMessage) {
		super(myAgent);
		this.graph = graph;
	    this.receivers = recievers;
	    this.msg = interblocageMessage;
	    
	}
	
	@Override
	public void action() {
		// TODO Auto-generated method stub
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(BFSExploAgent.SERVICE_EXP);
		dfd.addServices(sd);
		
		try {
			DFAgentDescription[] result;
			result = DFService.search(myAgent, dfd);
								
			for(AID receiver : receivers)
			{
				msg.addReceiver(receiver);
			}
			
			//we want the response in 3 seconds
			msg.setReplyByDate(new Date(System.currentTimeMillis() + 3000));
			((mas.abstractAgent)this.myAgent).sendMessage(msg);
			System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
			
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}


	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
/*
	@Override
	public void action() {
		// TODO Auto-generated method stub
		//String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();
				System.out.println("************************SendGraphBehaviour****************************");
				//send graph for the 2-neighbours
				
				ACLMessage msg=new ACLMessage(ACLMessage.PROPOSE);
				msg.setSender(this.myAgent.getAID());
				
			
				
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(BFSExploAgent.SERVICE_EXP);
				dfd.addServices(sd);
				
				try {
					DFAgentDescription[] result;
					result = DFService.search(myAgent, dfd);
										
					for(AID receiver : receivers)
					{
						msg.addReceiver(receiver);
					}
					msg.setContent("INTERBLOCAGE: \n Agent: "+myAgent.getLocalName()+"\n blocked at:" +((abstractAgent) myAgent).getCurrentPosition()+"\n want move to :"+moveTo);
					((mas.abstractAgent)this.myAgent).sendMessage(msg);
					System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
				} catch (FIPAException e) {
					e.printStackTrace();
				}
				
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return true;
	}
	*/


	
}
