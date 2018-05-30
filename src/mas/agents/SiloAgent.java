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
import mas.behaviours.CheckMailBoxBehaviour;
import mas.behaviours.InterblocageListenerBehaviour;
import mas.behaviours.SendGraphBehaviour;
import mas.behaviours.SendInterblocageStartMessageBehaviour;
import mas.behaviours.SiloInterblocageResolutionBehaviour;
import mas.behaviours.SiloWalkBehaviour;
import mas.graph.Graph;

public class SiloAgent extends abstractAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8889582860728748887L;

//	public static final String SERVICE_TANK = "Silo";
	
	private Graph graph;
	private ACLMessage interblocageMessage;
	private StringBuilder moveTo;
	private StringBuilder previousState;
	
	private ArrayList<AID> receivers;

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
		sd.setType(Constants.SERVICE_TANK);
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
		if(args[0]!=null){

			deployAgent((Environment) args[0], (EntityType) args[1]);

		}else{
			System.err.println("Malfunction during parameter's loading of agent"+ this.getClass().getName());
			System.exit(-1);
		}

		FSMBehaviour fsm = new FSMBehaviour(this) {
			public int onEnd() {
				System.out.println("FSM behaviour ended");
				myAgent.doDelete(); // TODO 12.03 : Is it supposedto be here or on takeDown function ? 
				return super.onEnd();
			}

		};

		graph = new Graph();
		interblocageMessage = new ACLMessage(ACLMessage.REQUEST);

		moveTo = new StringBuilder("");
		previousState = new StringBuilder("");
		receivers = new ArrayList<>();


		fsm.registerFirstState(new SiloWalkBehaviour(this, graph, moveTo), Constants.STATE_WALK);
		fsm.registerState(new SendGraphBehaviour(this, graph), Constants.STATE_GRAPH_TRANSMISSION);
		
		fsm.registerState(new SendInterblocageStartMessageBehaviour(this,graph, receivers, previousState), Constants.STATE_START_INTERBLOCAGE);
		fsm.registerState(new InterblocageListenerBehaviour(this, graph, receivers, moveTo), Constants.STATE_INTERBLOCAGE_LISTENER);
		fsm.registerState(new SiloInterblocageResolutionBehaviour(this, graph, interblocageMessage, moveTo), Constants.STATE_INTERBLOCAGE_RESOLUTION);
		fsm.registerState(new CheckMailBoxBehaviour(this, graph, new StringBuilder(Constants.STATE_WALK), previousState,/* graph_subscribers,*/ interblocageMessage, moveTo), Constants.STATE_CHECK_MAILBOX);

		fsm.registerTransition(Constants.STATE_WALK, Constants.STATE_GRAPH_TRANSMISSION, Constants.MOVED);
		fsm.registerTransition(Constants.STATE_WALK, Constants.STATE_START_INTERBLOCAGE, Constants.BLOCKED); // /!\TODO
		fsm.registerDefaultTransition(Constants.STATE_GRAPH_TRANSMISSION, Constants.STATE_CHECK_MAILBOX);
		fsm.registerTransition(Constants.STATE_CHECK_MAILBOX, Constants.STATE_WALK, Constants.GOTO_STATE_WALK);

		//Gérer les interblocages :
		fsm.registerTransition(Constants.STATE_CHECK_MAILBOX, Constants.STATE_INTERBLOCAGE_RESOLUTION, Constants.GOTO_STATE_INTERBLOCAGE_RESOLUTION);
		fsm.registerTransition(Constants.STATE_CHECK_MAILBOX, Constants.STATE_INTERBLOCAGE_LISTENER, Constants.GOTO_STATE_INTERBLOCAGE_LISTENER);
		fsm.registerDefaultTransition(Constants.STATE_START_INTERBLOCAGE, Constants.STATE_CHECK_MAILBOX);
		fsm.registerDefaultTransition(Constants.STATE_INTERBLOCAGE_RESOLUTION, Constants.STATE_WALK); //TODO !!! !!!!!!!!!!!!!!!!!!!!
		
		fsm.registerTransition(Constants.STATE_INTERBLOCAGE_LISTENER, Constants.STATE_WALK, Constants.HAS_PRIORITY);
		fsm.registerTransition(Constants.STATE_INTERBLOCAGE_LISTENER, Constants.STATE_WALK, Constants.GIVES_PRIORITY); //TODO 18.4.: To be changed to STATE_GIVES_PRIORITY once the class works 
		fsm.registerTransition(Constants.STATE_INTERBLOCAGE_LISTENER, Constants.STATE_WALK, Constants.NO_RESPONSE); //TODO 18.4: We want to retry to move again, to find another way or to stay (i.e., blocking Golem), or resend the message, or...?
		
		
		addBehaviour(fsm);



		System.out.println("the agent "+this.getLocalName()+ " is started");

	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){

	}
	
	public StringBuilder getMoveTo() {
		return moveTo;
	}
	
	public ACLMessage getInterblocageMessage() {
		return interblocageMessage;
	}
	
	public void setInterblocageMessage(ACLMessage msg) {
		interblocageMessage = msg;
	}

}
