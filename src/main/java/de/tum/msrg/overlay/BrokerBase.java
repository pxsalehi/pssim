package de.tum.msrg.overlay;

import de.tum.msrg.message.Subscription;
import de.tum.msrg.pubsub.Failable;
import jist.runtime.JistAPI;
import de.tum.msrg.message.Message;
import de.tum.msrg.message.Publication;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.underlay.Node;
import de.tum.msrg.utils.Range;
import de.tum.msrg.utils.SimLogger;

import java.io.PrintStream;
import java.util.*;

public class BrokerBase implements Failable{
	/**
	 * This interface encapsulates entity calls which are synchronized
	 * by the simulation framework
	 */
	public interface BrokerBaseSimInterface extends JistAPI.Proxiable {
		void startPublishing();
		void sendMessage(Message msg);
		void receiveMessage(Message msg);
		void processMessage(Message msg);
		// the backup send mechanism used to bypass failure
		void bypassFailureAndSendMessage(Message msg);
	}

	public static boolean applyLoadDelay = false;
	protected int id;
	protected NodeInfo nodeInfo;
	// the simulation entity of th broker
	protected BrokerBaseSimInterface nodeSim;
	// the underlay node of this broker
	protected Node underlayNode;
	protected Router router;
	protected OverlayBase<? extends BrokerBase> overlay;
	protected List<OverlayLink> overlayLinks;
	// overlay routing table: overlayRoutes[dest] gives nextHop
	protected BrokerBase[] overlayRoutes;
	protected List<BrokerBase> neighbors = null;
	// time ranges where the broker is down
	protected List<Range> failTrace = new ArrayList<Range>();
	// advs, subs and pubs of this broker
	protected PubSubNodeWload pubSubLoad;
	// broker output represents broker's throughput
	protected BrokerOutput brokerOutput;
	// counters, etc
	private long messagesReceived = 0;
	private long messagesDelivered = 0;
	private float totalDeliveryLatency = 0;
	private long messagesDeliveredToSelf = 0;
	private long messagesSent = 0;
	private long messagesPublished = 0;
	private long messagesDropped = 0;

	protected Set<Integer> seenPubIds = new HashSet<Integer>();
	
	public BrokerBase(NodeInfo nodeInfo, Node physicalNode, OverlayBase<BrokerBase> overlay, boolean applyLoadDelay) {
		this.nodeInfo = nodeInfo;
		this.id = nodeInfo.getId();
		underlayNode = physicalNode;
		this.overlay = overlay;
		overlayLinks = new ArrayList<OverlayLink>();
		overlayRoutes = new BrokerBase[overlay.getNoOfNodes()];
		router = new Router(this, overlay);
		this.applyLoadDelay = applyLoadDelay;
		this.pubSubLoad = new PubSubNodeWload(nodeInfo);
		brokerOutput = new BrokerOutput(this, nodeInfo.getThroughput());
	}

	protected BrokerBase() {}

	public void addOverlayLink(OverlayLink link) {
		if(!overlayLinks.contains(link))
			overlayLinks.add(link);
	}

	public OverlayLink getLinkTo(BrokerBase oNode) {
		for (OverlayLink oLink : overlayLinks) {
			if (oLink.isToLink(oNode)) {
				return oLink;
			}
		}
		return null;
	}

	public void addOverlayRoute(BrokerBase nextNode, BrokerBase targetNode) {
		overlayRoutes[targetNode.getId()] = nextNode;
	}

	public BrokerBase getNextNodeToRoute(BrokerBase targetNode) {
		return overlayRoutes[targetNode.getId()];
	}

	public BrokerBase getNextNodeToRoute(int targetId) {
		return overlayRoutes[targetId];
	}

	public float getLatency(BrokerBase oNode) {
		OverlayLink oLink = getLinkTo(oNode);
		return (oLink == null) ? -1 : oLink.getLatency();
	}

	public List<BrokerBase> getNeighbors() {
		if(neighbors == null) {
			neighbors = new ArrayList<BrokerBase>();
			for (OverlayLink oLink : overlayLinks)
				if (oLink.getFromNode() == this) {
					neighbors.add(oLink.getToNode());
				} else {
					neighbors.add(oLink.getFromNode());
				}
		}
		return neighbors;
	}

	public void restart() {
		SimLogger.info("#Restarting broker=" + getId() + " time=" + JistAPI.getTime());
		SimLogger.info("#FinishedRestarting broker=" + getId() + " time=" + JistAPI.getTime());
	}

	public void addFailTrace(long start, long end) {
		Range r = new Range(start, end);
		failTrace.add(r);
		// schedule a call to re-initiate the broker state at the end of each failure
		JistAPI.runAt(new Runnable(){
			@Override
			public void run() {
                // since scheduled fails may overlap, first check that broker is not down then restart
				if(!isDown())
				    restart();
			}
		}, end + 1);
		SimLogger.info("#ScheduleRestart broker=" + getId() + " restart_time=" + (end + 1) +
						" time=" + JistAPI.getTime());
	}

	// if this node is currently down from the broker 'from'
	public boolean isDown(BrokerBase from) {
		return checkIsDown(from, JistAPI.getTime());
	}

	// if this node is down at the current time of simulation
	public boolean isDown() {
		return checkIsDown(JistAPI.getTime());
	}

	// if there is a down time overlapping with the given period
	public boolean isDown(Range period) {
		for (Range r : failTrace)
			if (r.doesOverlap(period)) return true;
		return false;
	}

	public boolean checkIsDown(BrokerBase from, long currTime) {
		Range commDuration = new Range(currTime, currTime);
		if (!from.equals(this)) {
			float linkLatency = getLatency(from);
			if(linkLatency == -1)
				return isDown();
//				throw new RuntimeException(String.format(
//						"Brokers %d and %d are not neighbours in the overlay!",
//						getId(), from.getId()));
			commDuration.end = currTime + linkLatency;
		}
		return isDown(commDuration);
	}

	public boolean checkIsDown(long currTime) {
		for (Range r : failTrace)
			if (r.isCovered(currTime)) return true;
		return false;
	}

	// matching info must be present in pub
	public int getMatchingSubscriptionCount(Publication pub) {
		int match = 0;
		for (Subscription sub: pubSubLoad.getSubs())
			if (pub.getMatchingSubscriptions().contains(sub.getID()))
				match++;
		return match;
	}

	public boolean equals(BrokerBase anotherNode) {
		return id == anotherNode.getId();
	}

	public String toString() {
		return "Broker" + getId();
	}

	public void printRoutingTable(PrintStream outStream) {
		for (int i = 0; i < overlayRoutes.length; i++) {
			if (overlayRoutes[i] != null) {
				outStream.printf("[%03d]->[%03d]\n", i, overlayRoutes[i].getId());
			} else {
				outStream.printf("[%03d]->[null]\n", i);
			}
		}
	}

	// for debugging purposes
	public void printOverlayLinks() {
		for (OverlayLink oLink : overlayLinks) {
			System.out.printf("%d -> %d, %f ",
					oLink.getFromNode().getId(), oLink.getToNode().getId(), oLink.getLatency());
			System.out.println();
		}
	}

	public void printAllDetails() {
		System.out.println("-----------BEGIN----------");
		System.out.printf("id: %d; underlay: %d, ", id, underlayNode.getID());
		System.out.println();
		System.out.println("Routing table");
		printRoutingTable(System.out);
		System.out.println("Overlay Links");
		printOverlayLinks();
		System.out.println();
		pubSubLoad.printAllDetails();
		System.out.println("Messages received: " + messagesReceived);
		System.out.println("Messages matched: " + messagesDelivered);
		System.out.println("---------END---------");
	}

	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof BrokerBase)) return false;
		BrokerBase other = (BrokerBase)obj;
		if (getId() != other.getId()) return false;
		return true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Range> getFailTrace() {
		return failTrace;
	}

	public void setFailTrace(List<Range> failTrace) {
		this.failTrace = failTrace;
	}

	public NodeInfo getNodeInfo() {
		return nodeInfo;
	}

	public void setNodeInfo(NodeInfo nodeInfo) {
		this.nodeInfo = nodeInfo;
	}

	public BrokerBaseSimInterface getNodeSim() {
		return nodeSim;
	}

	public void setNodeSim(BrokerBaseSimInterface nodeSim) {
		this.nodeSim = nodeSim;
	}

	public Node getUnderlayNode() {
		return underlayNode;
	}

	public void setUnderlayNode(Node underlayNode) {
		this.underlayNode = underlayNode;
	}

	public PubSubNodeWload getPubSubLoad() {
		return pubSubLoad;
	}

	public void setPubSubLoad(PubSubNodeWload pubSubLoad) {
		this.pubSubLoad = pubSubLoad;
	}

	public List<OverlayLink> getOverlayLinks() {
		return overlayLinks;
	}

	public void setOverlayLinks(List<OverlayLink> overlayLinks) {
		this.overlayLinks = overlayLinks;
	}

	public BrokerBase[] getOverlayRoutes() {
		return overlayRoutes;
	}

	public void setOverlayRoutes(BrokerBase[] overlayRoutes) {
		this.overlayRoutes = overlayRoutes;
	}

	public Router getRouter() {
		return router;
	}

	public boolean addSeenPubId(Publication pub) {
		return seenPubIds.add(pub.getId());
	}

	public Set<Integer> getSeenPubIds() {
		return seenPubIds;
	}

	public void incrementMessagesDelivered() {
		++messagesDelivered;
	}

	public void updateBrokerTotalDeliveryLatency(float latency) {
		totalDeliveryLatency += latency;
	}

	public void incrementMessagesSent() {
		++messagesSent;
	}

	public void incrementMessagesReceived() {
		messagesReceived++;
	}

	public void incrementMessagesPublished() {
		messagesPublished++;
	}

	public void incrementMessagesDropped() {
		messagesDropped++;
	}

	public void incrementMessagesDeliveredToSelf() {
		messagesDeliveredToSelf++;
	}

	public long getMessagesSent() {
		return messagesSent;
	}

	public long getMessagesDropped() {
		return messagesDropped;
	}

	public long getMessagesReceived() {
		return messagesReceived;
	}

	public long getMessagesDelivered() {
		return messagesDelivered;
	}

	public float getTotalDeliveryLatency() {
		return totalDeliveryLatency;
	}

	public long getMessagesPublished() {
		return messagesPublished;
	}

	public long getMessagesDeliveredToSelf() {
		return messagesDeliveredToSelf;
	}

	public double getAverageDeliveryLatency() {
		return messagesDelivered == 0 ? 0 : (totalDeliveryLatency / messagesDelivered);
	}

	public OverlayBase<? extends BrokerBase> getOverlay() {
		return overlay;
	}

	public BrokerOutput getBrokerOutput() {
		return brokerOutput;
	}
}