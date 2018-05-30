//<<<<<<< HEAD
//package mas.behaviours;
//
//import java.util.ArrayList;
//
//import jade.core.AID;
//import jade.core.Agent;
//import jade.core.behaviours.SimpleBehaviour;
//import jade.lang.acl.ACLMessage;
//import jade.lang.acl.MessageTemplate;
//import jade.lang.acl.UnreadableException;
//import mas.graph.Graph;
//
//
///**
// * Waits for the graphs he asked for and perform a fusion between his and the ones he receives. 
// * Sends an acknowledgement to the sender once the fusion is completed. 
// */
//
//
//public class ReceiveGraphBehaviour extends SimpleBehaviour{
//
//	
//	private static final long serialVersionUID = -2058134622078521998L;
//	
//	private Graph graph;
//	private ArrayList<AID> senders;
//	private int timer = 0 ;
//	private int time_limit = 3; //TODO 5/04/2018: define the time limit properly
//	
//	
//	
//	public ReceiveGraphBehaviour (final Agent myagent, Graph graph, ArrayList<AID> senders) {
//		super(myagent);
//		this.graph=graph;
//		this.senders=senders;
//		this.time_limit = senders.size()+2;
//		
//	}
//
//	@Override
//	public void action() {
//		
//		System.out.println("************************ReceiveGraphBehaviour****************************");
//		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM); // TODO 05/04/2018: what if there were other types of msgs.confirm and the one wetreated here wasn't the right one (and we would remove it from the stack) 
//		ACLMessage msg = myAgent.receive(mt);
//
//		if(msg!=null && senders.contains(msg.getSender())) // we only take the messages from the senders we're interested in
//		{
//			// TODO 28.2 : fuse the current graph with the one received
//			System.out.println(">>Agent : "+myAgent.getLocalName()+"  new msg received "+msg);
//			
//			try {
//				//Graph fusion
//				System.out.println(myAgent.getLocalName()+"******************MON GRAPHE AVANT FUSION");
//				graph.printNodes();
//				System.out.println(myAgent.getLocalName()+"******************GRAPHE RECU");
//				((Graph)msg.getContentObject()).printNodes();
//				graph.fusion(((Graph)msg.getContentObject()));
//				System.out.println(myAgent.getLocalName()+"******************NOUVEAU GRAPHE APRES FUSION");
//				graph.printNodes();
//				senders.remove(msg.getSender());
//				
//				//reply with an acknowledgement to the sender
//				
//				ACLMessage ackn=new ACLMessage(ACLMessage.CONFIRM);
//				ackn.setSender(this.myAgent.getAID());
//				ackn.addReceiver(msg.getSender());
//				ackn.setContent(CheckMailBoxBehaviour.MESSAGE_GRAPH_RECEIVED);
//				((mas.abstractAgent)this.myAgent).sendMessage(ackn);
//				//System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
//				
//				
//			} catch (UnreadableException e) {
//				e.printStackTrace();
//			}
//			
//			timer = 0;
//		}
//		else
//		{
//			timer++;
//			// TODO 5/04/2018: ?
//		}
//		timer++;
//		
//	}
//
//	
//
//	@Override
//	public boolean done() {
//		return (senders.isEmpty() || timer==time_limit);
//	}
//	
//	@Override
//	public int onEnd() {
//		timer = 0;
//		
//		return super.onEnd();
//	}
//
//=======
package mas.behaviours;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import mas.agents.Constants;
import mas.graph.Graph;


/**
 * Waits for the graphs he asked for and perform a fusion between his and the ones he receives. 
 * Sends an acknowledgement to the sender once the fusion is completed. 
 */


public class ReceiveGraphBehaviour extends SimpleBehaviour{

	
	private static final long serialVersionUID = -2058134622078521998L;
	
	private Graph graph;
	private ArrayList<AID> senders;
	private int timer = 0 ;
	private int time_limit = 3; //TODO 5/04/2018: define the time limit properly
	
	
	
	public ReceiveGraphBehaviour (final Agent myagent, Graph graph, ArrayList<AID> senders) {
		super(myagent);
		this.graph=graph;
		this.senders=senders;
		this.time_limit = senders.size()+2;
		
	}

	@Override
	public void action() {
		
		System.out.println("************************ReceiveGraphBehaviour****************************");
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM); // TODO 05/04/2018: what if there were other types of msgs.confirm and the one wetreated here wasn't the right one (and we would remove it from the stack) 
		ACLMessage msg = myAgent.receive(mt);

		if(msg!=null && senders.contains(msg.getSender())) // we only take the messages from the senders we're interested in
		{
			// TODO 28.2 : fuse the current graph with the one received
			System.out.println(">>Agent : "+myAgent.getLocalName()+"  new msg received "+msg);
			
			try {
				//Graph fusion
				System.out.println(myAgent.getLocalName()+"******************MON GRAPHE AVANT FUSION");
				graph.printNodes();
				System.out.println(myAgent.getLocalName()+"******************GRAPHE RECU");
				((Graph)msg.getContentObject()).printNodes();
				graph.fusion(((Graph)msg.getContentObject()));
				System.out.println(myAgent.getLocalName()+"******************NOUVEAU GRAPHE APRES FUSION");
				graph.printNodes();
				senders.remove(msg.getSender());
				
				//reply with an acknowledgement to the sender
				
				ACLMessage ackn=new ACLMessage(ACLMessage.CONFIRM);
				ackn.setSender(this.myAgent.getAID());
				ackn.addReceiver(msg.getSender());
				ackn.setContent(Constants.MESSAGE_GRAPH_RECEIVED);
				((mas.abstractAgent)this.myAgent).sendMessage(ackn);
				//System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
				
				
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			
			timer = 0;
		}
		else
		{
			timer++;
			// TODO 5/04/2018: ?
		}
		timer++;
		
	}

	

	@Override
	public boolean done() {
		return (senders.isEmpty() || timer==time_limit);
	}
	
	@Override
	public int onEnd() {
		timer = 0;
		
		return super.onEnd();
	}

//>>>>>>> master
}