/////////////////////////////////////////////////////////////////////////////
// Semester: CS367 Spring 2017
// PROJECT: Program 5
// FILE: GraphADT.java, GraphNode.java, InvalidFileException.java,
//		 Location.java, MapApp.java, NavigationGraph.java, Path.java
//
// Author1: Yipeng Cao, cao64@wisc.edu, cao64, Lecture 002 
// Author2: Yong Jae Cho, ycho55@wisc.edu, ycho55, Lecture 002
// ---------------- OTHER ASSISTANCE CREDITS
// Persons: Identify persons by name, relationship to you, and email.
// Describe in detail the the ideas and help they provided.
//
// Online sources: avoid web searches to solve your problems, but if you do
// search, be sure to include Web URLs and description of
// of any information you find.
///////////////////////////////////////////////////////////////////////////////

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class NavigationGraph implements GraphADT<Location, Path> {
	
	private String[] edgePropertyNames;
	private List<GraphNode<Location, Path>> vertices;
	private int numid = 0; // numid is to trace index of vertices and DijkstrasList

	public NavigationGraph(String[] edgePropertyNames) {
		this.edgePropertyNames = edgePropertyNames;
		vertices = new ArrayList<GraphNode<Location, Path>>();
	}

	/**
	 * Returns a Location object given its name
	 * 
	 * @param name name of the location
	 * @return Location
	 */
	public Location getLocationByName(String name) {
		if (name == null)
			throw new IllegalArgumentException("Null location name");
		// find specific location object in vertices list 
		for (GraphNode<Location, Path> node : vertices)
			if (node.getVertexData().getName().equals(name))
				return node.getVertexData();
		return null;
	}

	@Override
	/**
	 * Adds a vertex to the Graph
	 * 
	 * @param vertex
	 *            vertex to be added
	 */
	public void addVertex(Location vertex) {
		int locationID = getLocationID(vertex);
		if (locationID == -1) {
			// do not have this source vertex
			vertices.add(new GraphNode<Location, Path>(vertex, numid++));
		}
	}

	
	/**
	 * Get location id with location input
	 * 
	 * @param location specific location
	 * @return locationID num (-1 when do not find in the vertices list)
	 */
	private int getLocationID(Location location) {
		for (int i = 0; i < vertices.size(); i++)
			if (vertices.get(i).getVertexData().equals(location))
				return i;
		return -1;
	}
	@Override
	/**
	 * Creates a directed edge from src to dest
	 * 
	 * @param src source vertex from where the edge is outgoing
	 * @param dest destination vertex where the edge is incoming
	 * @param edge edge between src and dest
	 */
	public void addEdge(Location src, Location dest, Path edge) {
		int locationID = getLocationID(src);
		// do not have this source vertex
		if (locationID == -1) {
			List<Path> temp = new ArrayList<Path>();
			temp.add(edge);
			vertices.add(new GraphNode<Location, Path>(src, temp, 0));
		} else {
			// have this source vertex, add this path to existing path list
			for (GraphNode<Location, Path> node : vertices) {
				if (node.getId() == locationID) {
					node.getOutEdges().add(edge);
					return;
				}
			}
		}
	}

	@Override
	/**
	 * Getter method for the vertices
	 * 
	 * @return List of vertices of type V
	 */
	public List<Location> getVertices() {
		List<Location> temp = new ArrayList<Location>();
		for (GraphNode<Location, Path> node : vertices)
			temp.add(node.getVertexData());
		return temp;
	}

	@Override
	/**
	 * Returns edge if there is one from src to dest vertex else return null
	 * 
	 * @param src Source vertex
	 * @param dest Destination vertex
	 * @return Edge from src to dest
	 */
	public Path getEdgeIfExists(Location src, Location dest) {
		for (GraphNode<Location, Path> node : vertices)
			if (node.getVertexData().equals(src)) // if contains source
				for (Path path : node.getOutEdges())
					if (path.getDestination().equals(dest)) // if contains destination
						return path;
		return null;
	}

	@Override
	/**
	 * Returns the outgoing edges from a vertex
	 * 
	 * @param src Source vertex
	 * @return List of edges
	 */
	public List<Path> getOutEdges(Location src) {
		for (GraphNode<Location, Path> node : vertices)
			if (node.getVertexData().equals(src))
				return node.getOutEdges();
		return null;
	}

	@Override
	/**
	 * Returns neighbors of a vertex
	 * 
	 * @param vertex vertex for which the neighbors are required
	 * @return List of neighbors
	 */
	public List<Location> getNeighbors(Location vertex) {
		// neighbors means two ways connected (source and destination)
		List<Location> temp = new ArrayList<Location>();
		for (GraphNode<Location, Path> node : vertices)
			// if it is "vertex" is source
			if (node.getVertexData().equals(vertex))
				for (Path path : node.getOutEdges())
					temp.add(path.getDestination());
			// if it is "vertex" is destination
			else
				for (Path path : node.getOutEdges())
					if (path.getDestination().equals(vertex))
						temp.add(path.getDestination());
		return temp;
	}

	@Override
	/**
	 * Calculate the shortest route from src to dest vertex using edgePropertyName
	 * 
	 */
	public List<Path> getShortestRoute(Location src, Location dest, String edgePropertyName) {
		if (src == null || dest == null || edgePropertyName == null)
			throw new IllegalArgumentException(src + ", " + dest + " and/or "
					+ edgePropertyName
					+ " are not valid Location name and/or edge property name in the graph");
		if (src.equals(dest))
			throw new IllegalArgumentException(
					src + " and " + dest + " correspond to the same Location");
		// return the shortestRoute based on edge property
		List<Path> pathList = new ArrayList<Path>();
		// get index of which property matters
		int properties = Arrays.asList(this.edgePropertyNames)
				.indexOf(edgePropertyName);

		// *** declare new class ***
		// <package> class that store graph node data, visited state, total
		// weight, predecessor
		class DijkstrasNode {
			// initialize each vertex visited mark to false
			boolean verticesVisited = false;
			// initialize each vertex total weight to "infinity"
			int totalWeight = Integer.MAX_VALUE;
			// initialize each vertex predecessor to null
			DijkstrasNode predecessor = null;
			// id containing the same info as GraphNode's id
			// this is also a connection of DijkstrasNode and GraphNode and
			// indexing in the list
			int numid;
			// reference to graphNode
			GraphNode<Location, Path> GraphNodeData;
		}

		// *** Dijkstras algorithm ***
		// Dijkstraslist is to represent more info of all vertices
		List<DijkstrasNode> Dijkstraslist = new ArrayList<DijkstrasNode>(
				vertices.size());
		// linking every elements in Dijkstraslist to vertices list, with respect
		// to the indexing
		for (int i = 0; i < vertices.size(); i++) {
			Dijkstraslist.add(new DijkstrasNode());
			Dijkstraslist.get(i).GraphNodeData = vertices.get(i);
			// update id
			Dijkstraslist.get(i).numid = vertices.get(i).getId();
		}

		// Using comparator to pass to priority queue to compare totalWeight
		Comparator<DijkstrasNode> comparator = new Comparator<DijkstrasNode>() {
			@Override
			public int compare(DijkstrasNode d1, DijkstrasNode d2) {
				return (int) ((Integer) d1.totalWeight)
						.compareTo(((Integer) d2.totalWeight));
			}
		};

		// create new priority queue pq
		PriorityQueue<DijkstrasNode> pq = new PriorityQueue<DijkstrasNode>(
				comparator);
		// traverse to find start vertex
		for (int i = 0; i < Dijkstraslist.size(); i++) {
			if (vertices.get(i).getVertexData().equals(src)) {
				DijkstrasNode startingNode = Dijkstraslist.get(i);
				// set start vertex's total weight to 0
				startingNode.totalWeight = 0;
				pq.add(startingNode); // same as insert()
				break;
			}
		}
		while (!pq.isEmpty()) {
			// pick next unvisited vertex with lowest calculated distance
			DijkstrasNode currNode = pq.poll();

			// Updating suc's total weight in priority queue
			 
			if (!currNode.verticesVisited) {

				currNode.verticesVisited = true; // set visited state to true
				// for each unvisited successor S adjacent to currNode
				for (Path path : currNode.GraphNodeData.getOutEdges()) {
					DijkstrasNode suc = Dijkstraslist
							.get(getID(path.getDestination()));
					// if this successor is not visited yet
					if (!suc.verticesVisited) {
						// suc's total weight = curr's total weight + edge weight
						// from currNode to S
						int newtotalWeight = (int) (currNode.totalWeight
								+ getEdgeIfExists(
										currNode.GraphNodeData.getVertexData(),
										suc.GraphNodeData.getVertexData())
												.getProperties()
												.get(properties));
						if (suc.totalWeight > newtotalWeight) {
							suc.totalWeight = newtotalWeight;
							// update suc's predecessor to currNode
							suc.predecessor = currNode;
						}
						pq.add(suc);
					}
				}
			}
		}

		// Adding path to the array
		DijkstrasNode curr = Dijkstraslist.get(getID(dest));
		while (curr.predecessor != null) {
			for (Path path : curr.predecessor.GraphNodeData.getOutEdges())
				if (path.getDestination()
						.equals(curr.GraphNodeData.getVertexData()))
					pathList.add(0, path);
			curr = curr.predecessor;
		}
		return pathList;

	}

	@Override
	/**
	 * Getter method for edge property names
	 * 
	 * @return array of String that denotes the edge property names
	 */
	public String[] getEdgePropertyNames() {
		return this.edgePropertyNames;
	}

	/**
	 * Return a string representation of the graph
	 * 
	 * @return String representation of the graph
	 */
	public String toString() {
		String string = "";
		for (int i = 0; i < vertices.size(); i++) {
			for (int j = 0; j < vertices.get(i).getOutEdges().size(); j++) {
				string += vertices.get(i).getVertexData().toString() + " ";
				for (double properties : vertices.get(i).getOutEdges().get(j).getProperties())
					string += properties + " ";
				string += vertices.get(i).getOutEdges().get(j).getDestination();
				if (j < vertices.get(i).getOutEdges().size() - 1)
					string += ",";
			}
			if (i < vertices.size() - 1)
				string += "\n";
		}
		return string;
	}
	/**
	 * it is helper method to get node
	 * 
	 * @param src source location
	 * @return vertices that has same location, otherwise null
	 */
	private GraphNode<Location, Path> getNode(Location src) {

		for (int i = 0; i < vertices.size(); i++) {
			if (vertices.get(i).getVertexData().equals(src)) {
				return vertices.get(i);
			}
		}
		return null;
	}

	/**
	 * helper method to get id with location input
	 * 
	 * @param loc destination from MapApp
	 * @return i i for the id number, -1 otherwise
	 */
	private int getID(Location loc) {
		for (int i = 0; i < vertices.size(); i++)
			if (vertices.get(i).getVertexData().equals(loc))
				return i;
		return -1;
	}
	
}
