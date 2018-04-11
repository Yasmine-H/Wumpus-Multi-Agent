package mas.behaviours;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import mas.graph.Graph;

public class GivesPriorityBehaviour extends Behaviour{

	private Graph graph;
	private ACLMessage interblocageMsg;
	
	public GivesPriorityBehaviour(mas.abstractAgent myAgent, Graph graph, ACLMessage message) {
		super(myAgent);
		this.interblocageMsg = message;
	}
	
	@Override
	public void action() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
