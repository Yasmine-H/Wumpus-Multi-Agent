package mas.behaviours;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.abstractAgent;
import mas.graph.Graph;
import mas.graph.Node;
import mas.others.InterblocageAcceptMT;

public class InterblocageListenerBehaviour extends Behaviour{
	private static final long serialVersionUID = 8688081240099240575L;
	
	public static final int HAS_PRIORITY = 1;
	public static final int GIVES_PRIORITY = 0;
	public static final int NO_RESPONSE = -1;
	
	private Graph graph;
	private ArrayList<AID> senders;
	private int result;
	private int timer = 0 ;
	private int time_limit = 3; //TODO 5/04/2018: define the time limit properly
	private StringBuilder moveTo;
	
	public InterblocageListenerBehaviour(final mas.abstractAgent myagent, Graph graph, ArrayList<AID> senders, StringBuilder moveTo) {
		super(myagent);
		this.graph=graph;
		this.senders=senders;
		this.moveTo = moveTo;
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		//InterblocageAcceptMT iamt = new InterblocageAcceptMT();
		
		System.out.println(myAgent.getLocalName()+": Interblocage listener Bevahivour*************************");
		//TODO 9.4.: Define its own template to distinguish interblocage messages and graph proposal messages !!! - if we have another message with this performative that does not concern interblocage? 
		MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
		//ACLMessage msg = myAgent.blockingReceive(mt, originalMsg.getReplyByDate().getTime()-System.currentTimeMillis());
		//TODO 18.4: Create a constant for 3000, and check out if it is not too much 
		ACLMessage msg = myAgent.receive(mt);
		result = NO_RESPONSE;
		
		if(msg != null) {
			System.out.println("Agent : "+myAgent.getLocalName()+" new msg received : "+msg.getContent());
			if (msg.getPerformative() == ACLMessage.AGREE) {
				System.out.println(myAgent.getLocalName()+" has priority! It will move.");
				result = HAS_PRIORITY;
			}
			else if(msg.getPerformative() == ACLMessage.REFUSE) {
				System.out.println(myAgent.getLocalName()+" has to give priority! It will go to the state GivePriorityBehaviour.");
				if(msg.getContent().contains("move to :")) {
					String[] lineParts = msg.getContent().split(":");
					moveTo.replace(0, moveTo.length(), lineParts[lineParts.length - 1].trim());	
				}
				else {
					ArrayList<Node> neighbours = graph.getAllNodes();
					for(Node node :neighbours) {
						
						boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(node.getId()); //moveTo.replace(0, moveTo.length(), node.getId());
						if(moved) {
							break;
						}
					}
				}
				result = GIVES_PRIORITY;
			}
		}
		else {
			ArrayList<Node> neighbours = graph.getNode(((abstractAgent) myAgent).getCurrentPosition()).getNeighbours();
			for(Node node :neighbours) {
					boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(node.getId()); //moveTo.replace(0, moveTo.length(), node.getId());
					if(moved) {
						break;
					}
			}
		}
		
	}	
	@Override
	public boolean done() {
		return true;
	}
	
	public int onEnd() {
		return result;
	}

}

