//<<<<<<< HEAD
package mas.behaviours;

import java.util.ArrayList;

import env.EntityType;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import mas.abstractAgent;
import mas.agents.BFSExploAgent;
import mas.agents.CollectorAgent;
import mas.agents.SiloAgent;
import mas.agents.Constants;
import mas.graph.Graph;

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
		sd.setType(Constants.SERVICE_EXP);
		dfd.addServices(sd);
		ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
		msg.setSender(this.myAgent.getAID());
		
		//private ArrayList<AID> receivers2;
		try {
			DFAgentDescription[] result;
			result = DFService.search(myAgent, dfd);
				System.out.println("DFD length : "+result.length);
				for(int i=0; i<result.length; i++)
				{
					//System.out.println("My AID is "+myAgent.getAID() +" and I want to send to "+result[i].getName());
					//System.out.println("new receiver : "+result[i]+" myAgent.getAId() : "+myAgent.getAID());
					if(!result[i].getName().equals(myAgent.getAID()))
					{
						//System.out.println("IN IF ...................");
						System.out.println("new receiver : "+result[i]+" myAgent.getAId() : "+myAgent.getAID());
						msg.addReceiver(result[i].getName());
						
					}
				}
			System.out.println(EntityType.AGENT_COLLECTOR.getName());
			System.out.println(((EntityType)myAgent.getArguments()[1]).getName());
			if(((EntityType)myAgent.getArguments()[1]).getName().equalsIgnoreCase(EntityType.AGENT_COLLECTOR.getName())) {
				System.out.println("In IF .................................................................. ");
				msg.setContent("INTERBLOCAGE DETECTED: \nAgent: "+myAgent.getLocalName()+"\nType: COLL \nBlocked at: "
						+((abstractAgent) myAgent).getCurrentPosition()+"\nWant move to: "+((CollectorAgent) myAgent).getMoveTo());
			}
			else if(((EntityType)myAgent.getArguments()[1]).getName().equalsIgnoreCase(EntityType.AGENT_EXPLORER.getName())) {
				msg.setContent("INTERBLOCAGE DETECTED: \nAgent: "+myAgent.getLocalName()+"\nType: EXPLO \nBlocked at: "
						+((abstractAgent) myAgent).getCurrentPosition()+"\nWant move to: "+((BFSExploAgent) myAgent).getMoveTo());
			}
			else if(((EntityType)myAgent.getArguments()[1]).getName().equalsIgnoreCase(EntityType.AGENT_TANKER.getName())) {
				msg.setContent("INTERBLOCAGE DETECTED: \nAgent: "+myAgent.getLocalName()+"\nType: SILO \nBlocked at: "
						+((abstractAgent) myAgent).getCurrentPosition()+"\nWant move to: "+((SiloAgent) myAgent).getMoveTo());
			}
			
				
			
			//we want the response in 3 seconds
			//TODO 10.4: Define "3 seconds" as a constant (should be the same for each message)
			//msg.setReplyByDate(new Date(System.currentTimeMillis() + 3000));
			System.out.println(">>Agent : "+this.myAgent.getLocalName()+"  msg "+msg+" to be sent");
			//((mas.abstractAgent)this.myAgent).sendMessage(msg);
		
			((mas.abstractAgent)this.myAgent).sendMessage(msg);
			//System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
			
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		this.previousState.replace(0, this.previousState.length(), Constants.STATE_START_INTERBLOCAGE);
		return true;
	}
	
}
//=======
//package mas.behaviours;
//
//import jade.core.behaviours.Behaviour;
//import jade.core.behaviours.SimpleBehaviour;
//import jade.domain.DFService;
//import jade.domain.FIPAException;
//import jade.domain.FIPAAgentManagement.DFAgentDescription;
//import jade.domain.FIPAAgentManagement.ServiceDescription;
//import jade.lang.acl.ACLMessage;
//import jade.lang.acl.MessageTemplate;
//import jade.proto.AchieveREInitiator;
//import mas.abstractAgent;
//import mas.agents.BFSExploAgent;
//import mas.graph.Graph;
//
//import java.io.IOException;
//import java.sql.Date;
//import java.util.ArrayList;
//
//import jade.core.AID;
//
//public class SendInterblocageStartMessageBehaviour extends Behaviour{
//	
//	private static final long serialVersionUID =8688081240099240575L;
//	private Graph graph;
//	private ArrayList<AID> receivers;
//	private ACLMessage msg;
//	private String type;
//	
//	
//	public SendInterblocageStartMessageBehaviour(final mas.abstractAgent myAgent, Graph graph, ArrayList<AID> recievers, ACLMessage interblocageMessage, String type) {
//		super(myAgent);
//		this.graph = graph;
//	    this.receivers = recievers;
//	    this.msg = interblocageMessage;
//	    this.type = type;
//	}
//	
//	@Override
//	public void action() {
//		// TODO Auto-generated method stub
//		System.out.println("Send Interblocage Bevahivour*************************");
//		DFAgentDescription dfd = new DFAgentDescription();
//		ServiceDescription sd = new ServiceDescription();
//		sd.setType(type);
//		dfd.addServices(sd);
//		ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
//		msg.setSender(this.myAgent.getAID());
//		
//		//private ArrayList<AID> receivers2;
//		try {
//			DFAgentDescription[] result;
//			result = DFService.search(myAgent, dfd);
//			
//				for(int i=0; i<result.length; i++)
//				{
//					System.out.println("My AID is "+myAgent.getAID() +" and I want to send to "+result[i].getName());
//					if(!result[i].getName().equals(myAgent.getAID()))
//					{
//						msg.addReceiver(result[i].getName());
//						
//					}
//				}
//
//			
//			msg.setContent("whyyyyyyyyyy");
//				
//			
//			//we want the response in 3 seconds
//			//TODO 10.4: Define "3 seconds" as a constant (should be the same for each message)
//			msg.setReplyByDate(new Date(System.currentTimeMillis() + 3000));
//			System.out.println(">>Agent : "+this.myAgent.getLocalName()+"  msg "+msg+" to be sent");
//			//((mas.abstractAgent)this.myAgent).sendMessage(msg);
//			((mas.abstractAgent)this.myAgent).sendMessage(msg);
//			System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
//			
//		} catch (FIPAException e) {
//			e.printStackTrace();
//		}
//	}
//
//
//	@Override
//	public boolean done() {
//		// TODO Auto-generated method stub
//		return true;
//	}
///*
//	@Override
//	public void action() {
//		// TODO Auto-generated method stub
//		//String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();
//				System.out.println("************************SendGraphBehaviour****************************");
//				//send graph for the 2-neighbours
//				
//				ACLMessage msg=new ACLMessage(ACLMessage.PROPOSE);
//				msg.setSender(this.myAgent.getAID());
//				
//			
//				
//				DFAgentDescription dfd = new DFAgentDescription();
//				ServiceDescription sd = new ServiceDescription();
//				sd.setType(BFSExploAgent.SERVICE_EXP);
//				dfd.addServices(sd);
//				
//				try {
//					DFAgentDescription[] result;
//					result = DFService.search(myAgent, dfd);
//										
//					for(AID receiver : receivers)
//					{
//						msg.addReceiver(receiver);
//					}
//					msg.setContent("INTERBLOCAGE: \n Agent: "+myAgent.getLocalName()+"\n blocked at:" +((abstractAgent) myAgent).getCurrentPosition()+"\n want move to :"+moveTo);
//					((mas.abstractAgent)this.myAgent).sendMessage(msg);
//					System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
//				} catch (FIPAException e) {
//					e.printStackTrace();
//				}
//				
//	}
//
//	@Override
//	public boolean done() {
//		// TODO Auto-generated method stub
//		return true;
//	}
//	*/
//
//
//	
//}
//>>>>>>> master
