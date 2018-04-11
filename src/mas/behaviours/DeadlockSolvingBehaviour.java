package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import mas.graph.Graph;

public class DeadlockSolvingBehaviour extends SimpleBehaviour{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7576738121358742486L;
	private Graph graph;
	
	public DeadlockSolvingBehaviour(final mas.abstractAgent myagent, Graph graph) {
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
