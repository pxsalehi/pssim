package de.tum.msrg.overlay;

import de.tum.msrg.config.Configuration;
import jist.runtime.JistAPI;
import de.tum.msrg.message.Message;
import de.tum.msrg.message.PubMessage;
import de.tum.msrg.message.Publication;
import de.tum.msrg.sim.StatsCollector;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.utils.SimLogger;

import java.util.List;
import java.util.Set;

/**
 * Created by pxsalehi on 22.12.15.
 */
public class BrokerBasicOperations {
    private int SHOW_PROGRESS_INTERVAL = 100000;
    private long lastProgressMark = 0;
    private BrokerBase broker;
    private BrokerBase.BrokerBaseSimInterface nodeProxy;
    private OverlayBase<? extends BrokerBase> overlay;

    public BrokerBasicOperations(BrokerBase broker, BrokerBase.BrokerBaseSimInterface nodeProxy,
                                 OverlayBase<? extends BrokerBase> overlay) {
        this.broker = broker;
        this.nodeProxy = nodeProxy;
        this.overlay = overlay;
    }

    public void doPublish() {
        if(broker.getPubSubLoad() == null)
            return;
        if (broker.getPubSubLoad().getPubs() != null && !broker.getPubSubLoad().getPubs().isEmpty())
            SimLogger.info(String.format("#StartPublishing broker=%d t=%d", broker.getId(), JistAPI.getTime()));
        if (!broker.getFailTrace().isEmpty()) {
            SimLogger.warn(String.format("Broker %d will fail %d time(s).",
                    broker.getId(), broker.getFailTrace().size()));
            SimLogger.debug(String.format("Broker %d's down times: %s", broker.getId(), broker.getFailTrace()));
        }
        for (Publication pub : broker.getPubSubLoad().getPubs()) {
            SimLogger.info(String.format("Broker %d publishing", broker.getId()));
            long time = JistAPI.getTime();
            pub.setPublicationTime(time);
            if (broker.isDown()) {
                // node is down, do not publish, go to next publish time
                if (SimLogger.isInfoEnabled())
                    SimLogger.info(String.format("Broker %d is down at t=%d and cannot publish.",
                            broker.getId(), JistAPI.getTime()));
                sleep(1000 / broker.getPubSubLoad().getPubRate());
                continue;
            }
            PubMessage pubMsg = new PubMessage(broker.getNodeInfo(), null, pub);
            if (pub.matches(broker) && !pub.isDeliveredTo(broker)) {
                if (SimLogger.isInfoEnabled())
                    SimLogger.info(String.format("#ReceivePublicationFromSelf pid=%d broker=%d t=%d",
                            pub.getId(), broker.getId(), JistAPI.getTime()));
                StatsCollector.getInstance().pubDeliveredViaTree(broker.getId(), pubMsg, time);
                StatsCollector.getInstance().incrementTotalSelfDeliveredPubCount();
                broker.incrementMessagesDelivered();
                broker.incrementMessagesDeliveredToSelf();
                // remove the sub from the pub target meaning there is no routing info in other nodes
                // about the fact that this node published something that was locally interested in itself!
                pub.updateDeliveredSubs(broker);
            }
            List<Message> routeMsgs = broker.getRouter().route(pubMsg);
            for (Message msg : routeMsgs) {
                if (SimLogger.isInfoEnabled())
                    SimLogger.info(String.format("#SendingPublication msg.id=%d broker=%d t=%d",
                            msg.getId(), broker.getId(), JistAPI.getTime()));
                nodeProxy.sendMessage(msg);
            }
            broker.addSeenPubId(pub);
            broker.incrementMessagesPublished();
            StatsCollector.getInstance().pubPublished(broker.getId(), pub, time);
            sleep(1000 / broker.getPubSubLoad().getPubRate());
        }
    }

    public void doSendMessage(Message msg) {
        BrokerBase toNode = overlay.getBroker(msg.getToNode().getId());
        long time = JistAPI.getTime();
        // print progress
        if (time - lastProgressMark > SHOW_PROGRESS_INTERVAL  && broker.getId() % 50 == 0) {
            lastProgressMark = time;
            System.out.println("sim_time = " + lastProgressMark);
        }
        if (broker.isDown())
            return;
        if (toNode.isDown(broker)) {
            SimLogger.error("#TargetBrokerDown  t=" + JistAPI.getTime());
            nodeProxy.bypassFailureAndSendMessage(msg);
        } else { // target node is not down, go on with tree
            OverlayLink link = broker.getLinkTo(toNode);
            broker.getBrokerOutput().getBrokerOutputSim().deliverMsg(msg, toNode, link.getLatency());
            StatsCollector.getInstance().pubSent(broker.getId(), ((PubMessage)msg).getPublication(), time);
            if (SimLogger.isDebugEnabled()) {
                SimLogger.debug("#SendingMessage broker=" + broker.getId() + " t=" + JistAPI.getTime() +
                        " pid=" + msg.getId() + " to=" + msg.getToNode() + " latency=" +
                        (msg.getLatency() - msg.getCalculatedDelay()) + " hopcount=" + msg.getHopCount());
            }
            broker.incrementMessagesSent();
        }
    }

    public void doReceiveMessage(Message msg) {
        if (broker.isDown())
            return;
        switch (msg.getType()) {
            case PUB:
                try {
                    doReceivePubMessage((PubMessage)msg);
                } catch (RuntimeSimException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                break;
            default:
                throw new RuntimeException("receiveMessage() only supports publication!");
        }
    }

    public void doReceivePubMessage(PubMessage msg) throws RuntimeSimException {
        broker.incrementMessagesReceived();
        if (broker.addSeenPubId(msg.getPublication()) == false) {
            broker.incrementMessagesDropped();
            return; // was not inserted, already received
        }
        List<Message> routeMsgs;
        msg.incrementHopCount();
        routeMsgs = broker.getRouter().route(msg);
        Publication pub = msg.getPublication();
        if (pub.matches(broker) && !pub.isDeliveredTo(broker)) {
            broker.incrementMessagesDelivered();
            broker.updateBrokerTotalDeliveryLatency(msg.getLatency());
            if (SimLogger.isInfoEnabled())
                SimLogger.info(String.format("#ReceivePublication pid=%d broker=%d t=%d",
                        msg.getId(), broker.getId(), JistAPI.getTime()));
            StatsCollector.getInstance().pubDeliveredViaTree(broker.getId(), msg, JistAPI.getTime());
            pub.updateDeliveredSubs(broker);
        } else {
            StatsCollector.getInstance().incrementTotalPureForwardCount();
        }
        for (Message rMsg : routeMsgs)
            nodeProxy.sendMessage(rMsg);
    }

    // sleep time milliseconds
    public static void sleep(long ms) {
        if(Configuration.JistIsRunning)
            JistAPI.sleep(ms);
        else
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
    }
}
