package de.tum.msrg.underlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import de.tum.msrg.utils.Utility;

/**
 * @author p00ya
 * Represents a physical node in the underlay network
 */
public class Node implements Comparable<Node> {
	
	protected static Network myNetwork;
	protected int id;
	protected Edge[] outLinks;
	/*
	 * routing table keeps next hop ID from the current node to all other nodes
	 */
	private int[] routeTable;
	private boolean congested;	
	/**
	 * Dijkstra data
	 */
	protected boolean dijkVisited;
	protected float dijkWeight;
	protected Node dijkParent;

	public Node(Network nw, int nodeID, int edgeCount) {
		myNetwork = nw;
		id = nodeID;
		outLinks = new Edge[edgeCount];
		congested = false;
		dijkVisited = false;
		dijkWeight = 0;
		dijkParent = null;
	}

	public void addEdge(Edge edge) throws UnderlayException {
		for (int i = 0; i < outLinks.length; i++) {
			if (outLinks[i] == null) {
				outLinks[i] = edge;
				return;
			}
		}
		throw new UnderlayException("cannot add another edge to node " + getID() + "! Increase out degree!");
	}

	public int getID() {
		return id;
	}

	public int getNoOfEdges() {
		return outLinks.length;
	}

	public Edge[] getOutLinks() {
		return outLinks;
	}

	public Edge getLink(Node toNode) {
		for (Edge link : outLinks) {
			if (link.getOtherEndNode(this) == toNode) return link;
		}
		return null;
	}

	public void toggleCongested() {
		congested = !congested;
	}

	public boolean isCongested() {
		return congested;
	}

	public void setDijkVisited(boolean val) {
		dijkVisited = val;
	}

	public boolean isDijkVisited() {
		return dijkVisited;
	}

	public void setDijkWeight(float w) {
		dijkWeight = w;
	}

	public float getDijkWeight() {
		return dijkWeight;
	}

	public void setDijkParentNode(Node parent) {
		dijkParent = parent;
	}

	public Node getDijkParentNode() {
		return dijkParent;
	}

	/**
	 * Initializes the routing table
	 */
	public void initRouteTable() {
		routeTable = new int[myNetwork.getNoOfNodes()];
		Arrays.fill(routeTable, -1);
	}

	/**
	 * Recursive function to build the routing table. First you have to build the
	 * Dijkstra tree from the Network instance. Then start this method's
	 * recursion from the destination node. It will recurse through the shortest
	 * path and terminate at the source node.
	 *
	 * @param route
	 * the shortest path route chain built in reverse order.
	 * Internally it is reversed in correct order to build the route
	 * map.
	 */
	public void setRoutes(RouteChain route) {
		List<Node> rChain = new ArrayList<Node>();
		if (dijkParent == null) {
			rChain = route.getRouterChain();
			Utility.reverse(rChain);
			Node nextNode = rChain.get(1);
			for (int i = 1; i < rChain.size(); i++) {
				routeTable[rChain.get(i).getID()] = nextNode.getID();
			}
		} else {
			Edge link = getLink(dijkParent);
			route.addLink(this, link);
			dijkParent.setRoutes(route);
		}
	}

	/**
	 * Returns the next hop node when routing towards the destNode.
	 *
	 * @param destNode
	 * the destination node.
	 * @return the next hop node.
	 */
	public Node getRoute(Node destNode) {
		int nextNodeIndex = routeTable[destNode.getID()];
		if (nextNodeIndex < 0) return null;
		return myNetwork.getNode(nextNodeIndex);
	}

	/**
	 * calculated the path latency from this node to destNode
	 * @param destNode
	 * @return path latency
	 * @throws UnderlayException
	 */
	public int getLatency(Node destNode) throws UnderlayException {
		Node nextNode = getRoute(destNode);
		if (nextNode == null) {
			System.out.printf("No route found %d->%d", id, destNode.getID());
			throw new UnderlayException(String.format("No route found %d->%d", id, destNode.getID()));
		}
		Edge link = getLink(nextNode);
		int delay = link.getDelay();
		if (nextNode.isCongested()) {
			throw new UnderlayException("Node congestion is not implemented yet!");
			// delay *= (1 + Network.CONGESTION_ERR);
		}
		return nextNode == destNode ? delay : delay + nextNode.getLatency(destNode);
	}

	public float getBandwidth(Node destNode) throws UnderlayException {
		Node nextNode = getRoute(destNode);
		if (nextNode == null) {
			System.out.printf("No route found %d->%d", id, destNode.getID());
			throw new UnderlayException(String.format("No route found %d->%d", id, destNode.getID()));
		}
		Edge link = getLink(nextNode);
		float bw = link.getBandwidth();
		return nextNode == destNode ? bw : Math.min(bw, nextNode.getBandwidth(destNode));
	}

	public void buildRouteChain(Node destNode, RouteChain route) throws UnderlayException {
		Node nextNode = getRoute(destNode);
		if (nextNode == null) {
			throw new UnderlayException(String.format("No route found %d->%d", id, destNode.getID()));
		}
		Edge link = getLink(nextNode);
		route.addLink(this, link);
		if (nextNode != destNode) {
			nextNode.buildRouteChain(destNode, route);
		}
	}

	public String toString() {
		return String.format("node%4d: outdeg=%d", id, outLinks.length);
	}

	public int compareTo(Node aNode) {
		return id - aNode.getID();
	}

	public boolean equals(Node aNode) {
		return id == aNode.getID();
	}

	public static Network getMyNetwork() {
		return Node.myNetwork;
	}

	public int[] getRouteTable() {
		return routeTable;
	}
}