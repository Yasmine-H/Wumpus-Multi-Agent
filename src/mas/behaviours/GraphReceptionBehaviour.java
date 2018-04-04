package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import mas.graph.Graph;

public class GraphReceptionBehaviour extends SimpleBehaviour {
	
	public static final int WAITING = 0;
	public static final int SENDERS = 1;
	public static final int NO_SENDERS = 2;
	
	private Graph graph;
	
	public GraphReceptionBehaviour(final mas.abstractAgent myagent, Graph graph) {
		super(myagent);
		this.graph=graph;
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
