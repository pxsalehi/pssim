package de.tum.msrg.baseline;

import de.tum.msrg.AllTests;
import de.tum.msrg.config.ConfigParserException;
import de.tum.msrg.config.Configuration;
import de.tum.msrg.message.Advertisement;
import de.tum.msrg.message.Publication;
import de.tum.msrg.message.Subscription;
import de.tum.msrg.overlay.PubSubNodeWload;
import de.tum.msrg.overlay.RuntimeSimException;
import de.tum.msrg.resources.topologies.Topologies;
import de.tum.msrg.sim.StatsCollector;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.topology.Topology;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TestBaselineWithLinkCongestion {
    private Configuration config;
    private Topology topology;
    private BaselineOverlay overlay;
    BaselineBroker b0, b1, b2, b3, b4, b5;
    int seed;

//    @Before
//    public void setUp() throws RuntimeSimException, ConfigParserException, IOException {
//        seed = 8543546;
//        config = new Configuration(new FileReader(Topologies.T01_DIR_PATH + "sim.config"));
//        config.setProperty(ConfigKeys.SIM_APPLY_LINK_CONGESTION, true);
//        Configuration.JistIsRunning = false;
//        topology = new Topology(new FileReader(Topologies.T01_DIR_PATH + "topology.txt"));
//        overlay = new BaselineOverlay(config, topology, seed);
//        overlay.initOverlay();
//        b0 = overlay.getBroker(0); b1 = overlay.getBroker(1); b2 = overlay.getBroker(2);
//        b3 = overlay.getBroker(3); b4 = overlay.getBroker(4); b5 = overlay.getBroker(5);
//        b0.setPubSubLoad(new PubSubNodeWload(b0.getNodeInfo()));
//        b1.setPubSubLoad(new PubSubNodeWload(b1.getNodeInfo()));
//        b2.setPubSubLoad(new PubSubNodeWload(b2.getNodeInfo()));
//        b3.setPubSubLoad(new PubSubNodeWload(b3.getNodeInfo()));
//        b4.setPubSubLoad(new PubSubNodeWload(b4.getNodeInfo()));
//        b5.setPubSubLoad(new PubSubNodeWload(b5.getNodeInfo()));
//    }

    @After
    public void tearDown() {
        StatsCollector.reset();
        Advertisement.resetCounter();
        Subscription.resetCounter();
        Publication.resetCounter();
    }

    @Test
    public void testOverlayLinks() {
        assertEquals(b2.getOverlayLinks().size(), 3);
        assertEquals(b0.getOverlayLinks().size(), 2);
        assertEquals(b1.getOverlayLinks().size(), 2);
        assertEquals(b3.getOverlayLinks().size(), 1);
        assertEquals(b4.getOverlayLinks().size(), 1);
        assertEquals(b5.getOverlayLinks().size(), 1);
    }

    @Test @Ignore
    public void testLinkCongestionT01PublishRateHigherThanLinkBW() {
        // pub should go b0->b2->b4
        // check publishing from b0 to b4
        // create a pub
        Publication p0 = new Publication(0, new float[]{2.3f, 100f});
        p0.setSourceBroker(b0.getNodeInfo());
        // add subscriber
        List<NodeInfo> subscribers = new ArrayList<NodeInfo>();
        subscribers.add(b4.getNodeInfo());
        p0.setSubscribers(subscribers);
        Publication p1 = p0.clonePub(), p2 = p0.clonePub(), p3 = p0.clonePub(), p4 = p0.clonePub(), p5 = p0.clonePub();
        // load pub
        b0.getPubSubLoad().addPub(p0);
        b0.getPubSubLoad().addPub(p1);
        b0.getPubSubLoad().addPub(p2);
        b0.getPubSubLoad().addPub(p3);
        b0.getPubSubLoad().addPub(p4);
        b0.getPubSubLoad().addPub(p5);
        b0.getPubSubLoad().setPubInterval(250);
        // start sim
        overlay.getSimInterface().startSim();
        // check counters for messages
        // published
        assertEquals(b0.getMessagesPublished(), 6);
        assertEquals(b1.getMessagesPublished(), 0);
        assertEquals(b2.getMessagesPublished(), 0);
        assertEquals(b3.getMessagesPublished(), 0);
        assertEquals(b4.getMessagesPublished(), 0);
        assertEquals(b5.getMessagesPublished(), 0);
        // sent
        assertEquals(b0.getMessagesSent(), 6);
        assertEquals(b1.getMessagesSent(), 0);
        assertEquals(b2.getMessagesSent(), 6);
        assertEquals(b3.getMessagesSent(), 0);
        assertEquals(b4.getMessagesSent(), 0);
        assertEquals(b5.getMessagesSent(), 0);
        // received
        assertEquals(b0.getMessagesReceived(), 0);
        assertEquals(b1.getMessagesReceived(), 0);
        assertEquals(b2.getMessagesReceived(), 6);
        assertEquals(b3.getMessagesReceived(), 0);
        assertEquals(b4.getMessagesReceived(), 6);
        assertEquals(b5.getMessagesReceived(), 0);
        // matched
        assertEquals(b0.getMessagesDelivered(), 0);
        assertEquals(b1.getMessagesDelivered(), 0);
        assertEquals(b2.getMessagesDelivered(), 0);
        assertEquals(b3.getMessagesDelivered(), 0);
        assertEquals(b4.getMessagesDelivered(), 6);
        assertEquals(b5.getMessagesDelivered(), 0);
        // check seen publications
        assertTrue(CollectionUtils.isEqualCollection(b0.getSeenPubIds(), Arrays.asList(
                p0.getId(), p1.getId(), p2.getId(), p3.getId(), p4.getId(), p5.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b2.getSeenPubIds(), Arrays.asList(
                p0.getId(), p1.getId(), p2.getId(), p3.getId(), p4.getId(), p5.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b4.getSeenPubIds(), Arrays.asList(
                p0.getId(), p1.getId(), p2.getId(), p3.getId(), p4.getId(), p5.getId())));
        assertEquals(b1.getSeenPubIds().size(), 0);
        assertEquals(b3.getSeenPubIds().size(), 0);
        assertEquals(b5.getSeenPubIds().size(), 0);
        // check statsCollector
        assertEquals(StatsCollector.getInstance().getTotalDeliveredPubCount(), 6);
        assertEquals(StatsCollector.getInstance().getTotalPublishedPubCount(), 6);
        assertEquals(StatsCollector.getInstance().getTotalSentPubCount(), 12);
        assertEquals(StatsCollector.getInstance().getAverageLatency(), 73, AllTests.ASSERT_DELTA);
        assertEquals(StatsCollector.getInstance().getAverageHopCount(), 2, AllTests.ASSERT_DELTA);
    }

    @Test @Ignore
    public void testLinkCongestionPerDirection() {
    }
}
