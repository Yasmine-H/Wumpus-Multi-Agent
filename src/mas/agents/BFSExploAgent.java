package mas.agents;




import env.EntityType;
import env.Environment;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import mas.abstractAgent;
import mas.behaviours.BFSWalkBehaviour;
import mas.behaviours.DeadlockListenerBehaviour;
import mas.behaviours.DeadlockReportBehaviour;
import mas.behaviours.DeadlockSolvingBehaviour;
import mas.behaviours.GraphAcknowledgmentListener;
import mas.behaviours.GraphPropositionBehaviour;
import mas.behaviours.GraphReceiversListenerBehaviour;
import mas.behaviours.GraphReceptionBehaviour;
import mas.behaviours.GraphReceptionListenerBehaviour;
import mas.behaviours.ReceiveGraphBehaviour;
import mas.behaviours.SendGraphBehaviour;
import mas.graph.Graph;


public class BFSExploAgent extends abstractAgent{

	
	public static final String SERVICE_EXP = "explorer";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1784844593772918360L;

	private static final String STATE_WALK = "Walk";
	private static final String STATE_DEADLOCK_REPORT = "Deadlock Report";
	private static final String STATE_DEADLOCK_LISTENER = "Deadlock Listener";
	private static final String STATE_DEADLOCK_SOLVING = "Deadlock Solving";
	private static final String STATE_GRAPH_PROPOSITION = "Graph Proposition";
	private static final String STATE_GRAPH_RECEIVERS_LISTENER = "Graph Receivers Listener"; //waits for agents interested in getting the graph
	private static final String STATE_GRAPH_TRANSMISSION = "Graph Transmission";
	private static final String STATE_GRAPH_AKN_LISTENER = "Graph Reception Acknowledgment Listener";
	private static final String STATE_GRAPH_SENDERS_LISTENER = "Graph Senders Listener"; //waits for agents to propose their graphs
	private static final String STATE_GRAPH_RECEPTION = "Graph Reception";
	
	private Graph graph;

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		
		//registering into the DF 
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(SERVICE_EXP);
		sd.setName(getLocalName());
		dfd.addServices(sd);
		try
		{
			DFService.register(this, dfd);
		}
		catch(FIPAException fe)
		{
			fe.printStackTrace();
		}
		
		
		
		//get the parameters given into the object[]. In the current case, the environment where the agent will evolve
		final Object[] args = getArguments();
		if(args!=null && args[0]!=null && args[1]!=null){

			deployAgent((Environment) args[0], (EntityType) args[1]);

		}else{
			System.err.println("Malfunction during parameter's loading of agent"+ this.getClass().getName());
			System.exit(-1);
		}
		
		graph = new Graph();
		
		
		//Creating a finite-state machine
		
		FSMBehaviour fsm = new FSMBehaviour(this) {
			public int onEnd() {
				System.out.println("FSM behaviour ended");
				myAgent.doDelete(); // TODO 12.03 : Is it supposedto be here or on takeDown function ? 
				return super.onEnd();
			}

		};
		
		//Behaviours/States
		fsm.registerFirstState(new BFSWalkBehaviour(this, graph), STATE_WALK);
		fsm.registerState(new DeadlockReportBehaviour(this), STATE_DEADLOCK_REPORT);
		fsm.registerState(new DeadlockListenerBehaviour(this), STATE_DEADLOCK_LISTENER);
		fsm.registerState(new DeadlockSolvingBehaviour(this, graph), STATE_DEADLOCK_SOLVING);
		fsm.registerState(new GraphPropositionBehaviour(this, graph), STATE_GRAPH_PROPOSITION);
		fsm.registerState(new GraphReceiversListenerBehaviour(this, graph), STATE_GRAPH_RECEIVERS_LISTENER);
		fsm.registerState(new SendGraphBehaviour(this, graph), STATE_GRAPH_TRANSMISSION);
		fsm.registerState(new GraphAcknowledgmentListener(this, graph), STATE_GRAPH_AKN_LISTENER);
		fsm.registerState(new GraphReceptionListenerBehaviour(this, graph), STATE_GRAPH_SENDERS_LISTENER);
		fsm.registerState(new ReceiveGraphBehaviour(this, graph), STATE_GRAPH_RECEPTION);
		
		
		
		//Transitions
		//After moving 
		fsm.registerTransition(STATE_WALK, STATE_DEADLOCK_REPORT, BFSWalkBehaviour.BLOCKED);
		fsm.registerTransition(STATE_WALK, STATE_GRAPH_PROPOSITION, BFSWalkBehaviour.MOVED);		
		
		//Deadlock conflict
		fsm.registerDefaultTransition(STATE_DEADLOCK_REPORT, STATE_DEADLOCK_LISTENER);
		fsm.registerTransition(STATE_DEADLOCK_LISTENER, STATE_DEADLOCK_LISTENER, DeadlockListenerBehaviour.WAITING);
		fsm.registerTransition(STATE_DEADLOCK_LISTENER, STATE_DEADLOCK_SOLVING, DeadlockListenerBehaviour.ANSWER_RECEIVED);
		fsm.registerTransition(STATE_DEADLOCK_LISTENER, STATE_WALK, DeadlockListenerBehaviour.NO_ANSWER);
		fsm.registerTransition(STATE_DEADLOCK_SOLVING, STATE_DEADLOCK_REPORT, BFSWalkBehaviour.BLOCKED);
		fsm.registerTransition(STATE_DEADLOCK_SOLVING, STATE_GRAPH_PROPOSITION, BFSWalkBehaviour.MOVED);
		
		//Graph Transmission TODO 03.04.2018 : be careful of async msgs, are we sure this will work ? Did we forget some kind of msgs ?
		fsm.registerDefaultTransition(STATE_GRAPH_PROPOSITION, STATE_GRAPH_RECEIVERS_LISTENER);
		fsm.registerTransition(STATE_GRAPH_RECEIVERS_LISTENER, STATE_GRAPH_RECEIVERS_LISTENER, GraphReceiversListenerBehaviour.WAITING);
		fsm.registerTransition(STATE_GRAPH_RECEIVERS_LISTENER, STATE_GRAPH_TRANSMISSION, GraphReceiversListenerBehaviour.RECEIVERS);
		fsm.registerTransition(STATE_GRAPH_RECEIVERS_LISTENER, STATE_GRAPH_SENDERS_LISTENER, GraphReceiversListenerBehaviour.NO_RECEIVERS);
		fsm.registerDefaultTransition(STATE_GRAPH_TRANSMISSION, STATE_GRAPH_AKN_LISTENER);
		fsm.registerTransition(STATE_GRAPH_AKN_LISTENER, STATE_GRAPH_AKN_LISTENER, GraphAcknowledgmentListener.WAITING);
		fsm.registerTransition(STATE_GRAPH_AKN_LISTENER, STATE_GRAPH_SENDERS_LISTENER, GraphAcknowledgmentListener.COMPLETED);
		
		//Graph Reception
		fsm.registerTransition(STATE_GRAPH_SENDERS_LISTENER, STATE_GRAPH_SENDERS_LISTENER, GraphReceptionBehaviour.WAITING);
		fsm.registerTransition(STATE_GRAPH_SENDERS_LISTENER, STATE_GRAPH_RECEPTION, GraphReceptionBehaviour.SENDERS);
		fsm.registerTransition(STATE_GRAPH_SENDERS_LISTENER, STATE_WALK, GraphReceptionBehaviour.NO_SENDERS);
		fsm.registerDefaultTransition(STATE_GRAPH_RECEPTION, STATE_WALK);
		
						
		addBehaviour(fsm);

		
		/*
		addBehaviour(new BFSWalkBehaviour(this, graph));
		addBehaviour(new SendGraphBehaviour(this, graph));
		addBehaviour(new ReceiveGraphBehaviour(this, graph));
		*/
		System.out.println("the agent "+this.getLocalName()+ " is started");

	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
		try
		{
			DFService.deregister(this);
		}
		catch(FIPAException fe)
		{
			fe.printStackTrace();
		}
	}
}
