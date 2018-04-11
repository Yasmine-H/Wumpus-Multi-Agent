<<<<<<< HEAD
package mas.behaviours;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import mas.agents.BFSExploAgent;

public class CheckMailBoxBehaviour extends Behaviour{
	
	private String nextState;
	public static final int GOTO_STATE_WALK = 0;
	public static final int GOTO_STATE_GRAPH_PROPOSITION = 1;
	public static final int GOTO_STATE_INTERBLOCAGE_RESOLUTION = 2;
	private int result;
	
	public CheckMailBoxBehaviour(mas.abstractAgent myAgent, String nextState) {
		super(myAgent);
		this.nextState = nextState;
		result = -1;
	}
	
	@Override
	public void action() {
		// TODO Auto-generated method stub
		ACLMessage msg = myAgent.receive();
		
		if(msg != null) {
			if(msg.getContent().contains("INTERBLOCAGE")) {
				result = GOTO_STATE_INTERBLOCAGE_RESOLUTION;
			}
		}
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public int onEnd() {
		if(result != -1)
			return result;
		
		//else - there was no message, we continue to the nextState 
		if(nextState.equalsIgnoreCase(BFSExploAgent.STATE_GRAPH_PROPOSITION))
			return GOTO_STATE_GRAPH_PROPOSITION;
		else if(nextState.equalsIgnoreCase(BFSExploAgent.STATE_WALK))
			return GOTO_STATE_WALK;
		else if(nextState.equalsIgnoreCase(BFSExploAgent.STATE_INTERBLOCAGE_RESOLUTION))
			return GOTO_STATE_INTERBLOCAGE_RESOLUTION;
		//TODO 10.4.: Try to find nicer solution/default state
		else return -1;
		
	}

}
=======
package mas.behaviours;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import mas.agents.BFSExploAgent;

public class CheckMailBoxBehaviour extends Behaviour{
	
	private String nextState;
	public static final int GOTO_STATE_WALK = 0;
	public static final int GOTO_STATE_GRAPH_PROPOSITION = 1;
	public static final int GOTO_STATE_INTERBLOCAGE_RESOLUTION = 2;
	
	
	public CheckMailBoxBehaviour(mas.abstractAgent myAgent, String nextState) {
		super(myAgent);
		this.nextState = nextState;
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
	
	public int onEnd() {
		if(nextState.equalsIgnoreCase(BFSExploAgent.STATE_GRAPH_PROPOSITION))
			return GOTO_STATE_GRAPH_PROPOSITION;
		else if(nextState.equalsIgnoreCase(BFSExploAgent.STATE_WALK))
			return GOTO_STATE_WALK;
		else if(nextState.equalsIgnoreCase(BFSExploAgent.STATE_INTERBLOCAGE_RESOLUTION))
			return GOTO_STATE_INTERBLOCAGE_RESOLUTION;
		//TODO 10.4.: Try to find nicer solution/default state
		else return -1;
		
	}

}
>>>>>>> 857699c9e56834f56fbf11eeffefd8499fe512d4
