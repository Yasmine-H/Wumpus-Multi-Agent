package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import mas.graph.Graph;

public class GraphReceiversListenerBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8495714732465971286L;
	public static final int WAITING = 0;
	public static final int RECEIVERS = 1;
	public static final int NO_RECEIVERS = 1;
	
	public GraphReceiversListenerBehaviour(final mas.abstractAgent myagent, Graph graph) {
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
