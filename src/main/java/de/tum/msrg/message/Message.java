package de.tum.msrg.message;

import de.tum.msrg.topology.NodeInfo;
import jist.runtime.JistAPI;
import jist.runtime.JistAPI.Timeless;

public class Message implements Timeless {
	public enum Type {
		SUB, ADV, PUB;
	}

	public static void resetCounter() {
		msgCount = 0;
	}
	// publication size in bytes
	public static Integer MSG_SIZE = null;
	protected static int msgCount = 0;
	protected int id;
	protected Type type;
	protected NodeInfo fromNode;
	protected NodeInfo toNode;
	protected long latency;
	protected int hopCount;
	protected NodeInfo lastHop;
	
	// this constructor is typically used for constructing a new message
	public Message(Type type, NodeInfo fromNode, NodeInfo toNode) {
		id = msgCount++;
		this.type = type;
		this.fromNode = fromNode;
		this.toNode = toNode;
		latency = 0;
		hopCount = 0;
		lastHop = null;
	}
	
	public Message(Type type, NodeInfo fromNode, NodeInfo toNode, int id) {
		this(type, fromNode, toNode);
		msgCount--; // go back 1 id, since this message does not use the ids generated by msgCount
		this.id = id;
	}
	// this constructor should be used when cloning messages 
	public Message(Message msg) {
		id = msg.getId();
		type = msg.getType();
		fromNode = msg.getFromNode();
		toNode = msg.getToNode();
		hopCount = msg.getHopCount();
		latency = msg.getLatency();
		lastHop = msg.getLastHop();
	}
	
	public Message clone() {
		return new Message(this);
	}
	
	public static int getMsgCount() {
		return msgCount;
	}
	
	public void addLatency(long latency) {
		this.latency += latency;
	}
	
	public void incrementHopCount() {
		hopCount++;
	}
	
	public void switchDestination() {
		NodeInfo tmpDest = toNode;
		toNode = fromNode;
		fromNode = tmpDest;
	}

	public String toString() {
		String outString = String.format("%s ", type);
		outString += String.format("%d @ %08d: ", id, JistAPI.getTime());
		outString += String.format("%05d->", fromNode.getId());
		outString += toNode != null ? String.format("%05d", toNode.getId()) : "null";
		return outString;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Message message = (Message) o;
		if (id != message.id) return false;
		if (type != message.type) return false;
		if (fromNode != null ? !fromNode.equals(message.fromNode) : message.fromNode != null) return false;
		return toNode != null ? toNode.equals(message.toNode) : message.toNode == null;

	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + type.hashCode();
		result = 31 * result + (fromNode != null ? fromNode.hashCode() : 0);
		result = 31 * result + (toNode != null ? toNode.hashCode() : 0);
		return result;
	}

	public int getId() {
		return this.id;
	}
	
	public void setId(final int id) {
		this.id = id;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public void setType(final Type type) {
		this.type = type;
	}
	
	public NodeInfo getFromNode() {
		return this.fromNode;
	}
	
	public void setFromNode(final NodeInfo fromNode) {
		this.fromNode = fromNode;
	}
	
	public NodeInfo getToNode() {
		return this.toNode;
	}
	
	public void setToNode(final NodeInfo toNode) {
		this.toNode = toNode;
	}
	
	public long getLatency() {
		return this.latency;
	}
	
	public int getHopCount() {
		return this.hopCount;
	}

	public void setHopCount(int hopCount) {
		this.hopCount = hopCount;
	}

	public NodeInfo getLastHop() {
		return this.lastHop;
	}
	
	public void setLastHop(final NodeInfo lastHop) {
		this.lastHop = lastHop;
	}
}