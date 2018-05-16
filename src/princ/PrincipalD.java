package princ;



import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;


import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import mas.agents.DummyExploAgent;
import mas.agents.DummyMigrationAgent;
import mas.agents.DummyWumpusAgent;
import mas.agents.GateKeeperAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;

import env.Environment;
import env.Environment.ENVtype;




public class PrincipalD {

	private static String PLATFORM_HOSTNAME ="132.227.205.25";// "127.0.0.1"; 
	private static String PLATFORM_ID="Ithaq";
	private static Integer PLATFORM_PORT=8888;
	
	private static boolean PLATFORM_isDISTRIBUTED=true;
	private static boolean PLATFORM_isMAIN=false;
	
	private static HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();// container's name - container's ref
	private static List<AgentController> agentList;// agents's ref
	private static Runtime rt;	

	private static Environment env;// static ref of the real environment

	public static void main(String[] args){
		
		if(!PLATFORM_isDISTRIBUTED){
		//No gateKeeper, the environment is created and a reference is given to the agents at creation
			
		//0) Create the real environment and the observed one
		//env= new Environment(ENVtype.GRID_T,4,null);
		//env= new Environment(ENVtype.DOROGOVTSEV,15,null);
		env=new Environment("src/main/resources/map2015","src/main/resources/map2015-config");
		
		//1), create the platform (Main container (DF+AMS) + containers + monitoring agents : RMA and SNIFFER)
		rt=emptyPlatform(containerList);
		
		//2) create agents and add them to the platform.
		agentList=createAgents(containerList);

		//3) launch agents
		startAgents(agentList);
		
		}else{
			//DistributedVersion of the project
			if(PLATFORM_isMAIN){
				//Whe should create the Platform and the GateKeeper
				
				//1), create the platform (Main container (DF+AMS) + containers + monitoring agents : RMA and SNIFFER)
				rt=emptyPlatform(containerList);
				
				//2) create the gatekeeper (in charge of the environment) and add it (them) to the platform.
				agentList=createAgents(containerList);

				//3) launch agents
				startAgents(agentList);
			}else{
				//We only have to create the local container and our agents

				//1') If a distant platform already exist and you want to create and connect your container to it
				containerList.putAll(createAndConnectContainer("MyLocalContainer", PLATFORM_HOSTNAME, PLATFORM_ID, PLATFORM_PORT));
				
				//2) create agents and add them to the platform.
				//agentList=createAgents(containerList);

				//3) launch agents
				//startAgents(agentList);
			}
		}
	}



	/**********************************************
	 * 
	 * Methods used to create an empty platform
	 * 
	 **********************************************/

	/**
	 * Create an empty platform composed of 1 main container and 3 containers.
	 * @param containerList 
	 * @return a ref to the platform and update the containerList
	 */
	private static Runtime emptyPlatform(HashMap<String, ContainerController> containerList){

		Runtime rt = Runtime.instance();

		// 1) create a platform (main container+DF+AMS)
		Profile pMain = new ProfileImpl(PLATFORM_HOSTNAME, PLATFORM_PORT, PLATFORM_ID);
		System.out.println("Launching a main-container..."+pMain);
		AgentContainer mainContainerRef = rt.createMainContainer(pMain); //DF and AMS are include

		// 2) create the containers
		containerList.putAll(createContainers(rt));

		// 3) create monitoring agents : rma agent, used to debug and monitor the platform; sniffer agent, to monitor communications; 
		createMonitoringAgents(mainContainerRef);

		System.out.println("Plaform ok");
		return rt;

	}

	/**
	 * Create the containers used to hold the agents 
	 * @param rt The reference to the main container
	 * @return an Hmap associating the name of a container and its object reference.
	 * 
	 * note: there is a smarter way to find a container with its name, but we go fast to the goal here. Cf jade's doc.
	 */
	private static HashMap<String,ContainerController> createContainers(Runtime rt) {
		String containerName;
		ProfileImpl pContainer;
		ContainerController containerRef;
		HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();//bad to do it here.

		System.out.println("Launching containers ...");

		//create the container0	
		containerName="MyDistantContainer0";
		pContainer = new ProfileImpl(PLATFORM_HOSTNAME, PLATFORM_PORT, PLATFORM_ID);
		pContainer.setParameter(Profile.CONTAINER_NAME,containerName);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		
		containerList.put(containerName, containerRef);

		//create the container1	
		containerName="MyDistantContainer1";
		pContainer = new ProfileImpl(PLATFORM_HOSTNAME, PLATFORM_PORT, PLATFORM_ID);
		//pContainer = new ProfileImpl(null, 8888, null);
		pContainer.setParameter(Profile.CONTAINER_NAME,containerName);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		containerList.put(containerName, containerRef);

		//create the container2	
		containerName="MyDistantContainer2";
		pContainer = new ProfileImpl(PLATFORM_HOSTNAME, PLATFORM_PORT, PLATFORM_ID);
		//pContainer = new ProfileImpl(null, 8888, null);
		pContainer.setParameter(Profile.CONTAINER_NAME,containerName);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		containerList.put(containerName, containerRef);

		System.out.println("Launching containers done");
		return containerList;
	
	}

	/**
	 * 
	 * @param containerName
	 * @param host  is the IP of the host where the main-container should be listen to. A null value means use the default (i.e. localhost)
	 * @param platformID is the symbolic name of the platform, if different from default. A null value means use the default (i.e. localhost)
	 * @param port (if null, 8888 by default)
	 * @return
	 */
	private static HashMap<String,ContainerController> createAndConnectContainer(String containerName,String host, String platformID, Integer port){
		
		ProfileImpl pContainer;
		ContainerController containerRef;
		HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();//bad to do it here.
		Runtime rti=Runtime.instance();
		
		if (port==null){
			port=8888;
		}
		
		System.out.println("Create and Connect container "+containerName+ " to the host : "+host+", platformID: "+platformID+" on port "+port);
		
		pContainer = new ProfileImpl(host,port, platformID);
		pContainer.setParameter(Profile.CONTAINER_NAME,containerName);
		containerRef = rti.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		
		//ContainerID cID= new ContainerID();
		//cID.setName(containerName);
		//cID.setPort(port);
		//cID.setAddress(host);
			
		containerList.put(containerName, containerRef);
		return containerList;
	}

	/**
	 * create the monitoring agents (rma+sniffer) on the main-container given in parameter and launch them.
	 *  - RMA agent's is used to debug and monitor the platform;
	 *  - Sniffer agent is used to monitor communications
	 * @param mc the main-container's reference
	 */
	private static void createMonitoringAgents(ContainerController mc) {

		System.out.println("Launching the rma agent on the main container ...");
		AgentController rma;

		try {
			rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
			rma.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("Launching of rma agent failed");
		}

		System.out.println("Launching  Sniffer agent on the main container...");
		AgentController snif=null;

		try {
			snif= mc.createNewAgent("sniffeur", "jade.tools.sniffer.Sniffer",new Object[0]);
			snif.start();

		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("launching of sniffer agent failed");

		}		


	}



	/**********************************************
	 * 
	 * Methods used to create the agents and to start them
	 * 
	 **********************************************/


	/**
	 *  Creates the agents and add them to the agentList.  agents are NOT started.
	 *@param containerList :Name and container's ref
	 *@return the agentList
	 */
	private static List<AgentController> createAgents(HashMap<String, ContainerController> containerList) {
		System.out.println("Launching agents...");
		ContainerController c;
		String agentName;
		List<AgentController> agentList=new ArrayList<AgentController>();
		
		/*
		 *Distributed, the main already exist, we deploy the migration agent on MyLocalContainer
		 */
		
		c = containerList.get("MyLocalContainer");
		Assert.assertNotNull("This container does not exist",c);
		agentName="Ulysse1";
		String gatekeeperName="GK";
		try {
			Object[] objtab=new Object[]{gatekeeperName};//used to give informations to the agent
			AgentController	ag=c.createNewAgent(agentName,DummyMigrationAgent.class.getName(),objtab);
			agentList.add(ag);
			System.out.println(agentName+" launched");
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
		/*
		 * Distributed, the main is here, we deploy the GateKeeper on container0
		 */
//		c = containerList.get("MyDistantContainer0");
//		Assert.assertNotNull("This container does not exist",c);
//		agentName="GK";
//		try {
//			Object[] objtab=new Object[]{"src/main/resources/map2015","src/main/resources/map2015-config"};//used to give informations to the agent
//			AgentController	ag=c.createNewAgent(agentName,GateKeeperAgent.class.getName(),objtab);
//			agentList.add(ag);
//			System.out.println(agentName+" launched");
//		} catch (StaleProxyException e) {
//			e.printStackTrace();
//		}


		System.out.println("Agents launched...");
		return agentList;
	}

	/**
	 * Start the agents
	 * @param agentList
	 */
	private static void startAgents(List<AgentController> agentList){

		System.out.println("Starting agents...");


		for(final AgentController ac: agentList){
			try {
				ac.start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}

		}
		System.out.println("Agents started...");
	}

}






