package mas.behaviours;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import env.Attribute;
import env.Couple;
import jade.core.behaviours.SimpleBehaviour;
import mas.graph.Graph;
import mas.graph.Node;

public class CollectorWalkBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4023577769135483172L;
	private Graph graph;
	private String treasureType;
	private String moveTo;
	
	
	public CollectorWalkBehaviour (final mas.abstractAgent myagent, Graph graph) {
		super(myagent);
		this.graph = graph;
		this.treasureType = ((mas.abstractAgent)this.myAgent).getMyTreasureType();
		
	}
	
	@Override
	public void action() {
		System.out.println(myAgent.getLocalName()+"************************CollectorWalkBehaviour****************************");
		// TODO Auto-generated method stub
		List<Couple<String,List<Attribute>>> lobs=((mas.abstractAgent)this.myAgent).observe();
		String id = lobs.get(0).getLeft();
		List<Attribute> lattribute= lobs.get(0).getRight();
		Node currentNode = graph.getNode(id);
		
		//Empty backpack is silo agent nearby
		((mas.abstractAgent)this.myAgent).emptyMyBackPack("Silo");			
		System.out.println(myAgent.getLocalName()+" : the remaining backpack capacity after calling to empty is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
		//compare treasure value to the agent's
		//if it's the same, pick some of it if there is enough space
		System.out.println(myAgent.getLocalName()+" : My type is : "+((mas.abstractAgent)this.myAgent).getMyTreasureType());
		System.out.println(myAgent.getLocalName()+" : My current backpack capacity is:"+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());

		for(Attribute a:lattribute){
			System.out.println(myAgent.getLocalName()+" : Type of the treasure on the current position: "+a.getName());
			System.out.println(myAgent.getLocalName()+" : Value of the treasure on the current position: "+a.getValue());
			System.out.println(myAgent.getLocalName()+" : the remaining backpack capacity is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
			if(a.getName().equals(treasureType)) // same treasure type 
			{
				System.out.println(myAgent.getLocalName()+" : The agent grabbed :"+((mas.abstractAgent)this.myAgent).pick());
				System.out.println(myAgent.getLocalName()+" : the remaining backpack capacity is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
				//Empty backpack is silo agent nearby
				((mas.abstractAgent)this.myAgent).emptyMyBackPack("Silo");
				System.out.println(myAgent.getLocalName()+" : the remaining backpack capacity after calling to empty is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
				
				if((int) a.getValue()<((mas.abstractAgent)this.myAgent).getBackPackFreeSpace()) //enough space in the backpack
				{
					//((mas.abstractAgent)this.myAgent).pick();
					//Node node = graph.getNode(lobs.get(0).getLeft());
					//node.setContent(contentList); TODO 13.4.2018 : update content
					//System.out.println(myAgent.getLocalName()+" : The agent grabbed :"+((mas.abstractAgent)this.myAgent).pick());
					//System.out.println(myAgent.getLocalName()+" : the remaining backpack capacity is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
					//System.out.println(myAgent.getLocalName()+" : The value of treasure on the current position: (unchanged before a new call to observe()): "+a.getValue());
					
				}
				else // not enough space
				{
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!Not enough place in the backpack :(");
				}
			}
			else // not same treasure type
			{
				System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOO not my type :(");
			}

		}

		if(currentNode != null)
		{
			lobs=((mas.abstractAgent)this.myAgent).observe(); //update content
			List<Attribute> newContentList = lobs.get(0).getRight();
			if(!currentNode.getVisited() || currentNode.hasChanged(newContentList)){
				currentNode.setVisited(true);
				currentNode.setContent(newContentList);
				//System.out.println(myAgent.getLocalName()+" : The value of treasure on the current position: (unchanged before a new call to observe()): "+a.getValue());
				
			}

			Node goalNode;
			if(((mas.abstractAgent)this.myAgent).getBackPackFreeSpace() < 10){
				goalNode = graph.getSiloPosition(); //we must empty the backpack
			}
			else //there is enough space
			{
				goalNode = graph.getBestNode(treasureType);
			}
			ArrayList<Node> path = graph.getPath(currentNode, goalNode);
			if(path!=null){
				moveTo = path.get(1).getId();
				boolean moved = ((mas.abstractAgent)this.myAgent).moveTo(moveTo); //
				System.out.println("!!!!!!!!!!!!!!!!!!MOVING TOOOOOO :: "+moveTo+" bc bestNode is : "+goalNode.getId()+
						"\n and complete path is "+path.toString());
			}
			else
			{
				System.out.println("path is null ! ");
				System.out.println("CollectorAgent : "+myAgent.getLocalName()+" : no graph received containing currentNode yet ! my id = "+id+" and graph is : ");
				//Random move from the current position
				Random r= new Random();
				//1) get a couple <Node ID,list of percepts> from the list of observables
				int moveId=r.nextInt(lobs.size());

				//2) Move to the picked location. The move action (if any) MUST be the last action of your behaviour
				((mas.abstractAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
				//graph.printNodes();
			}
		}

		else{
			System.out.println("CollectorAgent : "+myAgent.getLocalName()+" : no graph received containing currentNode yet ! my id = "+id+" and graph is : ");
			//Random move from the current position
			Random r= new Random();
			//1) get a couple <Node ID,list of percepts> from the list of observables
			int moveId=r.nextInt(lobs.size());

			//2) Move to the picked location. The move action (if any) MUST be the last action of your behaviour
			((mas.abstractAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
			//graph.printNodes();
		}

	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return true;
	}

}
