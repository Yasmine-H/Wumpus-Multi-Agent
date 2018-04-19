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
import mas.behaviours.GraphRequestBehaviour;
import mas.behaviours.InterblocageListenerBehaviour;
import mas.behaviours.InterblocageResolutionBehaviour;
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
	public static final String STATE_SEND_GRAPH_REQUEST = "Graph Request";
	
	public static final String STATE_START_INTERBLOCAGE = "Interblocage Start Message";
	public static final String STATE_INTERBLOCAGE_LISTENER = "Interblocage Listener";
	public static final String STATE_CHECK_MAILBOX = "Check MailBox";
	public static final String STATE_INTERBLOCAGE_RESOLUTION = "Interblocage Resolution";
	public static final String STATE_GIVES_PRIORITY = "Interblocage Gives Priority";
	
	
	private Graph graph;
	private ArrayList<AID> receivers;
	private ArrayList<AID> senders;
	private ACLMessage interblocageMessage;
	private ArrayList<AID> graph_subscribers;
	private StringBuilder moveTo;
	private StringBuilder previousState;
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
		graph_subscribers = new ArrayList<>();
		interblocageMessage = new ACLMessage(ACLMessage.REQUEST);
		moveTo = new StringBuilder("");
		previousState = new StringBuilder("");
		
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
		
		
		fsm.registerFirstState(new BFSWalkBehaviour(this, graph, moveTo), STATE_WALK);
		fsm.registerState(new SendGraphBehaviour(this, graph, receivers, graph_subscribers), STATE_GRAPH_TRANSMISSION);
		fsm.registerState(new ReceiveGraphBehaviour(this, graph, senders), STATE_GRAPH_RECEPTION);
		fsm.registerState(new GraphRequestBehaviour(this), STATE_SEND_GRAPH_REQUEST);
		
		//TODO 7.4.2018: je viens de fusionner - ajout des états pour interblocage (vérifier si ca marche)
		fsm.registerState(new SendInterblocageStartMessageBehaviour(this,graph, receivers, previousState), STATE_START_INTERBLOCAGE);

		fsm.registerState(new InterblocageListenerBehaviour(this, graph, receivers), STATE_INTERBLOCAGE_LISTENER);
		//TODO 11.4.2018 : LAST ATTENTION
		fsm.registerState(new CheckMailBoxBehaviour(this, graph, new StringBuilder(STATE_WALK), previousState, graph_subscribers, interblocageMessage), STATE_CHECK_MAILBOX);
		fsm.registerState(new InterblocageResolutionBehaviour(this, graph, interblocageMessage), STATE_INTERBLOCAGE_RESOLUTION);
		//fsm.registerTransition(STATE_WALK, STATE_SEND_GRAPH_REQUEST, BFSWalkBehaviour.MOVED);
		
		//fsm.registerTransition(STATE_WALK, STATE_SEND_GRAPH_REQUEST, BFSWalkBehaviour.BLOCKED); // /!\TODO
		fsm.registerDefaultTransition(STATE_SEND_GRAPH_REQUEST, STATE_CHECK_MAILBOX);
		fsm.registerTransition(STATE_CHECK_MAILBOX, STATE_GRAPH_TRANSMISSION, CheckMailBoxBehaviour.GOTO_STATE_GRAPH_TRANSMISSION);
		fsm.registerDefaultTransition(STATE_GRAPH_TRANSMISSION, STATE_CHECK_MAILBOX);
		fsm.registerTransition(STATE_CHECK_MAILBOX, STATE_WALK, CheckMailBoxBehaviour.GOTO_STATE_WALK);
		
		fsm.registerTransition(STATE_WALK, STATE_CHECK_MAILBOX, BFSWalkBehaviour.MOVED);
		fsm.registerTransition(STATE_WALK, STATE_START_INTERBLOCAGE, BFSWalkBehaviour.BLOCKED);
		fsm.registerTransition(STATE_CHECK_MAILBOX, STATE_INTERBLOCAGE_RESOLUTION, CheckMailBoxBehaviour.GOTO_STATE_INTERBLOCAGE_RESOLUTION);
		fsm.registerTransition(STATE_CHECK_MAILBOX, STATE_INTERBLOCAGE_LISTENER, CheckMailBoxBehaviour.GOTO_STATE_INTERBLOCAGE_LISTENER);
		fsm.registerDefaultTransition(STATE_CHECK_MAILBOX, STATE_WALK);
		fsm.registerDefaultTransition(STATE_START_INTERBLOCAGE, STATE_CHECK_MAILBOX);
		fsm.registerDefaultTransition(STATE_INTERBLOCAGE_RESOLUTION, STATE_WALK); //TODO !!! !!!!!!!!!!!!!!!!!!!!
		
		fsm.registerTransition(STATE_INTERBLOCAGE_LISTENER, STATE_WALK, InterblocageListenerBehaviour.HAS_PRIORITY);
		fsm.registerTransition(STATE_INTERBLOCAGE_LISTENER, STATE_WALK, InterblocageListenerBehaviour.GIVES_PRIORITY); //TODO 18.4.: To be changed to STATE_GIVES_PRIORITY once the class works 
		fsm.registerTransition(STATE_INTERBLOCAGE_LISTENER, STATE_WALK, InterblocageListenerBehaviour.NO_RESPONSE); //TODO 18.4: We want to retry to move again, to find another way or to stay (i.e., blocking Golem), or resend the message, or...?
		
		addBehaviour(fsm);

		
		
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
	
	public StringBuilder getMoveTo() {
		return moveTo;
	}
	
}
