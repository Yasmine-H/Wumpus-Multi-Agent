package mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import mas.agents.BFSExploAgent;
import mas.graph.Graph;
import mas.graph.Node;

/**
 * Waits for graph proposals and compares the samples received with his graph to send a reply or not. 
 */

public class GraphSendersListenerBehaviour extends SimpleBehaviour{

	
	
	private static final long serialVersionUID = 8688081240099240575L;
	
	public static final int SENDERS_EMPTY = 0;
	public static final int SENDERS_NOT_EMPTY = 1;
	
	private static final double PROPORTION = 1.0/6.0;
	private static final int TIME_LIMIT = 2;
	
	private Graph graph;
	private ArrayList<AID> senders;
	private int timer = 0;
	 
	
	public GraphSendersListenerBehaviour(final mas.abstractAgent myagent, Graph graph, ArrayList<AID> senders) {
		super(myagent);
		this.graph=graph;
		this.senders=senders;
	}
	
	@Override
	public void action() {
		System.out.println("************************GraphSenderListenerBehaviour**************************** Proportion=="+PROPORTION);
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
		ACLMessage msg = myAgent.receive(mt);

		if(msg!=null)
		{
			System.out.println(">>Agent : "+myAgent.getLocalName()+"  new msg received "+msg);
			try {
				System.out.println(myAgent.getLocalName()+"****************** GRAPHE DE "+msg.getSender().getLocalName());
				graph.printNodes();
				System.out.println(myAgent.getLocalName()+"******************ECHANTILLON RECU PAR "+msg.getSender().getLocalName());
				ArrayList<Node> sample = (ArrayList<Node>)msg.getContentObject();
				
				for(Node node : sample)
				{
					System.out.println(node.getId()+" reçu par "+myAgent.getLocalName());
				}
				
				if(isAnInterestingProposition(sample))
				{
					System.out.println("The agent "+myAgent.getName()+" is interested in "+msg.getSender().getName()+" 's graph");
					
					//send answer to the agent
					
					ACLMessage reply=new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					reply.setSender(this.myAgent.getAID());
				
					DFAgentDescription dfd = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType(BFSExploAgent.SERVICE_EXP);
					dfd.addServices(sd);
					DFAgentDescription[] result;
					try {
						result = DFService.search(myAgent, dfd);
						reply.addReceiver(msg.getSender());
						((mas.abstractAgent)this.myAgent).sendMessage(reply);
						senders.add(msg.getSender());
						
					} catch (FIPAException e) {
						e.printStackTrace();
					}

				}
								
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			
			timer=0;
		}
		else
		{
			// stop the conversation and do something else
			System.out.println("No msg");
			timer++;
		}
		
		

		//Little pause to allow you to follow what is going on
		try {
			System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param sample
	 * @return whether it is interesting for the agent to ask for the other agent's graph by comparing the ratio 
	 * (number of unknown nodes/size of the sample) to a defined proportion 
	 */
	private boolean isAnInterestingProposition(ArrayList<Node> sample) {
		
		double unknownNodes = 0;
		
		for(Node node : sample){
			
			int myNodeIndex = graph.getNodeIndex(node.getId());
			if(myNodeIndex == -1 || (graph.getNode(myNodeIndex)).getVisited()==false && node.getVisited()==true) //if the node doesn't exist in our graph or if we can get more info on the node we're interested
				unknownNodes++;
		}
		
		
		System.out.println("---------------Agent : "+myAgent.getLocalName()+" ratio is "+unknownNodes+"/"+sample.size()+"="+unknownNodes/sample.size()+"\nProportion is : "+PROPORTION);
		return (unknownNodes/sample.size()>=PROPORTION);
	}

	@Override
	public boolean done() {
		System.out.println("done???? timer = "+timer+" and time_limit=="+TIME_LIMIT);
		return timer==TIME_LIMIT;
	}
	
	@Override
	public int onEnd() {
		timer=0;
		return super.onEnd();
	}
}
