package de.tum.msrg.topology;

import java.util.HashSet;
import java.util.Set;

public class NodeInfo {
	private int id;
	private int degree;
	// max number of messages per second this node can send
	private long throughput;
	// max number of msgs per second this node can handle
	private long inputCapacity;
	private Set<NodeInfo> neighbors = new HashSet<NodeInfo>();

	public NodeInfo(int id, int degree, long throughput, long inputCapacity) {
		this.id = id;
		this.degree = degree;
		this.throughput = throughput;
		this.inputCapacity = inputCapacity;
	}

	public NodeInfo(int id, int degree, long throughput) {
		this(id, degree, throughput, Long.MAX_VALUE);
	}

	public NodeInfo(int id, int degree) {
		this(id, degree, Long.MAX_VALUE);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getDegree() {
		return degree;
	}
	
	public void setDegree(int degree) {
		this.degree = degree;
	}

	public long getThroughput() {
		return throughput;
	}

	public void setThroughput(long throughput) {
		this.throughput = throughput;
	}

	public long getInputCapacity() {
		return inputCapacity;
	}

	public void setInputCapacity(long inputCapacity) {
		this.inputCapacity = inputCapacity;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeInfo other = (NodeInfo) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public void addNeighbor(NodeInfo neighbor) {
		neighbors.add(neighbor);
	}

	public Set<NodeInfo> getNeighbors() {
		return neighbors;
	}

	@Override
	public String toString() {
		return String.format("id:%d\tdegree:%d", id, degree);
	}
}