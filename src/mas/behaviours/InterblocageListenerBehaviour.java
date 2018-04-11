package mas.behaviours;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.graph.Graph;
import mas.others.InterblocageAcceptMT;

public class InterblocageListenerBehaviour extends Behaviour{
	private static final long serialVersionUID = 8688081240099240575L;
	
	public static final int HAS_PRIORITY = 1;
	public static final int GIVES_PRIORITY = 0;
	public static final int NO_RESPONSE = -1;
	
	private Graph graph;
	private ArrayList<AID> senders;
	private ACLMessage originalMsg;
	private int result;
	
	public InterblocageListenerBehaviour(final mas.abstractAgent myagent, Graph graph, ArrayList<AID> senders, ACLMessage originalMsg) {
		super(myagent);
		this.graph=graph;
		this.senders=senders;
		this.originalMsg = originalMsg;
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		//InterblocageAcceptMT iamt = new InterblocageAcceptMT();
		
		System.out.println("Interblocage listener Bevahivour*************************");
		//TODO 9.4.: Define its own template to distinguish interblocage messages and graph proposal messages
		//MessageTemplate mt = new MessageTemplate.MatchExpression(iamt); 
		//ACLMessage msg = myAgent.blockingReceive(mt, originalMsg.getReplyByDate().getTime()-System.currentTimeMillis());
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		//ACLMessage msg = myAgent.blockingReceive(mt, originalMsg.getReplyByDate().getTime()-System.currentTimeMillis());
		ACLMessage msg = myAgent.blockingReceive(mt, 60000);
		result = NO_RESPONSE;
		
		if(msg != null) {
			System.out.println("Agent : "+myAgent.getLocalName()+" new msg received : "+msg.getContent());
			if (msg.getPerformative() == ACLMessage.AGREE) {
				result = HAS_PRIORITY;
			}
			else if(msg.getPerformative() == ACLMessage.REFUSE) {
				result = GIVES_PRIORITY;
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

