package mas.behaviours;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import env.Attribute;
import env.Couple;
import jade.core.behaviours.SimpleBehaviour;
import mas.agents.BFSExploAgent;
import mas.graph.Graph;
import mas.graph.Node;

public class SiloWalkBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1830274726141667787L;

	private Graph graph;

	public SiloWalkBehaviour(final mas.abstractAgent myagent, Graph graph) {
		super(myagent);
		this.graph=graph;
	}

	@Override
	public void action() {

		System.out.println(myAgent.getLocalName()+"************************SiloWalkBehaviour****************************");
		//Example to retrieve the current position
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();


		if (myPosition!=""){

			//List of observable from the agent's current position
			List<Couple<String,List<Attribute>>> lobs=((mas.abstractAgent)this.myAgent).observe();//myPosition
			//System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
			String id = lobs.get(0).getLeft();

			if(graph.isCompleted()){
				//get path to center
				Node goalNode = graph.getSiloPosition();
				if(goalNode.getId()!=id)
				{
					ArrayList<Node> path = graph.getPath(graph.getNode(id), goalNode);
					if(path!=null){
						String moveTo = path.get(1).getId();
						//boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(moveTo); //
						System.out.println("!!!!!!!!!!!!!!!!!!SILO MOVING TOOOOOO :: "+moveTo+" bc position is : "+goalNode.getId()+
								"\n and complete path is "+path.toString());
					}
				}
			}
			else
			{
				//Random move from the current position
				Random r= new Random();
				//1) get a couple <Node ID,list of percepts> from the list of observables
				int moveId=r.nextInt(lobs.size());
				//2) Move to the picked location. The move action (if any) MUST be the last action of your behaviour
				((mas.abstractAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
			}



		}

	}

	@Override
	public boolean done() {

		return true;
	}

}
