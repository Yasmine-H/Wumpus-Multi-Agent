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
	private static final String STATE_SEND = "SendMessage";
	private static final String STATE_RECEIVE = "ReceiveMessage";
	
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
		fsm.registerState(new SendGraphBehaviour(this, graph), STATE_SEND);
		fsm.registerState(new ReceiveGraphBehaviour(this, graph), STATE_RECEIVE);
		
		
		//Transitions
		fsm.registerDefaultTransition(STATE_WALK, STATE_SEND);
		fsm.registerDefaultTransition(STATE_SEND, STATE_RECEIVE);
		fsm.registerDefaultTransition(STATE_RECEIVE, STATE_WALK);
						
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
