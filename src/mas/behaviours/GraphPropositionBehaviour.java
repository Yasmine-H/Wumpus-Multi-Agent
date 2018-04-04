package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import mas.graph.Graph;

public class GraphPropositionBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2816646757983256333L;
	private Graph graph;
	
	public GraphPropositionBehaviour(final mas.abstractAgent myagent, Graph graph) {
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
