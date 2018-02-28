package mas.agents;




import env.Environment;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import mas.abstractAgent;
import mas.behaviours.BFSWalkBehaviour;
import mas.behaviours.ReceiveGraph;
import mas.behaviours.SendGraph;
import mas.graph.Graph;


public class BFSExploAgent extends abstractAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1784844593772918360L;

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
		sd.setType("explorer");
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

			deployAgent((Environment) args[0]);

		}else{
			System.err.println("Malfunction during parameter's loading of agent"+ this.getClass().getName());
			System.exit(-1);
		}
		
		graph = new Graph();
		
		//Add the behaviours
		addBehaviour(new BFSWalkBehaviour(this, graph));
		addBehaviour(new SendGraph(this, graph));
		addBehaviour(new ReceiveGraph(this, graph));
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
