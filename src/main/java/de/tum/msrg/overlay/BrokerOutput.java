package de.tum.msrg.overlay;

import de.tum.msrg.message.BatchPubMessage;
import de.tum.msrg.message.Message;
import de.tum.msrg.baseline.BatchFactor;
import de.tum.msrg.sim.StatsCollector;
import de.tum.msrg.utils.SimLogger;
import jist.runtime.JistAPI;

/**
 * Created by pxsalehi on 16.01.17.
 *
 * Each broker has a brokerOutput entity which simulates the throughput of the broker in terms
 * of number of packets it can send per second
 */
public class BrokerOutput {
    public interface BrokerOutputSimInterface extends JistAPI.Proxiable {
        void deliverMsg(Message msg, BrokerBase dest, int latency);
    }

    public static boolean applyLoadDelay = false;
    private long throughput;  // single basic message (packet) per second
    private BrokerBase thisBroker;
    private BrokerOutputSimInterface brokerOutputSim;
    // maximum packet size in bytes
    public static final double MTU = 1500;
    private boolean overloaded = false;

    public class BrokerOutputSimEntity implements BrokerOutputSimInterface {
        private BrokerOutputSimInterface brokerOutputProxy;
        // number of msgs currently being transmitted or queued to be sent
        private Integer msgsQueuedToProcess = 0;

        public BrokerOutputSimEntity() {
            brokerOutputProxy = (BrokerOutputSimInterface)JistAPI.proxy(this, BrokerOutputSimInterface.class);
        }

        @Override
        public void deliverMsg(Message msg, BrokerBase dest, int latency) {
            int msgSize = calculateOutMsgs(msg);
            int outputDelay = calculateOutputDelay(msgSize, "Message" + msg.getId());
            long totalDelay = latency + outputDelay;
            StatsCollector.getInstance().updatePacketSentCount(msgSize, thisBroker.getId());
            if (!thisBroker.getPubSubLoad().getAdvs().isEmpty())  // a publisher
                StatsCollector.getInstance().updatePacketSentCountPerPublisher(msgSize, thisBroker.getId());
            BrokerBasicOperations.sleep(totalDelay);
            msg.addLatency(totalDelay);
            dest.getNodeSim().receiveMessage(msg);
        }

        private int calculateOutputDelay(final int msgsToProcess, String msgID) {
            if (!applyLoadDelay)
                return 0;
            long curTime = JistAPI.getTime();
            float overloadProcessDelay = 0;
            double processTime = 0;
            synchronized (this.msgsQueuedToProcess) {
                this.msgsQueuedToProcess += msgsToProcess;
                if (applyLoadDelay && this.msgsQueuedToProcess > throughput) {
                    if (this.msgsQueuedToProcess > 1.0 * throughput)  // 10% above capacity
                        overloaded = true;
                    StatsCollector.getInstance().addQueueSize(thisBroker.getId(), (int) (this.msgsQueuedToProcess - throughput));
                    // overload delay is the queue of packets to sent and s irrelevant of
                    // processing throughput of p/s system
                    overloadProcessDelay = (this.msgsQueuedToProcess - throughput) * (1000 / throughput);
                    if (SimLogger.isDebugEnabled()) // && overloadDelay > 0)
                        SimLogger.debug(String.format("#BrokerOutOverloaded %s. " +
                                        "Queue size=%.2f Overload delay=%.2f msgID=%s",
                                thisBroker.toString(), (this.msgsQueuedToProcess - throughput), overloadProcessDelay, msgID));
                } else {
                    overloaded = false;
                }
                // process time is irrelevant of msg size, just the matching and queueing for send
                processTime = msgsToProcess * (1000 / throughput);
                JistAPI.runAt(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (msgsQueuedToProcess) {
                            msgsQueuedToProcess -= msgsToProcess;
                        }
                    }
                }, (long) (curTime + overloadProcessDelay + processTime));
            }
            return (int) (overloadProcessDelay + processTime);
        }

        public BrokerOutputSimInterface getProxy() {
            return brokerOutputProxy;
        }
    }

    public BrokerOutput(BrokerBase thisBroker, long throughput) {
        this.thisBroker = thisBroker;
        this.throughput = throughput;
        brokerOutputSim = (new BrokerOutputSimEntity()).getProxy();

    }

    // calculate number of output msgs (packets required to send the message
    public static int calculateOutMsgs(Message msg) {
        int msgPerPub = (int) Math.ceil(Message.MSG_SIZE / MTU);
        if(msg instanceof BatchPubMessage) {
            int batchSize = ((BatchPubMessage)msg).getBatchSize();
            return (int) (/*msgPerPub * */batchSize * BatchFactor.getInstance().getBatchFactor(batchSize));
        } else {
            return 1; //msgPerPub;
        }
    }

    public BrokerOutputSimInterface getBrokerOutputSim() {
        return brokerOutputSim;
    }

    public BrokerBase getThisBroker() {
        return thisBroker;
    }

    public long getThroughput() {
        return throughput;
    }

    public boolean isOverloaded() {
        return overloaded;
    }
}
