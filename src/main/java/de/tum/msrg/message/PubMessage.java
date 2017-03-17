package de.tum.msrg.message;

import de.tum.msrg.topology.NodeInfo;

public class PubMessage extends Message {

	private Publication pubData;

	public PubMessage(NodeInfo fromNode, NodeInfo toNode, Publication pub) {
		super(Type.PUB, fromNode, toNode);
		pubData = pub;
	}

	public PubMessage(NodeInfo fromNode, NodeInfo toNode, Publication pub, int msgId) {
		super(Type.PUB, fromNode, toNode, msgId);
		pubData = pub;
	}

	public PubMessage(PubMessage msg) {
		super(msg);
		pubData = msg.getPublication();
	}
	
	@Override
	public PubMessage clone() {
		return new PubMessage(this);
	}

	public Publication getPublication() {
		return pubData;
	}
}
