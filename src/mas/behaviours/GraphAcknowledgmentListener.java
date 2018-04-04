package mas.behaviours;

import jade.core.behaviours.Behaviour;
import mas.graph.Graph;

public class GraphAcknowledgmentListener extends Behaviour {

	public static final int WAITING = 0;
	public static final int COMPLETED = 1;
	

	
	public GraphAcknowledgmentListener(final mas.abstractAgent myagent, Graph graph) {
		super(myagent);
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
