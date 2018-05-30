package mas.agents;

public class Constants {
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
	
	
	public static final int HAS_PRIORITY = 1;
	public static final int GIVES_PRIORITY = 0;
	public static final int NO_RESPONSE = -1;
	
	public static final int MAX_PARENT_DEGREE = 4;
	
	public static final int MOVED = 1;
	public static final int BLOCKED = 0;

	public static final int SEND_GRAPH = 3;
	public static final int TIME_OUT = 2;
	public static final int GOTO_STATE_WALK = 0;
	public static final int GOTO_STATE_GRAPH_TRANSMISSION = 1;
	public static final int GOTO_STATE_INTERBLOCAGE_RESOLUTION = 2;
	public static final int GOTO_STATE_INTERBLOCAGE_LISTENER = 4;
	public static final int GOTO_GIVES_PRIORITY = 5;
	public  static String MESSAGE_GRAPH_RECEIVED = "Message Received";
	
	public static final String SERVICE_COLLECTOR = "collector";
	public static final String SERVICE_EXP = "explorator";
	public static final String SERVICE_TANK = "silo";

}
