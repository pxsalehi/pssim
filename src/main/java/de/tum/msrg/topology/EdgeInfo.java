package de.tum.msrg.topology;

public class EdgeInfo {
	private int id;
	private NodeInfo from;
	private NodeInfo to;
	private int bandwidth;
	private int delay;
	
	public EdgeInfo(int id, NodeInfo from, NodeInfo to, int bandwidth, int delay) {
		this.id = id;
		this.from = from;
		this.to = to;
		this.bandwidth = bandwidth;
		this.delay = delay;
	}
	
	public EdgeInfo(int id, NodeInfo from, NodeInfo to, int delay) {
		this(id, from, to, Integer.MAX_VALUE, delay);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public NodeInfo getFrom() {
		return from;
	}
	
	public void setFrom(NodeInfo from) {
		this.from = from;
	}
	
	public NodeInfo getTo() {
		return to;
	}
	
	public void setTo(NodeInfo to) {
		this.to = to;
	}
	
	public int getBandwidth() {
		return bandwidth;
	}
	
	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}
	
	public int getDelay() {
		return delay;
	}
	
	public void setDelay(int delay) {
		this.delay = delay;
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
		EdgeInfo other = (EdgeInfo) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("id:%d\tfrom:%d\tto:%d\tlatency:%.2f\tbandwidth:%.2f",
				             id, from.getId(), to.getId(), delay, bandwidth);
	}
}