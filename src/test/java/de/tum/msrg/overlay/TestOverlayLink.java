package de.tum.msrg.overlay;

import de.tum.msrg.baseline.BaselineBroker;
import de.tum.msrg.baseline.ConfigKeys;
import de.tum.msrg.baseline.BaselineOverlay;
import de.tum.msrg.config.ConfigParserException;
import de.tum.msrg.config.Configuration;
import de.tum.msrg.resources.topologies.Topologies;
import de.tum.msrg.sim.StatsCollector;
import de.tum.msrg.topology.Topology;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.IOException;

/**
 * Created by pxsalehi on 08.04.16.
 */
public class TestOverlayLink {

    private Configuration config;
    private Topology topology;
    private BaselineOverlay overlay;
    BaselineBroker b0, b1, b2, b3, b4, b5;
    int seed;

    public void setUp(boolean applyLinkBW) throws RuntimeSimException, ConfigParserException, IOException {
//        seed = 8543546;
//        config = new Configuration(new FileReader(Topologies.T01_DIR_PATH + "sim.config"));
//        config.setProperty(ConfigKeys.SIM_APPLY_LINK_CONGESTION, applyLinkBW);
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
    }

    @After
    public void tearDown() {
        StatsCollector.reset();
    }

    @Test
    public void testEqualUndirected() throws RuntimeSimException, IOException, ConfigParserException {
        setUp(false);
        OverlayLink b0tob1 = b0.getLinkTo(b1);
        OverlayLink b1tob0 = b1.getLinkTo(b0);
        OverlayLink b0tob2 = b0.getLinkTo(b2);
        assertEquals(b0tob1, b1tob0);
        assertFalse(b0tob1.equals(b0tob2));
    }

    @Test
    public void testEqualDirected() throws RuntimeSimException, IOException, ConfigParserException {
        setUp(true);
        OverlayLink b0tob1 = b0.getLinkTo(b1);
        OverlayLink b1tob0 = b1.getLinkTo(b0);
        OverlayLink b0tob2 = b0.getLinkTo(b2);
        assertFalse(b0tob1.equals(b1tob0));
        assertFalse(b0tob1.equals(b0tob2));
    }

    @Test
    public void testHashCodeUndirected() throws RuntimeSimException, IOException, ConfigParserException {
        setUp(false);
        OverlayLink b0tob1 = b0.getLinkTo(b1);
        OverlayLink b1tob0 = b1.getLinkTo(b0);
        OverlayLink b0tob2 = b0.getLinkTo(b2);
        assertTrue(b0tob1.hashCode() == b1tob0.hashCode());
        assertTrue(b0tob1.hashCode() != b0tob2.hashCode());
    }

    @Test
    public void testHashCodeDirected() throws RuntimeSimException, IOException, ConfigParserException {
        setUp(true);
        OverlayLink b0tob1 = b0.getLinkTo(b1);
        OverlayLink b1tob0 = b1.getLinkTo(b0);
        OverlayLink b0tob2 = b0.getLinkTo(b2);
        assertTrue(b0tob1.hashCode() != b1tob0.hashCode());
        assertTrue(b0tob1.hashCode() != b0tob2.hashCode());
    }
}
