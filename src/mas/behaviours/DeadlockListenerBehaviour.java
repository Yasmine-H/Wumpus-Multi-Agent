package mas.behaviours;

import java.util.ArrayList;

import jade.core.behaviours.SimpleBehaviour;

public class DeadlockListenerBehaviour extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8274791699060746178L;
	public static final int WAITING = 0;
	public static final int ANSWER_RECEIVED = 1;
	public static final int NO_ANSWER = 2;
	private final int TIME_UP = 5;
	private int countdown;
	private ArrayList<String> receivers; 
	
	
	public DeadlockListenerBehaviour(final mas.abstractAgent myagent) {
		super(myagent);
		countdown = 0;
		
	}
	
	@Override
	public void action() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean done() {
		return countdown == TIME_UP;
	}

	
	@Override
	public int onEnd() {
		if(countdown == TIME_UP)
		{
			countdown = 0;
			if(receivers.isEmpty())
				return ANSWER_RECEIVED;
			else
				return NO_ANSWER;
		}
		else
			return WAITING; // TODO : is it necessary ??
	}
}
