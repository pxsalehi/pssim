package de.tum.msrg.overlay;

import jist.runtime.JistAPI;
import de.tum.msrg.message.Message;
import de.tum.msrg.message.PubMessage;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.utils.SimLogger;

import java.util.*;

public class Router<BrokerType extends BrokerBase> {

	protected BrokerType thisNode;
	public static boolean isAdvBased;
	protected OverlayBase<BrokerType> overlay;

	public Router(BrokerType node, OverlayBase<BrokerType> overlay) {
		thisNode = node;
		this.overlay = overlay;
	}
	
	public void initialize() {
	}

	public List<Message> routePub(Message msg) {
		PubMessage pmsg = (PubMessage)msg;
		List<Message> forwardMsgs = new ArrayList<Message>();
		List<BrokerType> subscribers = new ArrayList<BrokerType>();
		Set<NodeInfo> subscriberIDs;
		Set<BrokerBase> toNodes;
		BrokerType src;
		subscriberIDs = pmsg.getPublication().getSubscribers();
		for(NodeInfo node: subscriberIDs)
			subscribers.add(overlay.getBroker(node.getId()));
		NodeInfo publisher = pmsg.getPublication().getSourceBroker();
		src = overlay.getBroker(publisher.getId());
		toNodes = new HashSet<BrokerBase>();
		for (BrokerType subNode : subscribers) {
			BrokerBase nextNode = findNextHop(src, subNode, msg.getId());
			if (nextNode != null && !nextNode.equals(pmsg.getFromNode()))
				toNodes.add(nextNode);
		}
		if(SimLogger.isDebugEnabled())
			SimLogger.debug("#RouterResult for msg.id=" + pmsg.getId() + " (src=" + src + ") on broker="
					+ thisNode.getId() + " recipients=" + toNodes + " t=" + JistAPI.getTime());
		for (BrokerBase nextNode : toNodes) {
			if (!nextNode.equals(thisNode)) {
				PubMessage cloneMessage = new PubMessage(pmsg);
				cloneMessage.setFromNode(thisNode.getNodeInfo());
				cloneMessage.setToNode(nextNode.getNodeInfo());
				forwardMsgs.add(cloneMessage);
			}
		}
		return forwardMsgs;
	}

	// calculate next hop after this node to reach from publisher to subscriber
	public BrokerType findNextHop(BrokerType publisher, BrokerType subscriber, int msgID) {
		// if this node is not on route between pub.source_broker and subscriber, drop!
		if(isAdvBased && !thisNode.equals(subscriber)) {
			Set<BrokerBase> steps =  overlay.getPathSteps(publisher, subscriber);
			if(steps == null || !steps.contains(thisNode)) {
				if(SimLogger.isDebugEnabled())
					SimLogger.debug(String.format(
							"#DropPub (src=%d)(no sub info found) msg.id=%d broker=%d t=%d",
							publisher.getId(), msgID, thisNode.getId(), JistAPI.getTime()));
				return null;
			}
		}
		BrokerType nextNode = (BrokerType) thisNode.getNextNodeToRoute(subscriber);
		return nextNode;
	}

	public BrokerBase findNextHop(NodeInfo publisher, NodeInfo subscriber, Message msg) {
		return findNextHop(overlay.getBroker(publisher.getId()), overlay.getBroker(subscriber.getId()), msg.getId());
	}

	public List<Message> route(Message msg) {
		switch (msg.getType()) {
		case PUB:
			return routePub(msg);
		default:
			throw new RuntimeException("Routing non-publication messages is not implemented! :-(");
		}
	}

}
