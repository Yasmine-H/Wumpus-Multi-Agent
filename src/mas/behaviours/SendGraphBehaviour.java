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
import mas.agents.Constants;
import mas.graph.Graph;

/**
 * Sends the agent's graph to the agents that replied to his graph proposal
 */
public class SendGraphBehaviour extends SimpleBehaviour{

	
	private static final long serialVersionUID = -2058134622078521998L;

	/**
	 * 
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	
	private Graph graph;
//	private ArrayList<AID> graph_subscribers;
	private int count;
	
	
	public SendGraphBehaviour (final Agent myagent, Graph graph){ //, ArrayList<AID> graph_subscribers) {
		super(myagent);
		this.graph=graph;
//		this.graph_subscribers=graph_subscribers;
		this.count = 0;
	}

	@Override
	public void action() {
		//String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();
		System.out.println(myAgent.getLocalName()+"************************SendGraphBehaviour**************************** count : "+count);
		
		if(count == Constants.SEND_GRAPH) // send graph
		{
			count = 0;
			if(graph.size()>0)
			{
				//send graph for the 2-neighbours
	
				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
	
	
	
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				//sd.setType(BFSExploAgent.SERVICE_EXP);
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
					/*
				for(AID subscriber : graph_subscribers)
				{
					msg.addReceiver(subscriber);
				}
					 */
	
	
					//msg.addReceiver(graph_subscriber);
					msg.setContentObject(graph);
					((mas.abstractAgent)this.myAgent).sendMessage(msg);
					System.out.println(">>Agent : "+myAgent.getLocalName()+"  graph sent");//+"\nTHE NUMBER OF RECEIVERS SHOULD BE ::::: "+graph_subscribers.size());
//					System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");//+"\nTHE NUMBER OF RECEIVERS SHOULD BE ::::: "+graph_subscribers.size());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (FIPAException e) {
					e.printStackTrace();
				}
			}
		}
		
		else{
			count ++;
		}
	}

	@Override
	public boolean done() {
		//graph_subscribers.clear();
		return true;
	}

}