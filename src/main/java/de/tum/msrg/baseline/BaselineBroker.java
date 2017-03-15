package de.tum.msrg.baseline;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.tum.msrg.config.Configuration;
import de.tum.msrg.message.PubMessage;
import de.tum.msrg.message.Publication;
import de.tum.msrg.sim.StatsCollector;
import de.tum.msrg.utils.SimLogger;
import jist.runtime.JistAPI;
import de.tum.msrg.message.Message;
import de.tum.msrg.overlay.*;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.underlay.Node;

/**
 * normal publish/subscribe broker with no fault-tolerance mechanism
 */
public class BaselineBroker extends BrokerBase {
	public class BaselineBrokerSimEntity implements BrokerBaseSimInterface {
		private BrokerBase broker;
		private BrokerBaseSimInterface nodeProxy;
		private BrokerBasicOperations brokerOps;

		public BaselineBrokerSimEntity(BrokerBase node) {
			broker = node;
			nodeProxy = (BrokerBaseSimInterface) JistAPI.proxy(this, BrokerBaseSimInterface.class);
			brokerOps = new BrokerBasicOperations(broker, nodeProxy, overlay);
		}

		public BrokerBaseSimInterface getProxy() {
			return nodeProxy;
		}

		@Override
		public void startPublishing() {
			brokerOps.doPublish();
		}

		@Override
		public void sendMessage(Message msg) {
			if(msg.getType() == Message.Type.PUB) {
				BrokerBase nextHop = overlay.getBroker(msg.getToNode());
				// update util ratio average stats
				int matchingSubscriptions = 0, matchingSubscribers = 0;
				long totalPathLen = 0;
				Publication pub = ((PubMessage)msg).getPublication();
				for(NodeInfo subscriber: pub.getSubscribers()) {
					// for subs which are not delivered and are reachable via this msg's next hop
					if (!pub.getDeliveredSubs().contains(subscriber)) {
						Set<BrokerBase> path = overlay.getPathSteps(getId(), subscriber.getId());
						if (path.contains(nextHop)) {
							// find number of matches on the subscriber broker, can be more than 1
							BrokerBase subBroker = overlay.getBroker(subscriber);
							++matchingSubscribers;
							matchingSubscriptions += subBroker.getMatchingSubscriptionCount(pub);
							int pathLen = path.size();
							// number of hops is one less than number of nodes on the path
							// (includung source and dest)
							if (pathLen > 0)
								--pathLen;
							totalPathLen += pathLen;
						}
					}
				}
				if (matchingSubscriptions > 0)
					StatsCollector.getInstance().addUtilMetric(
							getId(), matchingSubscriptions, (double)totalPathLen / matchingSubscribers);
			}
			brokerOps.doSendMessage(msg);
		}

		@Override
		public void receiveMessage(Message msg) {
			processMessage(msg);
			brokerOps.doReceiveMessage(msg);
		}

		@Override
		public void processMessage(Message msg) {
			if(!applyLoadDelay)
				return;
			// use an input capacity similar to output capacity
			// ...
		}

		@Override
		public void bypassFailureAndSendMessage(Message msg) {
			// a normal pub/sub implementation has no fault
			// tolerance measure. Therefore, the backup dissemination
			// will simply drop the message
		}
	}
	
	public BaselineBroker(NodeInfo nodeInfo, Node physicalNode, OverlayBase<BaselineBroker> overlay, Configuration config) {
		this.nodeInfo = nodeInfo;
		this.id = nodeInfo.getId();
		underlayNode = physicalNode;
		this.overlay = overlay;
		overlayLinks = new ArrayList<OverlayLink>();
		overlayRoutes = new BaselineBroker[overlay.getNoOfNodes()];
		router = new Router(this, overlay);
		this.applyLoadDelay = config.getBooleanConfig(ConfigKeys.SIM_APPLY_LOAD_DELAY);
		this.pubSubLoad = new PubSubNodeWload(nodeInfo);
		nodeSim = (new BaselineBrokerSimEntity(this)).getProxy();
		brokerOutput = new BrokerOutput(this, nodeInfo.getThroughput());
	}
	
	protected BaselineBroker() {
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (getClass() != obj.getClass()) return false;
		BaselineBroker other = (BaselineBroker)obj;
		if (getId() != other.getId()) return false;
		return true;
	}
}
