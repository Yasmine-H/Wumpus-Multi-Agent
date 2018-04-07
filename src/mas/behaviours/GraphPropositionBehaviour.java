package mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import mas.agents.BFSExploAgent;
import mas.graph.Graph;
import mas.graph.Node;

/**
 * 
 * Sends a sample of the agent's graph to the other agents. Each receiver will then compare the sample to his own graph and send a reply 
 * if he's interested in this graph. 
 * TODO 05/04/2018 : Is it really necessary ? Two different agents may propose the same graph and the receiver will say yes to both. 
 * Should we control that too ? Is it worth it ?
 * 
 */
public class GraphPropositionBehaviour extends SimpleBehaviour {

	
	private static final long serialVersionUID = 2816646757983256333L;
	private Graph graph;
	
	public GraphPropositionBehaviour(final mas.abstractAgent myagent, Graph graph) {
		super(myagent);
		this.graph=graph;
	}
	
	@Override
	public void action() {
		System.out.println("************************GraphPropositionBehaviour****************************");
		//send graph for the 2-neighbours
		ACLMessage msg=new ACLMessage(ACLMessage.PROPOSE);
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
			
			ArrayList<Node> sample = getSample(graph.size()/2);
			msg.setContentObject(sample);
			((mas.abstractAgent)this.myAgent).sendMessage(msg);
			System.out.println(">>Agent : "+myAgent.getLocalName()+"  msg "+msg+" sent");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Little pause to allow you to follow what is going on
		try {
			System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private ArrayList<Node> getSample(int sampleSize) {
		
		ArrayList<Node> sample = new ArrayList<Node>();
		
		//Random indexes generation
		ArrayList<Integer> list = new ArrayList<Integer>(graph.size());
        for(int i = 0; i < graph.size(); i++) {
            list.add(i);
        }

        Random rand = new Random();
        while(list.size() > graph.size() - sampleSize) {
            int index = rand.nextInt(list.size());
            list.remove(index);
            //System.out.println("Selected: "+list.remove(index));
        }
		
        //Creating a sample from the generated indexes
		for(int i : list)
		{
			sample.add(graph.getNode(i));
		}
		
		
		return sample;
	}

	@Override
	public boolean done() {
		
		return true;
	}

}
