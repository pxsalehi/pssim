package de.tum.msrg.topology;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import de.tum.msrg.topology.NodeInfo;

import java.util.Arrays;

/**
 * Created by pxsalehi on 10.12.15.
 */
public class TestNodeInfo {
    @Test
    public void testEquals() {
        NodeInfo n0 = new NodeInfo(0, 1);
        NodeInfo n1 = new NodeInfo(1, 2);
        NodeInfo n2 = new NodeInfo(2, 1);
        assertTrue(CollectionUtils.isEqualCollection(Arrays.asList(n0, n1), Arrays.asList(n1, n0)));
        assertFalse(CollectionUtils.isEqualCollection(Arrays.asList(n0, n1), Arrays.asList(n1, n2)));
    }
}
