package de.tum.msrg.overlay;

import com.sun.corba.se.pept.broker.Broker;
import de.tum.msrg.message.Message;
import de.tum.msrg.sim.StatsCollector;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.utils.SimLogger;
import jist.runtime.JistAPI;

public class OverlayLink {

	private boolean directed = false;
	private int latency;
	private BrokerBase fromNode;
	private BrokerBase toNode;

	public OverlayLink(BrokerBase fromNode, BrokerBase toNode, int latency) {
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.latency = latency;
	}

	// for mocking
	protected OverlayLink() {}

	public void setDirected() {
		directed = true;
	}

	public boolean isDirected() {
		return directed;
	}

	public boolean isToLink(BrokerBase toNode) {
		if (directed) {
			return this.toNode == toNode;
		}
		return (this.toNode == toNode) || (fromNode == toNode);
	}

	public BrokerBase getOtherEnd(BrokerBase thisEnd) {
		if(!thisEnd.equals(fromNode) && !thisEnd.equals(toNode))
			throw new RuntimeException("Invalid link endpoint!");
		if (directed && thisEnd.equals(fromNode))
			return toNode;
		else if(!directed)
			return thisEnd.equals(fromNode) ? toNode :fromNode;
		return null;
	}

	public NodeInfo getOtherEnd(NodeInfo thisEnd) {
		if(!thisEnd.equals(fromNode.getNodeInfo()) && !thisEnd.equals(toNode.getNodeInfo()))
			throw new RuntimeException("Invalid link endpoint!");
		if (directed && thisEnd.equals(fromNode.getNodeInfo()))
			return toNode.getNodeInfo();
		else if(!directed)
			return thisEnd.equals(fromNode.getNodeInfo()) ? toNode.getNodeInfo() :fromNode.getNodeInfo();
		return null;
	}

	@Override
	public String toString() {
		return "(link:" + fromNode + (directed ? "->" : "<=>") + toNode + ")";
	}

	public int getLatency() {
		return this.latency;
	}

	public void setLatency(final int latency) {
		this.latency = latency;
	}

	public BrokerBase getFromNode() {
		return this.fromNode;
	}

	public BrokerBase getToNode() {
		return this.toNode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		OverlayLink that = (OverlayLink) o;

		if (!fromNode.equals(that.fromNode)) return false;
		return toNode.equals(that.toNode);

	}

	@Override
	public int hashCode() {
		int result = fromNode.hashCode();
		result = 31 * result + toNode.hashCode();
		return result;
	}
}