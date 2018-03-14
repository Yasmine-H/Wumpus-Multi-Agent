package mas.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import env.Couple;
import java.util.HashMap;

public class Graph implements Serializable{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -157157695933610988L;
	private ArrayList<Node> graph;
	
	
	public Graph()
	{
		graph=new ArrayList<>();
	}
        
        public ArrayList<Node> getAllNodes(){
            return graph;
        }
	
	
	public int getNodeIndex(String id)
	{
		for(int i=0; i< graph.size(); i++){
			if(graph.get(i).getId().equalsIgnoreCase(id)){
				return i;
			}
		}
		
		return -1 ; //this node hasn't been added to the graph yet
	}
	
        public boolean isInGraph(String id){
            for(Node node: graph){
                if(node.getId().equalsIgnoreCase(id))
                    return true;
            }
            return false;
        }
	
        public void addNode(Node node){
            if(!isInGraph(node.getId())){
                graph.add(node);
            }
        }
        /*
	public void addNode(Node node)
	{
		int index = getNodeIndex(node.getId());
		if(index == -1)
		{
			graph.add(node);
			Collections.sort(graph); // sort the neighbours of a node
		}
	}
	*/
        
        
	public Node getNode(int index)
	{
		return graph.get(index);
	}
	/*
	public Node getNode(String id)
	{
		return getNode(getNodeIndex(id));
	}
        */
        public Node getNode(String id){
            for(Node node: graph){
                if(node.getId().equalsIgnoreCase(id)){
                    return node;
                }
            }
            return null;
        }
        /* 
            @params currentNode : the node in which we are starting
            @return : the nearest not visited node from the currentNode. Returns null if all nodes have been already visited.
        */
        public Node getClosestUnvisited(Node currentNode){
            //toExplore is a FIFO to perform our BFS = parcours en largeur
            //at the beginning, we put each neighbour of the currentNode (= initial node) to the FIFO
            ArrayList<Node> toExplore = (ArrayList<Node>) currentNode.getNeighbours().clone(); 

	    //The maintenance of the father relation - the key (1st element) 
            //is the node and the value (2nd element) is its father node on the path
            //we use it for the path reconstruction
            HashMap<Node, Node> cameFrom = new HashMap<Node, Node>();
            
	    //the currentNode does not have a father, and it is the father of each of its neighbours	
            cameFrom.put(currentNode, null);
            for(Node node: toExplore){
                cameFrom.put(node, currentNode);
            }
            
	    //while the FIFO is not empty (i.e. there is a node we didn't take into account in our BFS)	
            while(!toExplore.isEmpty()){
		//we pop up the first element of the FIFO    
                Node newVisited = toExplore.remove(0);
                
		//if this element is an unvisited node, we return it 
		//in fact, this function is not very useful - usually we don't need the node but the path to it
                if(!newVisited.getVisited()){
                    return newVisited;
                }
                //else: we add all its neighbours to the FIFO if they aren't yet there, and 
		//for eachneighbour, we precise that its father is newVisited
                for(Node neighbour: newVisited.getNeighbours()){
                    if(!cameFrom.containsKey(neighbour) && !toExplore.contains(neighbour)){
                        toExplore.add(neighbour);
                        cameFrom.put(neighbour, newVisited);
                    }
                }
            }
            
            //else : all nodes have already been visited, there is nothing to visit
            return null;
        }
        
        /* @param Node finalNode : the node we want to go into
	   @param HashMap<Node, Node> cameFrom : the key is the node, the value is its father in the arborescence 
	   @return ArrayList<Node> path: return the path from the initial node to the finalNode
	*/
        private ArrayList<Node> pathReconstruction(Node finalNode, HashMap<Node, Node> cameFrom){
            
	    ArrayList<Node> path = new ArrayList<>();
            path.add(finalNode);
        //    System.out.println("The nearest node: "+finalNode.toString());
            
	    //we find the father of the final node	
	    Node father = cameFrom.get(finalNode);
            
	    //while the node has a father, we can "remote the path"
	    //the only node without the father is the initial node
            while(father != null){
            //    System.out.println("Father: "+father.toString());
		
		//we add the father at the beginning of the path, as we are remoting the path     
                path.add(0, father);
		//and we find its father...    
                father = cameFrom.get(father);
            }
            
            return path;
        }
        
        /* 
            @params currentNode : the node in which we are starting
            @return : the path (AttayList<Node>) to the nearest not visited node. Returns null if every node has been already visited.
        */
        public ArrayList<Node> getPathToClosestUnvisited(Node currentNode){
            //toExplore is the FIFO of nodes to explore
            //System.out.println(currentNode.getNeighbours().toString());
            ArrayList<Node> toExplore = (ArrayList<Node>) currentNode.getNeighbours().clone(); 
            //the father relation: the first composant represents the node we have reached, the second represents its father on the path
            HashMap<Node, Node> cameFrom = new HashMap<Node, Node>();
            
            //the node in which we are starting doesn't have any father
            cameFrom.put(currentNode, null);
            
            //for each neighbour of currentNode, currentNode is its father
            for(Node node: toExplore)    
                cameFrom.put(node, currentNode);
            
            
            //while the FIFO is not empty (i.e. there is a node we didn't take into account in our BFS)	
            while(!toExplore.isEmpty()){
                //we pop up the first element of the FIFO
		Node newVisited = toExplore.remove(0);
                
		//if this element is an unvisited node, we return the path to it
                if(!newVisited.getVisited()){
                    //System.out.println(cameFrom.toString());
                    return pathReconstruction(newVisited, cameFrom);
                }
                //else:
                for(Node neighbour: newVisited.getNeighbours()){
                    if(!toExplore.contains(neighbour) && !cameFrom.containsKey(neighbour)){
                        toExplore.add(neighbour);
                        cameFrom.put(neighbour, newVisited);
                    }
                }
            }
            
            //else : all nodes have already been visited
            return null;
        }
        /*
	public String getClosestUnvisited(String id, ArrayList<Couple<String, String>> coupleIdsList) {
		
		
		//idsList.add(id); //contains the nodes that have already been considered in the search (saves us from the infinite loop)
		Node currentNode = getNode(id);
		ArrayList<String> listNeighboursId = currentNode.getNeighbours();
		System.out.println("///////////Current node : "+id+" = "+currentNode.getId()+" ? and list of neighbours : "+listNeighboursId.toString());
		
		for(String neighbour: listNeighboursId){
			int neighbourIndex = getNodeIndex(neighbour);
			if(!getNode(neighbourIndex).getVisited())
			{
				System.out.println("//////////////////// Id = "+id+" and neighbour "+neighbour+" hasn't been visisted");
				return neighbour;
			}
			else
				System.out.println("////////////////Neighbour "+neighbour+" has already been visited !");
		}
		
		//if all the direct neighbours have already been visited
		String result = null;
		int i=0;
		while(result==null && i<listNeighboursId.size())
		{
			String neigh = listNeighboursId.get(i);
			Couple<String,String> id_neigh = new Couple<String,String>(id, listNeighboursId.get(i));
			//Couple<String,String> neigh_id = new Couple<String,String>(listNeighboursId.get(i), id);
			boolean explored = false;
			for(int j=0; j<coupleIdsList.size(); j++)
			{
				if( (coupleIdsList.get(j).getLeft().equals(id) && coupleIdsList.get(j).getRight().equals(neigh))
					|| (coupleIdsList.get(j).getLeft().equals(neigh) && coupleIdsList.get(j).getRight().equals(id)))
					{
						explored = true;
						break;
					}
			}
			
			if(!explored)
			{
				
				coupleIdsList.add(id_neigh);
				System.out.println("-----------About to visit node : "+listNeighboursId.get(i));
				result=getClosestUnvisited(listNeighboursId.get(i), coupleIdsList);
			}
			
			if(result!=null)
				return listNeighboursId.get(i);
			else
				i++;
		}
		
		return null;
	}
	*/
	
	
/*	
public String getClosestUnvisited2(String id, ArrayList<String> idsList) {
		
		
		idsList.add(id); //contains the nodes that have already been considered in the search (saves us from the infinite loop)
		Node currentNode = getNode(id);
		ArrayList<String> listNeighboursId = currentNode.getNeighbours();
		
		System.out.println("///////////Current node : "+id+" = "+currentNode.getId()+" ? and list of neighbours : "+listNeighboursId.toString());
		
		for(int i=0; i<listNeighboursId.size(); i++)
		{
			if(!getNode(listNeighboursId.get(i)).getVisited())
			{
				System.out.println("//////////////////// Id = "+id+" and neighbour "+listNeighboursId.get(i)+" hasn't been visisted");
				return listNeighboursId.get(i);
			}
			else
			{
				System.out.println("////////////////Neighbour "+listNeighboursId.get(i)+" has already been visited !");
			}
		}*/
		/*
		for(String neighbour: listNeighboursId){
			int neighbourIndex = getNodeIndex(neighbour);
			if(!getNode(neighbourIndex).getVisited())
			{
				return neighbour;
			}
		}
		*/
        /*
		//if all the direct neighbours have already been visited
		String result = null;
		int i=0;
		while(result==null && i<listNeighboursId.size())
		{
			
			if(!idsList.contains(listNeighboursId.get(i)))
			{
				
				result=getClosestUnvisited2(listNeighboursId.get(i), idsList);
			}
			
			if(result!=null)
				return listNeighboursId.get(i);
			else
				i++;
		}
		
		return null;
	}
	
	*/
	
	public void printNodes()
	{
		System.out.println("***********List of the "+graph.size()+" known Nodes : ");
		for(int i=0; i<graph.size(); i++)
		{
			System.out.println("Node id : "+graph.get(i).getId()+" --- visited : "+graph.get(i).getVisited()+"\nList of neighbours : "+graph.get(i).getNeighbours().toString());
		}
	}
	
		public void fusion(Graph graph2)
	{
		
		for(Node node2 : graph2.getAllNodes())//loop for adding the new nodes
		{
			
			int index = getNodeIndex((node2.getId()));
			if(index == -1) //node doesn't exist in the graph
			{
				graph.add(node2.clone()); // WARNinG NEIGHBOUURSSS pointeurs
				//TODO 13.3:
				//We need to loop over all neighbours of this node in the graph2, and remove all neighbours of the copy
				// .... if the neighbour exists in the first graph, we add it into the list of neighbours 
				// .... else, we need to create the clone of the neighbour and add it into the the list of neighbours of the node we've just created
				// ... WARNING : By doing this, we risk to loose the neighbours of the neighbour 
				// ... two solutions proposed : 	- if the neighbour does not exist in the first graph, we don't add it for the moment; 
				//									  every time the new node is created, we add it as a neighbour of all its neighbours 
				//									- recursion??? I don't see how, the first solution is prefered
				
				//first of all, we remove all the neighbours of the clone
				getNode(node2.getId()).clearNeighbours();
				for(Node neighbour: node2.getNeighbours()) {
					int i = getNodeIndex(neighbour.getId());
					if(i != -1) { //the node exists in the original graph 
						getNode(node2.getId()).addNeighbour(getNode(i)); //we add it as a neighbour of if
					}
					//else : we do nothing
				}
			}
			
		}
		
		for(Node node2 : graph2.getAllNodes()) //loop for adding/updating the neighbours pointers
		{	
			int index = getNodeIndex((node2.getId()));
			if(!graph.get(index).getVisited()) //node has not been already visited so not all the neighoburs are set//   
			{
				if(node2.getVisited())
				{
					graph.get(index).setContent(node2.getContentList());

					graph.get(index).setVisited(true);
						//adding neighbours
						for(Node neighbour2 : node2.getNeighbours()) // TODO : modification de addneighbour pour ajouter deux arcs : node - neighobur et neighobur-nde
						{
							int index_neigh = getNodeIndex(neighbour2.getId());
							//if(index != -1) //the node of neighbour2 already exists
							//{
							graph.get(index).addNeighbour(graph.get(index_neigh));
							//}
							
						}
				}

			}
			else // we visited the node
			{
				if(node2.getVisited() && graph.get(index).getTime() < node2.getTime()) //content may have been changed 
				{
					graph.get(index).setContent(node2.getContentList());
				}
			}
			
		}
	
	}
	

}
