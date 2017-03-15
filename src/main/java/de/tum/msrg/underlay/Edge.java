package de.tum.msrg.underlay;

public class Edge {
	private static Network myNetwork;
	protected Node fromNode;
	protected Node toNode;
	private int bandwidth;
	private int delay;

	public Edge(Network nw, Node fNode, Node tNode, int bw, int edgeDelay) {
		myNetwork = nw;
		fromNode = fNode;
		toNode = tNode;
		bandwidth = bw;
		delay = edgeDelay;
	}

	public float getBandwidth() {
		return bandwidth;
	}

	public int getDelay() {
		return delay;
	}

	public Node getFromNode() {
		return fromNode;
	}

	public Node getToNode() {
		return toNode;
	}

	public Node getOtherEndNode(Node node) {
		return fromNode == node ? toNode : fromNode;
	}

	/*
	 * whether this edge is link from fNode to tNode
	 */
	public boolean isLink(Node fNode, Node tNode) {
		if ((fromNode == fNode && toNode == tNode) || (toNode == fNode && fromNode == tNode))
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("%d--%.2f-->%d", fromNode.getID(), delay, toNode.getID());
	}
}
