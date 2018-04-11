package mas.agents;




import java.util.ArrayList;

import env.EntityType;
import env.Environment;
import jade.core.AID;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import mas.abstractAgent;
import mas.behaviours.BFSWalkBehaviour;
import mas.behaviours.CheckMailBoxBehaviour;
import mas.behaviours.GraphAcknowledgmentListener;
import mas.behaviours.GraphPropositionBehaviour;
import mas.behaviours.GraphReceiversListenerBehaviour;
import mas.behaviours.GraphSendersListenerBehaviour;
import mas.behaviours.InterblocageListenerBehaviour;
import mas.behaviours.ReceiveGraphBehaviour;
import mas.behaviours.SendGraphBehaviour;
import mas.behaviours.SendInterblocageStartMessageBehaviour;
import mas.graph.Graph;


public class BFSExploAgent extends abstractAgent{

	
	public static final String SERVICE_EXP = "explorer";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1784844593772918360L;

	public static final String STATE_WALK = "Walk";
	public static final String STATE_DEADLOCK_REPORT = "Deadlock Report";
	public static final String STATE_DEADLOCK_LISTENER = "Deadlock Listener";
	public static final String STATE_DEADLOCK_SOLVING = "Deadlock Solving";
	public static final String STATE_GRAPH_PROPOSITION = "Graph Proposition";
	public static final String STATE_GRAPH_RECEIVERS_LISTENER = "Graph Receivers Listener"; //waits for agents interested in getting the graph
	public static final String STATE_GRAPH_TRANSMISSION = "Graph Transmission";
	public static final String STATE_GRAPH_AKN_LISTENER = "Graph Reception Acknowledgment Listener";
	public static final String STATE_GRAPH_SENDERS_LISTENER = "Graph Senders Listener"; //waits for agents to propose their graphs
	public static final String STATE_GRAPH_RECEPTION = "Graph Reception";
	
	public static final String STATE_START_INTERBLOCAGE = "Interblocage Start Message";
	public static final String STATE_INTERBLOCAGE_LISTENER = "Interblocage Listener";
	public static final String STATE_CHECK_MAILBOX = "Check MailBox";
	public static final String STATE_INTERBLOCAGE_RESOLUTION = "Interblocage Resolution";
	public static final String STATE_GIVES_PRIORITY = "Interblocage Gives Priority";
	
	
	private Graph graph;
	private ArrayList<AID> receivers;
	private ArrayList<AID> senders;
	private ACLMessage interblocageMessage;
	//private String moveTo;
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
		receivers = new ArrayList<>();
		senders = new ArrayList<>();
		interblocageMessage = new ACLMessage(ACLMessage.REQUEST);
		
		DFAgentDescription[] result;
		try {
			result = DFService.search(this, dfd);
			for(int i=0; i<result.length; i++)
			{
				System.out.println("My AID is "+this.getAID() +" and I want to send to "+result[i].getName());
				if(!result[i].getName().equals(this.getAID()))
				{
					receivers.add(result[i].getName());
				}
			}

		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
		//Creating a finite-state machine
		
		FSMBehaviour fsm = new FSMBehaviour(this) {
			public int onEnd() {
				System.out.println("FSM behaviour ended");
				myAgent.doDelete(); // TODO 12.03 : Is it supposedto be here or on takeDown function ? 
				return super.onEnd();
			}

		};
		
		/*
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
		
			
		*/
		//test
		
		fsm.registerFirstState(new BFSWalkBehaviour(this, graph, interblocageMessage), STATE_WALK);
		fsm.registerState(new GraphPropositionBehaviour(this, graph), STATE_GRAPH_PROPOSITION);
		fsm.registerState(new GraphReceiversListenerBehaviour(this, graph, receivers), STATE_GRAPH_RECEIVERS_LISTENER);
		fsm.registerState(new SendGraphBehaviour(this, graph, receivers), STATE_GRAPH_TRANSMISSION);
		fsm.registerState(new GraphAcknowledgmentListener(this, graph), STATE_GRAPH_AKN_LISTENER);
		fsm.registerState(new GraphSendersListenerBehaviour(this, graph, senders), STATE_GRAPH_SENDERS_LISTENER);
		fsm.registerState(new ReceiveGraphBehaviour(this, graph, senders), STATE_GRAPH_RECEPTION);
		
		//TODO 7.4.2018: je viens de fusionner - ajout des états pour interblocage (vérifier si ca marche)
		fsm.registerState(new SendInterblocageStartMessageBehaviour(this,graph, receivers, interblocageMessage), STATE_START_INTERBLOCAGE);

		fsm.registerLastState(new InterblocageListenerBehaviour(this, graph, receivers, interblocageMessage), STATE_INTERBLOCAGE_LISTENER);
		//TODO 11.4.2018 : LAST ATTENTION
		fsm.registerState(new CheckMailBoxBehaviour(this, STATE_WALK), STATE_CHECK_MAILBOX);
		
		/*
		
		//Transitions
		//After moving 
		fsm.registerDefaultTransition(STATE_WALK, STATE_GRAPH_PROPOSITION);		
		
		//Graph Transmission TODO 03.04.2018 : be careful of async msgs, are we sure this will work ? Did we forget some kind of msgs ?

		fsm.registerDefaultTransition(STATE_GRAPH_PROPOSITION, STATE_GRAPH_RECEIVERS_LISTENER);
		fsm.registerTransition(STATE_GRAPH_RECEIVERS_LISTENER, STATE_GRAPH_SENDERS_LISTENER, GraphSendersListenerBehaviour.SENDERS_EMPTY);
		fsm.registerTransition(STATE_GRAPH_RECEIVERS_LISTENER, STATE_GRAPH_TRANSMISSION, GraphSendersListenerBehaviour.SENDERS_NOT_EMPTY);
		fsm.registerDefaultTransition(STATE_GRAPH_TRANSMISSION, STATE_GRAPH_AKN_LISTENER);
		fsm.registerDefaultTransition(STATE_GRAPH_AKN_LISTENER, STATE_GRAPH_SENDERS_LISTENER);
		
		
		fsm.registerDefaultTransition(STATE_GRAPH_PROPOSITION, STATE_GRAPH_SENDERS_LISTENER);
		fsm.registerTransition(STATE_GRAPH_SENDERS_LISTENER, STATE_WALK, GraphSendersListenerBehaviour.SENDERS_EMPTY);
		fsm.registerTransition(STATE_GRAPH_SENDERS_LISTENER, STATE_GRAPH_RECEPTION, GraphSendersListenerBehaviour.SENDERS_NOT_EMPTY);
		fsm.registerDefaultTransition(STATE_GRAPH_RECEPTION, STATE_WALK); 
		
		
		//fsm.registerTransition(STATE_WALK, STATE_START_INTERBLOCAGE, BFSWalkBehaviour.BLOCKED);
		/*fsm.registerTransition(STATE_GRAPH_RECEIVERS_LISTENER, STATE_GRAPH_RECEIVERS_LISTENER, GraphReceiversListenerBehaviour.WAITING);
		fsm.registerTransition(STATE_GRAPH_RECEIVERS_LISTENER, STATE_GRAPH_TRANSMISSION, GraphReceiversListenerBehaviour.RECEIVERS);
		fsm.registerTransition(STATE_GRAPH_RECEIVERS_LISTENER, STATE_GRAPH_SENDERS_LISTENER, GraphReceiversListenerBehaviour.NO_RECEIVERS);
		/*fsm.registerDefaultTransition(STATE_GRAPH_TRANSMISSION, STATE_GRAPH_AKN_LISTENER);
		fsm.registerTransition(STATE_GRAPH_AKN_LISTENER, STATE_GRAPH_AKN_LISTENER, GraphAcknowledgmentListener.WAITING);
		fsm.registerTransition(STATE_GRAPH_AKN_LISTENER, STATE_GRAPH_SENDERS_LISTENER, GraphAcknowledgmentListener.COMPLETED);
		
		
		//Graph Reception
		fsm.registerTransition(STATE_GRAPH_SENDERS_LISTENER, STATE_GRAPH_SENDERS_LISTENER, GraphReceptionBehaviour.WAITING);
		fsm.registerTransition(STATE_GRAPH_SENDERS_LISTENER, STATE_GRAPH_RECEPTION, GraphReceptionBehaviour.SENDERS);
		fsm.registerTransition(STATE_GRAPH_SENDERS_LISTENER, STATE_WALK, GraphReceptionBehaviour.NO_SENDERS);
		fsm.registerDefaultTransition(STATE_GRAPH_RECEPTION, STATE_WALK);
		*/
		
		fsm.registerDefaultTransition(STATE_WALK, STATE_START_INTERBLOCAGE);
		//fsm.registerDefaultTransition(STATE_START_INTERBLOCAGE, STATE_WALK);
		fsm.registerDefaultTransition(STATE_START_INTERBLOCAGE, STATE_INTERBLOCAGE_LISTENER);
		/*fsm.registerTransition(STATE_INTERBLOCAGE_LISTENER, STATE_WALK, InterblocageListenerBehaviour.HAS_PRIORITY);
		fsm.registerTransition(STATE_INTERBLOCAGE_LISTENER, STATE_CHECK_MAILBOX, InterblocageListenerBehaviour.NO_RESPONSE);
		fsm.registerTransition(STATE_INTERBLOCAGE_LISTENER, STATE_GIVES_PRIORITY, InterblocageListenerBehaviour.GIVES_PRIORITY);
		fsm.registerTransition(STATE_CHECK_MAILBOX, STATE_WALK, CheckMailBoxBehaviour.GOTO_STATE_WALK);
		//fsm.registerTransition(STATE_CHECK_MAILBOX, STATE_GRAPH_PROPOSITION, CheckMailBoxBehaviour.GOTO_STATE_GRAPH_PROPOSITION);
		fsm.registerTransition(STATE_CHECK_MAILBOX, STATE_INTERBLOCAGE_RESOLUTION, CheckMailBoxBehaviour.GOTO_STATE_INTERBLOCAGE_RESOLUTION);
		fsm.registerDefaultTransition(STATE_INTERBLOCAGE_RESOLUTION, STATE_CHECK_MAILBOX); //, 1); //has priority
//		fsm.registerTransition(STATE_INTERBLOCAGE_RESOLUTION,  , event);
		*/
		addBehaviour(fsm);

		
		
		//addBehaviour(new SendInterblocageStartMessageBehaviour(this, graph, null, null));
		//addBehaviour(new BFSWalkBehaviour(this, graph));
		/*addBehaviour(new SendGraphBehaviour(this, graph));
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
	/*
	public void setMoveTo(String moveTo) {
		this.moveTo = moveTo;
	}*/
}
