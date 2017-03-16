package de.tum.msrg.message;

import de.tum.msrg.topology.NodeInfo;

import java.util.ArrayList;
import java.util.List;

public class PubMessage extends Message {

	private Publication pubData;
	private boolean switchToGossip = false;
	private boolean switchToOverlay = false;
	private boolean alreadySwitched = false;
	// whether this pub message was send using a direct link from the publisher
	private boolean directSend = false;

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
		switchToGossip = msg.switchToGossip();
		switchToOverlay = msg.switchToOverlay();
		alreadySwitched = msg.alreadySwitched;
	}
	
	@Override
	public PubMessage clone() {
		return new PubMessage(this);
	}

	public Publication getPublication() {
		return pubData;
	}

	public boolean switchToGossip() {
		return switchToGossip;
	}

	public boolean switchToOverlay() {
		return switchToOverlay;
	}

	public void setSwitchToGossip(boolean switchToGossip) {
		this.switchToGossip = switchToGossip;
	}

	public void setSwitchToOverlay(boolean switchToOverlay) {
		this.switchToOverlay = switchToOverlay;
	}

	public boolean isAlreadySwitched() {
		return alreadySwitched;
	}

	public void setAlreadySwitched(boolean b) {
		alreadySwitched = b;
	}

	public void setDirectSend(boolean b) {
		directSend = b;
	}

	public boolean isDirectSend() {
		return directSend;
	}
}
