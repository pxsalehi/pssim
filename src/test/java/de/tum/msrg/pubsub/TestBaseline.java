package de.tum.msrg.pubsub;

import de.tum.msrg.AllTests;
import de.tum.msrg.config.ConfigParserException;
import de.tum.msrg.config.Configuration;
import de.tum.msrg.message.PubMessage;
import de.tum.msrg.message.Publication;
import de.tum.msrg.overlay.BrokerBase;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import de.tum.msrg.overlay.Router;
import de.tum.msrg.overlay.RuntimeSimException;
import de.tum.msrg.overlay.PubSubNodeWload;
import de.tum.msrg.sim.StatsCollector;
import de.tum.msrg.resources.topologies.Topologies;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.topology.Topology;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import de.tum.msrg.utils.Range;

import static org.junit.Assert.*;

public class TestBaseline {
    private Configuration config;
    private Topology topology;
    private PSOverlay overlay;
    PSBroker b0, b1, b2, b3, b4, b5;
    int seed;

    @Before
    public void setUp() throws RuntimeSimException, ConfigParserException, IOException {
        seed = 8543546;
        config = new Configuration(new FileReader(Topologies.T01_DIR_PATH + "sim.config"));
        StatsCollector.reset();
        StatsCollector.getInstance().initialize(config.getIntConfig(ConfigKeys.SIM_NO_OF_NODES),
                                                config.getIntConfig(ConfigKeys.WLOAD_NO_OF_CLASSES));
        topology = new Topology(new FileReader(Topologies.T01_DIR_PATH + "topology.txt"));
        overlay = new PSOverlay(config, topology, seed);
        overlay.initOverlay();
        b0 = overlay.getBroker(0); b1 = overlay.getBroker(1); b2 = overlay.getBroker(2);
        b3 = overlay.getBroker(3); b4 = overlay.getBroker(4); b5 = overlay.getBroker(5);
        b0.setPubSubLoad(new PubSubNodeWload(b0.getNodeInfo()));
        b1.setPubSubLoad(new PubSubNodeWload(b1.getNodeInfo()));
        b2.setPubSubLoad(new PubSubNodeWload(b2.getNodeInfo()));
        b3.setPubSubLoad(new PubSubNodeWload(b3.getNodeInfo()));
        b4.setPubSubLoad(new PubSubNodeWload(b4.getNodeInfo()));
        b5.setPubSubLoad(new PubSubNodeWload(b5.getNodeInfo()));
    }

    @After
    public void tearDown() {
        StatsCollector.reset();
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

    @Test
    public void testOverlayRoutes() throws IOException, ConfigParserException, RuntimeSimException {
        // check overlay routing tables of
        // b0
        assertNull(b0.getNextNodeToRoute(b0));
        assertEquals(b0.getNextNodeToRoute(b1), b1);
        assertEquals(b0.getNextNodeToRoute(b2), b2);
        assertEquals(b0.getNextNodeToRoute(b3), b1);
        assertEquals(b0.getNextNodeToRoute(b4), b2);
        assertEquals(b0.getNextNodeToRoute(b5), b2);
        // b1
        assertEquals(b1.getNextNodeToRoute(b0), b0);
        assertNull(b1.getNextNodeToRoute(b1));
        assertEquals(b1.getNextNodeToRoute(b2), b0);
        assertEquals(b1.getNextNodeToRoute(b3), b3);
        assertEquals(b1.getNextNodeToRoute(b4), b0);
        assertEquals(b1.getNextNodeToRoute(b5), b0);
        // b2
        assertEquals(b2.getNextNodeToRoute(b0), b0);
        assertEquals(b2.getNextNodeToRoute(b1), b0);
        assertNull(b2.getNextNodeToRoute(b2));
        assertEquals(b2.getNextNodeToRoute(b3), b0);
        assertEquals(b2.getNextNodeToRoute(b4), b4);
        assertEquals(b2.getNextNodeToRoute(b5), b5);
        // b3
        assertEquals(b3.getNextNodeToRoute(b0), b1);
        assertEquals(b3.getNextNodeToRoute(b1), b1);
        assertEquals(b3.getNextNodeToRoute(b2), b1);
        assertNull(b3.getNextNodeToRoute(b3));
        assertEquals(b3.getNextNodeToRoute(b4), b1);
        assertEquals(b3.getNextNodeToRoute(b5), b1);
        // b4
        assertEquals(b4.getNextNodeToRoute(b0), b2);
        assertEquals(b4.getNextNodeToRoute(b1), b2);
        assertEquals(b4.getNextNodeToRoute(b2), b2);
        assertEquals(b4.getNextNodeToRoute(b3), b2);
        assertNull(b4.getNextNodeToRoute(b4));
        assertEquals(b4.getNextNodeToRoute(b5), b2);
        // b5
        assertEquals(b5.getNextNodeToRoute(b0), b2);
        assertEquals(b5.getNextNodeToRoute(b1), b2);
        assertEquals(b5.getNextNodeToRoute(b2), b2);
        assertEquals(b5.getNextNodeToRoute(b3), b2);
        assertEquals(b5.getNextNodeToRoute(b4), b2);
        assertNull(b5.getNextNodeToRoute(b5));
        // check route latencies
        assertEquals(b0.getLatency(b1), 52f, AllTests.ASSERT_DELTA);
        assertEquals(b0.getLatency(b4), -1f, AllTests.ASSERT_DELTA); // no direct overlay route between b0 and b4
    }

    @Test
    public void testGetNeighbors() {
        assertTrue(CollectionUtils.isEqualCollection(b2.getNeighbors(), Arrays.asList(b0, b4, b5)));
        assertTrue(CollectionUtils.isEqualCollection(b0.getNeighbors(), Arrays.asList(b1, b2)));
        assertTrue(CollectionUtils.isEqualCollection(b4.getNeighbors(), Arrays.asList(b2)));
    }

    @Test
    public void testFindAllPaths() {
        // after initOverlay all paths must have been found
        assertNotNull(overlay.getAllPaths());
        List<BrokerBase> path = overlay.getPathSteps(b0, b0);
        assertTrue(path.isEmpty());
        path = overlay.getPathSteps(b0, b1);
        assertTrue(CollectionUtils.isEqualCollection(path, Arrays.asList(b0, b1)));
        path = overlay.getPathSteps(b5, b4);
        assertTrue(CollectionUtils.isEqualCollection(path, Arrays.asList(b5, b2, b4)));
        path = overlay.getPathSteps(b5, b3);
        assertTrue(CollectionUtils.isEqualCollection(path, Arrays.asList(b5, b2, b0, b1, b3)));
        path = overlay.getPathSteps(b3, b5);
        assertTrue(CollectionUtils.isEqualCollection(path, Arrays.asList(b3, b1, b0, b2, b5)));
    }

    @Test
    public void testB0Publish() throws RuntimeSimException {
        // pub should go b0->b2->b4
        // check publishing from b0 to b4
        // create a pub
        Publication p0 = new Publication();
        p0.setPubClass(0);
        p0.setAttributes(new float[]{2.3f, 100f});
        p0.setSourceBroker(b0.getNodeInfo());
        // add subscriber
        List<NodeInfo> subscribers = new ArrayList<NodeInfo>();
        subscribers.add(b4.getNodeInfo());
        p0.setSubscribers(subscribers);
        // load pub
        b0.getPubSubLoad().addPub(p0);
        // start sim
        overlay.getSimInterface().startSim();
        // check counters for messages
        // received
        assertEquals(b0.getMessagesReceived(), 0);
        assertEquals(b1.getMessagesReceived(), 0);
        assertEquals(b2.getMessagesReceived(), 1);
        assertEquals(b3.getMessagesReceived(), 0);
        assertEquals(b4.getMessagesReceived(), 1);
        assertEquals(b5.getMessagesReceived(), 0);
        // matched
        assertEquals(b0.getMessagesDelivered(), 0);
        assertEquals(b1.getMessagesDelivered(), 0);
        assertEquals(b2.getMessagesDelivered(), 0);
        assertEquals(b3.getMessagesDelivered(), 0);
        assertEquals(b4.getMessagesDelivered(), 1);
        assertEquals(b5.getMessagesDelivered(), 0);
        // published
        assertEquals(b0.getMessagesPublished(), 1);
        assertEquals(b1.getMessagesPublished(), 0);
        assertEquals(b2.getMessagesPublished(), 0);
        assertEquals(b3.getMessagesPublished(), 0);
        assertEquals(b4.getMessagesPublished(), 0);
        assertEquals(b5.getMessagesPublished(), 0);
        // sent
        assertEquals(b0.getMessagesSent(), 1);
        assertEquals(b1.getMessagesSent(), 0);
        assertEquals(b2.getMessagesSent(), 1);
        assertEquals(b3.getMessagesSent(), 0);
        assertEquals(b4.getMessagesSent(), 0);
        assertEquals(b5.getMessagesSent(), 0);
        // check seen publications
        assertTrue(CollectionUtils.isEqualCollection(b0.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b2.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b4.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertEquals(b1.getSeenPubIds().size(), 0);
        assertEquals(b3.getSeenPubIds().size(), 0);
        assertEquals(b5.getSeenPubIds().size(), 0);
        // check statsCollector
        assertEquals(StatsCollector.getInstance().getTotalDeliveredPubCount(), 1);
        assertEquals(StatsCollector.getInstance().getTotalPublishedPubCount(), 1);
        assertEquals(StatsCollector.getInstance().getTotalSentPubCount(), 2);
        assertEquals(StatsCollector.getInstance().getAverageLatency(), 73, AllTests.ASSERT_DELTA);
        assertEquals(StatsCollector.getInstance().getAverageHopCount(), 2, AllTests.ASSERT_DELTA);
    }

    @Test
    public void testIsDown() throws RuntimeSimException {
        b1.addFailTrace(10, 20);
        b1.addFailTrace(100, 150);
        // check time range
        assertTrue(b1.isDown(new Range(15f, 30f)));
        assertTrue(b1.isDown(new Range(5f, 10f)));
        assertTrue(b1.isDown(new Range(20f, 90f)));
        assertFalse(b1.isDown(new Range(21f, 60f)));
        assertFalse(b1.isDown(new Range(0f, 9.9f)));
        // time point
        assertTrue(b1.checkIsDown(15));
        assertTrue(b1.checkIsDown(10));
        assertTrue(b1.checkIsDown(20));
        assertFalse(b1.checkIsDown(9));
        assertFalse(b1.checkIsDown(21));
        // from another broker
        assertTrue(b1.checkIsDown(b0, 0));
        assertTrue(b1.checkIsDown(b0, 10));
        assertTrue(b1.checkIsDown(b0, 20));
        assertFalse(b1.checkIsDown(b0, 21));
        assertFalse(b1.checkIsDown(b0, 40));
        assertFalse(b1.checkIsDown(b0, 47));
        assertTrue(b1.checkIsDown(b0, 48));
    }

    @Test(expected= RuntimeException.class)
    public void testIsDownNotNeighbours() throws RuntimeSimException {
        b1.checkIsDown(b4, 30);
    }

    @Test
    public void testIsDownWithoutGivingCurrTime() throws RuntimeSimException {
        assertFalse(b1.isDown());
        b1.addFailTrace(0, 20);
        assertTrue(b1.isDown());
        assertTrue(b1.isDown(b0));
    }

    //@Test(expected=RuntimeException.class)
    public void testOverlappingDownTimes() {
        b1.addFailTrace(10, 20);
        b1.addFailTrace(20, 150); // this is ok
        b1.addFailTrace(100, 190);
        assertTrue(b1.checkIsDown(10));
        assertTrue(b1.checkIsDown(20));
        assertTrue(b1.checkIsDown(30));
        assertTrue(b1.checkIsDown(50));
        assertTrue(b1.checkIsDown(60));
        assertTrue(b1.checkIsDown(160));
    }

    @Test
    public void testB0PublishB0IsDown() {
        // pub should go b0->b2->b4
        b0.addFailTrace(0, 100);
        // check publishing from b0 to b4, b0 is down
        // create a pub
        Publication p0 = new Publication();
        p0.setPubClass(0);
        p0.setAttributes(new float[]{2.3f, 100f});
        p0.setSourceBroker(b0.getNodeInfo());
        // add subscriber
        List<NodeInfo> subscribers = new ArrayList<NodeInfo>();
        subscribers.add(b4.getNodeInfo()); subscribers.add(b0.getNodeInfo());
        p0.setSubscribers(subscribers);
        // load pub
        b0.getPubSubLoad().addPub(p0);
        // start sim
        overlay.getSimInterface().startSim();
        // check counters for messages
        // received
        assertEquals(b0.getMessagesReceived(), 0);
        assertEquals(b1.getMessagesReceived(), 0);
        assertEquals(b2.getMessagesReceived(), 0);
        assertEquals(b3.getMessagesReceived(), 0);
        assertEquals(b4.getMessagesReceived(), 0);
        assertEquals(b5.getMessagesReceived(), 0);
        // matched
        assertEquals(b0.getMessagesDelivered(), 0);
        assertEquals(b0.getMessagesDeliveredToSelf(), 0);
        assertEquals(b1.getMessagesDelivered(), 0);
        assertEquals(b2.getMessagesDelivered(), 0);
        assertEquals(b3.getMessagesDelivered(), 0);
        assertEquals(b4.getMessagesDelivered(), 0);
        assertEquals(b5.getMessagesDelivered(), 0);
        // published
        assertEquals(b0.getMessagesPublished(), 0);
        assertEquals(b1.getMessagesPublished(), 0);
        assertEquals(b2.getMessagesPublished(), 0);
        assertEquals(b3.getMessagesPublished(), 0);
        assertEquals(b4.getMessagesPublished(), 0);
        assertEquals(b5.getMessagesPublished(), 0);
        // sent
        assertEquals(b0.getMessagesSent(), 0);
        assertEquals(b1.getMessagesSent(), 0);
        assertEquals(b2.getMessagesSent(), 0);
        assertEquals(b3.getMessagesSent(), 0);
        assertEquals(b4.getMessagesSent(), 0);
        assertEquals(b5.getMessagesSent(), 0);
        // check seen publications
        assertEquals(b0.getSeenPubIds().size(), 0);
        assertEquals(b2.getSeenPubIds().size(), 0);
        assertEquals(b4.getSeenPubIds().size(), 0);
        assertEquals(b1.getSeenPubIds().size(), 0);
        assertEquals(b3.getSeenPubIds().size(), 0);
        assertEquals(b5.getSeenPubIds().size(), 0);
        // check statsCollector
        assertEquals(StatsCollector.getInstance().getTotalDeliveredPubCount(), 0);
        assertEquals(StatsCollector.getInstance().getTotalPublishedPubCount(), 0);
        assertEquals(StatsCollector.getInstance().getTotalSentPubCount(), 0);
    }

    @Test
    public void testB0PublishB2IsDown() {
        // pub should go b0->b2->b4
        b2.addFailTrace(0, 100);
        // check publishing from b0 to b4, b0 is down
        // create a pub
        Publication p0 = new Publication();
        p0.setPubClass(0);
        p0.setAttributes(new float[]{2.3f, 100f});
        p0.setSourceBroker(b0.getNodeInfo());
        // add subscriber
        List<NodeInfo> subscribers = new ArrayList<NodeInfo>();
        subscribers.add(b4.getNodeInfo());
        p0.setSubscribers(subscribers);
        // load pub
        b0.getPubSubLoad().addPub(p0);
        // start sim
        overlay.getSimInterface().startSim();
        // check counters for messages
        // received
        assertEquals(b0.getMessagesReceived(), 0);
        assertEquals(b1.getMessagesReceived(), 0);
        assertEquals(b2.getMessagesReceived(), 0);
        assertEquals(b3.getMessagesReceived(), 0);
        assertEquals(b4.getMessagesReceived(), 0);
        assertEquals(b5.getMessagesReceived(), 0);
        // matched
        assertEquals(b0.getMessagesDelivered(), 0);
        assertEquals(b1.getMessagesDelivered(), 0);
        assertEquals(b2.getMessagesDelivered(), 0);
        assertEquals(b3.getMessagesDelivered(), 0);
        assertEquals(b4.getMessagesDelivered(), 0);
        assertEquals(b5.getMessagesDelivered(), 0);
        // published
        assertEquals(b0.getMessagesPublished(), 1);
        assertEquals(b1.getMessagesPublished(), 0);
        assertEquals(b2.getMessagesPublished(), 0);
        assertEquals(b3.getMessagesPublished(), 0);
        assertEquals(b4.getMessagesPublished(), 0);
        assertEquals(b5.getMessagesPublished(), 0);
        // sent
        assertEquals(b0.getMessagesSent(), 0);
        assertEquals(b1.getMessagesSent(), 0);
        assertEquals(b2.getMessagesSent(), 0);
        assertEquals(b3.getMessagesSent(), 0);
        assertEquals(b4.getMessagesSent(), 0);
        assertEquals(b5.getMessagesSent(), 0);
        // check seen publications
        assertTrue(CollectionUtils.isEqualCollection(b0.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertEquals(b2.getSeenPubIds().size(), 0);
        assertEquals(b4.getSeenPubIds().size(), 0);
        assertEquals(b1.getSeenPubIds().size(), 0);
        assertEquals(b3.getSeenPubIds().size(), 0);
        assertEquals(b5.getSeenPubIds().size(), 0);
        // check statsCollector
        assertEquals(StatsCollector.getInstance().getTotalDeliveredPubCount(), 0);
        assertEquals(StatsCollector.getInstance().getTotalPublishedPubCount(), 1);
        assertEquals(StatsCollector.getInstance().getTotalSentPubCount(), 0);
    }

    @Test
    public void testB0PublishB4IsDown() {
        // pub should go b0->b2->b4
        b4.addFailTrace(0, 100);
        // check publishing from b0 to b4, b0 is down
        // create a pub
        Publication p0 = new Publication();
        p0.setPubClass(0);
        p0.setAttributes(new float[]{2.3f, 100f});
        p0.setSourceBroker(b0.getNodeInfo());
        // add subscriber
        List<NodeInfo> subscribers = new ArrayList<NodeInfo>();
        subscribers.add(b4.getNodeInfo());
        p0.setSubscribers(subscribers);
        // load pub
        b0.getPubSubLoad().addPub(p0);
        // start sim
        overlay.getSimInterface().startSim();
        // check counters for messages
        // received
        assertEquals(b0.getMessagesReceived(), 0);
        assertEquals(b1.getMessagesReceived(), 0);
        assertEquals(b2.getMessagesReceived(), 1);
        assertEquals(b3.getMessagesReceived(), 0);
        assertEquals(b4.getMessagesReceived(), 0);
        assertEquals(b5.getMessagesReceived(), 0);
        // matched
        assertEquals(b0.getMessagesDelivered(), 0);
        assertEquals(b1.getMessagesDelivered(), 0);
        assertEquals(b2.getMessagesDelivered(), 0);
        assertEquals(b3.getMessagesDelivered(), 0);
        assertEquals(b4.getMessagesDelivered(), 0);
        assertEquals(b5.getMessagesDelivered(), 0);
        // published
        assertEquals(b0.getMessagesPublished(), 1);
        assertEquals(b1.getMessagesPublished(), 0);
        assertEquals(b2.getMessagesPublished(), 0);
        assertEquals(b3.getMessagesPublished(), 0);
        assertEquals(b4.getMessagesPublished(), 0);
        assertEquals(b5.getMessagesPublished(), 0);
        // sent
        assertEquals(b0.getMessagesSent(), 1);
        assertEquals(b1.getMessagesSent(), 0);
        assertEquals(b2.getMessagesSent(), 0);
        assertEquals(b3.getMessagesSent(), 0);
        assertEquals(b4.getMessagesSent(), 0);
        assertEquals(b5.getMessagesSent(), 0);
        // check seen publications
        assertTrue(CollectionUtils.isEqualCollection(b0.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b2.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertEquals(b4.getSeenPubIds().size(), 0);
        assertEquals(b1.getSeenPubIds().size(), 0);
        assertEquals(b3.getSeenPubIds().size(), 0);
        assertEquals(b5.getSeenPubIds().size(), 0);
        // check statsCollector
        assertEquals(StatsCollector.getInstance().getTotalDeliveredPubCount(), 0);
        assertEquals(StatsCollector.getInstance().getTotalPublishedPubCount(), 1);
        assertEquals(StatsCollector.getInstance().getTotalSentPubCount(), 1);
    }

    @Test
    public void testB0PublishMultipleSubscribers() throws RuntimeSimException {
        b1.addFailTrace(0, 100);
        // create a pub
        Publication p0 = new Publication();
        p0.setPubClass(0);
        p0.setAttributes(new float[]{2.3f, 100f});
        p0.setSourceBroker(b5.getNodeInfo());
        // add subscriber
        List<NodeInfo> subscribers = new ArrayList<NodeInfo>();
        subscribers.add(b5.getNodeInfo()); subscribers.add(b4.getNodeInfo()); subscribers.add(b3.getNodeInfo());
        p0.setSubscribers(subscribers);
        // load pub
        b5.getPubSubLoad().addPub(p0);
        StatsCollector.getInstance().pubGenerated(p0, p0.getSourceBroker());
        // start sim
        overlay.getSimInterface().startSim();
        // check counters for messages
        // received
        assertEquals(b0.getMessagesReceived(), 1);
        assertEquals(b1.getMessagesReceived(), 0);
        assertEquals(b2.getMessagesReceived(), 1);
        assertEquals(b3.getMessagesReceived(), 0);
        assertEquals(b4.getMessagesReceived(), 1);
        assertEquals(b5.getMessagesReceived(), 0);
        // matched
        assertEquals(b0.getMessagesDelivered(), 0);
        assertEquals(b1.getMessagesDelivered(), 0);
        assertEquals(b2.getMessagesDelivered(), 0);
        assertEquals(b3.getMessagesDelivered(), 0);
        assertEquals(b4.getMessagesDelivered(), 1);
        assertEquals(b5.getMessagesDelivered(), 1);
        assertEquals(b5.getMessagesDeliveredToSelf(), 1);
        // published
        assertEquals(b0.getMessagesPublished(), 0);
        assertEquals(b1.getMessagesPublished(), 0);
        assertEquals(b2.getMessagesPublished(), 0);
        assertEquals(b3.getMessagesPublished(), 0);
        assertEquals(b4.getMessagesPublished(), 0);
        assertEquals(b5.getMessagesPublished(), 1);
        // sent
        assertEquals(b0.getMessagesSent(), 0);
        assertEquals(b1.getMessagesSent(), 0);
        assertEquals(b2.getMessagesSent(), 2);
        assertEquals(b3.getMessagesSent(), 0);
        assertEquals(b4.getMessagesSent(), 0);
        assertEquals(b5.getMessagesSent(), 1);
        // check seen publications
        assertTrue(CollectionUtils.isEqualCollection(b0.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b2.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b4.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertEquals(b1.getSeenPubIds().size(), 0);
        assertEquals(b3.getSeenPubIds().size(), 0);
        assertTrue(CollectionUtils.isEqualCollection(b5.getSeenPubIds(), Arrays.asList(p0.getId())));
        // check statsCollector
        assertEquals(StatsCollector.getInstance().getTotalDeliveredPubCount(), 2);
        assertEquals(StatsCollector.getInstance().getTotalPublishedPubCount(), 1);
        assertEquals(StatsCollector.getInstance().getTotalSentPubCount(), 3);
        assertEquals(StatsCollector.getInstance().getTotalSubscriptionMatchCount(), 3);
        assertEquals(StatsCollector.getInstance().getAverageLatency(), (0 + 45) / 2f, AllTests.ASSERT_DELTA);
        assertEquals(StatsCollector.getInstance().getAverageHopCount(), (0 + 2) / 2f, AllTests.ASSERT_DELTA);
        assertEquals(StatsCollector.getInstance().getPubDeliveryRate(), 2f/3, AllTests.ASSERT_DELTA);
    }

    @Test
    public void testOnePublisherMultipleSubscribersNoFault() throws RuntimeSimException {
        // create a pub
        Publication p0 = new Publication();
        p0.setPubClass(0);
        p0.setAttributes(new float[]{2.3f, 100f});
        p0.setSourceBroker(b5.getNodeInfo());
        // add subscriber
        List<NodeInfo> subscribers = new ArrayList<NodeInfo>();
        subscribers.add(b5.getNodeInfo()); subscribers.add(b4.getNodeInfo()); subscribers.add(b3.getNodeInfo());
        p0.setSubscribers(subscribers);
        // load pub
        b5.getPubSubLoad().addPub(p0);
        StatsCollector.getInstance().pubGenerated(p0, p0.getSourceBroker());
        // start sim
        overlay.getSimInterface().startSim();
        // check counters for messages
        // received
        assertEquals(b0.getMessagesReceived(), 1);
        assertEquals(b1.getMessagesReceived(), 1);
        assertEquals(b2.getMessagesReceived(), 1);
        assertEquals(b3.getMessagesReceived(), 1);
        assertEquals(b4.getMessagesReceived(), 1);
        assertEquals(b5.getMessagesReceived(), 0);
        // matched
        assertEquals(b0.getMessagesDelivered(), 0);
        assertEquals(b1.getMessagesDelivered(), 0);
        assertEquals(b2.getMessagesDelivered(), 0);
        assertEquals(b3.getMessagesDelivered(), 1);
        assertEquals(b4.getMessagesDelivered(), 1);
        assertEquals(b5.getMessagesDelivered(), 1);
        assertEquals(b5.getMessagesDeliveredToSelf(), 1);
        // published
        assertEquals(b0.getMessagesPublished(), 0);
        assertEquals(b1.getMessagesPublished(), 0);
        assertEquals(b2.getMessagesPublished(), 0);
        assertEquals(b3.getMessagesPublished(), 0);
        assertEquals(b4.getMessagesPublished(), 0);
        assertEquals(b5.getMessagesPublished(), 1);
        // sent
        assertEquals(b0.getMessagesSent(), 1);
        assertEquals(b1.getMessagesSent(), 1);
        assertEquals(b2.getMessagesSent(), 2);
        assertEquals(b3.getMessagesSent(), 0);
        assertEquals(b4.getMessagesSent(), 0);
        assertEquals(b5.getMessagesSent(), 1);
        // check seen publications
        assertTrue(CollectionUtils.isEqualCollection(b0.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b2.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b4.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b1.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b3.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b5.getSeenPubIds(), Arrays.asList(p0.getId())));
        // check statsCollector
        assertEquals(StatsCollector.getInstance().getTotalDeliveredPubCount(), 3);
        assertEquals(StatsCollector.getInstance().getTotalPublishedPubCount(), 1);
        assertEquals(StatsCollector.getInstance().getTotalSentPubCount(), 5);
        assertEquals(StatsCollector.getInstance().getTotalSubscriptionMatchCount(), 3);
        assertEquals(StatsCollector.getInstance().getAverageLatency(), (0 + 45 + 220) / 3f, AllTests.ASSERT_DELTA);
        assertEquals(StatsCollector.getInstance().getAverageHopCount(), (0 + 2 + 4) / 3f, AllTests.ASSERT_DELTA);
        assertEquals(StatsCollector.getInstance().getPubDeliveryRate(), 1, AllTests.ASSERT_DELTA);
    }

    @Test
    public void testSeenPubsAreDropped() {
        // create a pub
        Publication p0 = new Publication();
        p0.setPubClass(0);
        p0.setAttributes(new float[]{2.3f, 100f});
        p0.setSourceBroker(b5.getNodeInfo());
        // add subscriber
        List<NodeInfo> subscribers = new ArrayList<NodeInfo>();
        subscribers.add(b5.getNodeInfo()); subscribers.add(b4.getNodeInfo()); subscribers.add(b3.getNodeInfo());
        p0.setSubscribers(subscribers);
        // load pub
        b5.getPubSubLoad().addPub(p0);
        // start sim
        overlay.getSimInterface().startSim();
        // try resending p0
        PubMessage p0m = new PubMessage(b5.getNodeInfo(), b2.getNodeInfo(), p0);
        b2.getNodeSim().receiveMessage(p0m);
        // check counters for messages
        // received
        assertEquals(b0.getMessagesReceived(), 1);
        assertEquals(b1.getMessagesReceived(), 1);
        assertEquals(b2.getMessagesReceived(), 2); // receives but drops
        assertEquals(b3.getMessagesReceived(), 1);
        assertEquals(b4.getMessagesReceived(), 1);
        assertEquals(b5.getMessagesReceived(), 0);
        // matched
        assertEquals(b0.getMessagesDelivered(), 0);
        assertEquals(b1.getMessagesDelivered(), 0);
        assertEquals(b2.getMessagesDelivered(), 0);
        assertEquals(b3.getMessagesDelivered(), 1);
        assertEquals(b4.getMessagesDelivered(), 1);
        assertEquals(b5.getMessagesDelivered(), 1);
        assertEquals(b5.getMessagesDeliveredToSelf(), 1);
        // published
        assertEquals(b0.getMessagesPublished(), 0);
        assertEquals(b1.getMessagesPublished(), 0);
        assertEquals(b2.getMessagesPublished(), 0);
        assertEquals(b3.getMessagesPublished(), 0);
        assertEquals(b4.getMessagesPublished(), 0);
        assertEquals(b5.getMessagesPublished(), 1);
        // sent
        assertEquals(b0.getMessagesSent(), 1);
        assertEquals(b1.getMessagesSent(), 1);
        assertEquals(b2.getMessagesSent(), 2);
        assertEquals(b3.getMessagesSent(), 0);
        assertEquals(b4.getMessagesSent(), 0);
        assertEquals(b5.getMessagesSent(), 1);
        // check seen publications
        assertTrue(CollectionUtils.isEqualCollection(b0.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b2.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b4.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b1.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b3.getSeenPubIds(), Arrays.asList(p0.getId())));
        assertTrue(CollectionUtils.isEqualCollection(b5.getSeenPubIds(), Arrays.asList(p0.getId())));
        // check messages dropped
        assertEquals(b2.getMessagesDropped(), 1);
        assertEquals(b5.getMessagesDropped(), 0);
        assertEquals(b0.getMessagesDropped(), 0);
        // check statsCollector
        assertEquals(StatsCollector.getInstance().getTotalDeliveredPubCount(), 3);
        assertEquals(StatsCollector.getInstance().getTotalPublishedPubCount(), 1);
        assertEquals(StatsCollector.getInstance().getTotalSentPubCount(), 5);
        assertEquals(StatsCollector.getInstance().getAverageLatency(), (0 + 45 + 220) / 3f, AllTests.ASSERT_DELTA);
        assertEquals(StatsCollector.getInstance().getAverageHopCount(), (0 + 2 + 4) / 3f, AllTests.ASSERT_DELTA);
    }

    @Test
    public void testRouterAdvBased() throws RuntimeSimException {
        Router.isAdvBased = true;
        // create a pub
        Publication p0 = new Publication();
        p0.setPubClass(0);
        p0.setAttributes(new float[]{2.3f, 100f});
        // b5 is the publisher
        p0.setSourceBroker(b5.getNodeInfo());
        // add subscriber
        List<NodeInfo> subscribers = new ArrayList<NodeInfo>();
        // b5 and b3 are matching subscribers for this pub
        subscribers.add(b5.getNodeInfo()); subscribers.add(b3.getNodeInfo());
        p0.setSubscribers(subscribers);
        PubMessage p0m = new PubMessage(b5.getNodeInfo(), null, p0);
        // in the following pubMsg equals, same id is used to make them equal
        // in adv based, all except 4 should be able to route the pub
        assertEquals(b4.getRouter().route(p0m).size(), 0);
        assertTrue(CollectionUtils.isEqualCollection(
                b5.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b5.getNodeInfo(), b2.getNodeInfo(), p0, p0m.getId()))));
        // on b2, router knows only about b3's sub since b5's sub is local
        assertTrue(CollectionUtils.isEqualCollection(
                b2.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b2.getNodeInfo(), b0.getNodeInfo(), p0, p0m.getId()))));
        // on b0, router knows only about b3's sub since b5's sub is local
        assertTrue(CollectionUtils.isEqualCollection(
                b2.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b2.getNodeInfo(), b0.getNodeInfo(), p0, p0m.getId()))));
        // b0 knows only about b3's sub
        assertTrue(CollectionUtils.isEqualCollection(
                b0.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b0.getNodeInfo(), b1.getNodeInfo(), p0, p0m.getId()))));
        // however, if b0 was the publisher(advertiser), it'd know about b3 and b5's subs
        p0.setSourceBroker(b0.getNodeInfo());
        assertTrue(CollectionUtils.isEqualCollection(
                b0.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b0.getNodeInfo(), b1.getNodeInfo(), p0, p0m.getId()),
                        new PubMessage(b0.getNodeInfo(), b2.getNodeInfo(), p0, p0m.getId()))));
        p0.setSourceBroker(b5.getNodeInfo());
        // b1 knows only about b3's sub
        assertTrue(CollectionUtils.isEqualCollection(
                b1.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b1.getNodeInfo(), b3.getNodeInfo(), p0, p0m.getId()))));
        // however, if b1 was the publisher(advertiser), it'd know about b3 and b5's subs
        p0.setSourceBroker(b1.getNodeInfo());
        assertTrue(CollectionUtils.isEqualCollection(
                b1.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b1.getNodeInfo(), b3.getNodeInfo(), p0, p0m.getId()),
                        new PubMessage(b1.getNodeInfo(), b0.getNodeInfo(), p0, p0m.getId()))));
        p0.setSourceBroker(b5.getNodeInfo());
        // b3 knows only about it's own local sub, therefore router knows no sub
        assertEquals(b3.getRouter().route(p0m).size(), 0);
    }

    @Test
    public void testRouterSubBased() throws RuntimeSimException {
        Router.isAdvBased = false;
        // create a pub
        Publication p0 = new Publication();
        p0.setPubClass(0);
        p0.setAttributes(new float[]{2.3f, 100f});
        // b0 is the publisher
        p0.setSourceBroker(b5.getNodeInfo());
        // add subscriber
        List<NodeInfo> subscribers = new ArrayList<NodeInfo>();
        // b5 and b3 are matching subscribers for this pub
        subscribers.add(b5.getNodeInfo()); subscribers.add(b3.getNodeInfo());
        p0.setSubscribers(subscribers);
        PubMessage p0m = new PubMessage(b0.getNodeInfo(), null, p0);
        // in sub based, all should be able to route the pub
        assertTrue(CollectionUtils.isEqualCollection(
                b0.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b0.getNodeInfo(), b1.getNodeInfo(), p0, p0m.getId()),
                        new PubMessage(b0.getNodeInfo(), b2.getNodeInfo(), p0, p0m.getId()))));
        assertTrue(CollectionUtils.isEqualCollection(
                b1.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b1.getNodeInfo(), b3.getNodeInfo(), p0, p0m.getId()),
                        new PubMessage(b1.getNodeInfo(), b0.getNodeInfo(), p0, p0m.getId()))));
        assertTrue(CollectionUtils.isEqualCollection(
                b2.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b2.getNodeInfo(), b0.getNodeInfo(), p0, p0m.getId()),
                        new PubMessage(b2.getNodeInfo(), b5.getNodeInfo(), p0, p0m.getId()))));
        assertTrue(CollectionUtils.isEqualCollection(
                b3.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b3.getNodeInfo(), b1.getNodeInfo(), p0, p0m.getId()))));
        assertTrue(CollectionUtils.isEqualCollection(
                b4.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b4.getNodeInfo(), b2.getNodeInfo(), p0, p0m.getId()))));
        assertTrue(CollectionUtils.isEqualCollection(
                b5.getRouter().route(p0m),
                Arrays.asList(
                        new PubMessage(b5.getNodeInfo(), b2.getNodeInfo(), p0, p0m.getId()))));
    }
}
