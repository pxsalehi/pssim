package de.tum.msrg.overlay;

import org.junit.Test;
import static org.junit.Assert.*;
import de.tum.msrg.overlay.OverlayGossipLatency;

/**
 * Created by pxsalehi on 11.01.16.
 */
public class TestOverlayGossipLatency {
    @Test
    public void testGenerateLatencies() {
        int noOfNodes = 20, minGossipLatency = 15, maxGossipLatency = 87;
        long seed = 325487;
        OverlayGossipLatency gossipLatencies =
                new OverlayGossipLatency(noOfNodes, minGossipLatency, maxGossipLatency, seed);
        for(int i = 0; i < noOfNodes; i++)
            for(int j = 0; j < noOfNodes; j++) {
                int latency = gossipLatencies.get(i, j);
                if (i == j)
                    assertEquals(latency, 0);
                else {
                    assertFalse(latency == 0);
                    assertTrue(latency >= minGossipLatency);
                    assertTrue(latency <= maxGossipLatency);
                }
            }
    }
}
