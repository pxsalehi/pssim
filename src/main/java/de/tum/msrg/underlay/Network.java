package de.tum.msrg.underlay;

import java.util.Random;

import de.tum.msrg.pubsub.ConfigKeys;
import de.tum.msrg.config.Configuration;
import de.tum.msrg.topology.EdgeInfo;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.topology.Topology;

public class Network {

	protected Configuration config;
	protected Topology topology;
	protected static Random randGen;
	protected int noOfNodes;
	protected int noOfEdges;
	protected Node[] nodes;
	protected Edge[] edges;
	private boolean routingTablesBuilt = false;
	private int dijkBuildSrc = -1;

	/**
	 * Constructor constructs the network with nodes and edges in the topology object
	 * @throws UnderlayException 
	 */
	public Network(Configuration config, Topology topo) throws UnderlayException {
		this.config = config;
		this.topology = topo;
		readConfig();
		createNetwork();
	}

	/**
	 * Reads the network creation configuration parameters.
	 */
	private void readConfig() {
		long randSeed = config.getLongConfig(ConfigKeys.SIM_RANDOM_SEED);
		if (randSeed == -1)
			randSeed = System.currentTimeMillis();
		randGen = new Random(randSeed);
	}
	
	/**
	 * create underlay network based on the topology
	 * @throws UnderlayException 
	 */
	protected void createNetwork() throws UnderlayException {
		noOfNodes = topology.getNoOfNodes();
		noOfEdges = topology.getNoOfEdges();
		nodes = new Node[noOfNodes];
		edges = new Edge[noOfEdges];
		for(NodeInfo n: topology.getNodes()) {
			nodes[n.getId()] = new Node(this, n.getId(), n.getDegree());
		}
		for(EdgeInfo e: topology.getEdges()) {
			Node from = nodes[e.getFrom().getId()];
			Node to = nodes[e.getTo().getId()];
			Edge edge = new Edge(this, from, to, e.getBandwidth(), e.getDelay());
			edges[e.getId()] = edge;
			from.addEdge(edge);
			to.addEdge(edge);
		}
	}

	public int getNoOfNodes() {
		return noOfNodes;
	}

	public int getNoOfEdges() {
		return noOfEdges;
	}

	public Edge[] getEdges() {
		return edges;
	}

	public Node[] getNodes() {
		return nodes;
	}

	public Node getNode(int nodeID) {
		return nodes[nodeID];
	}
	
	public Edge getEdge(int edgeID) {
		return edges[edgeID];
	}

	public Edge getEdge(Node fromNode, Node toNode) {
		if (fromNode != null && toNode != null) {
			for (Edge link : fromNode.getOutLinks()) {
				if (link.isLink(fromNode, toNode))
					return link;
			}
		}
		return null;
	}

	/**
	 * Initiates and build the routing tables in all nodes.
	 * 
	 */
	public void buildRouteTables() {
		// initialize the the routing table in the source node
		for (Node srcNode : nodes)
			srcNode.initRouteTable();
		for (Node srcNode : nodes) {
			// build the Dijkstra tree rooted at the source node
			buildDijkistraTree(srcNode);
			// get all the routing chains towards all other nodes
			for (Node destNode : nodes) {
				Node routeCheck = srcNode.getRoute(destNode);
				if (destNode != srcNode && routeCheck == null) {
					RouteChain route = new RouteChain(noOfNodes);
					destNode.setRoutes(route);
				}
			}
		}
		routingTablesBuilt = true;
	}

	/**
	 * First the buildRouteTable() method should've called to build the routing
	 * tables in all the nodes.
	 * 
	 * @param srcNode
	 * @param destNode
	 * @return a route chain from srcNode to destNode
	 * @throws UnderlayException throws an exception if no route is found or srcNode and destNode
	 * are the same
	 */
	public RouteChain findRoute(Node srcNode, Node destNode) throws UnderlayException {
		if (!routingTablesBuilt) {
			throw new UnderlayException("buildRouteTable() method should have been called " +
								        "before calling findRoute()");
		}
		RouteChain route = new RouteChain(noOfNodes);
		srcNode.buildRouteChain(destNode, route);
		route.trimToSize();
		return route;
	}
	/**
	 * Build Dijkistra's shortest path from a specified source node to all other
	 * node using the state variables in the node instances.
	 * 
	 * @param srcNode
	 *            The source node from where the Dijkstra shortest path tree is
	 *            to be built.
	 */
	public void buildDijkistraTree(Node srcNode) {
		// initialize
		for (Node node : nodes) {
			node.setDijkVisited(false);
			node.setDijkParentNode(null);
			node.setDijkWeight(Float.MAX_VALUE);
		}
		srcNode.setDijkWeight(0);
		// run Dijkstra algorithm
		while (true) {
			// find the next node with minimum cost link
			// considering delay as the link cost
			float minCost = Float.MAX_VALUE;
			Node nextNode = null;
			for (Node candidate : nodes) {
				if (!candidate.isDijkVisited() && candidate.getDijkWeight() < minCost) {
					nextNode = candidate;
					minCost = candidate.getDijkWeight();
				}
			}
			if (nextNode == null)
				break;
			// remove the node from future consideration
			nextNode.setDijkVisited(true);
			// relax the costs of the links branch from the chosen node
			for (Edge link : nextNode.getOutLinks()) {
				Node toNode = link.getOtherEndNode(nextNode);
				float suggestedWeight = nextNode.getDijkWeight() + link.getDelay();
				if (suggestedWeight < toNode.getDijkWeight()) {
					toNode.setDijkWeight(suggestedWeight);
					toNode.setDijkParentNode(nextNode);
				}
			}
		}
		dijkBuildSrc = srcNode.getID();
	}
	
	/**
	 * Finds the bottleneck bandwidth and node in a given route. Also check
	 * whether the given AS is in the given route.
	 */
	 //public float[] findBottleNeck(RouteChain route)
}
