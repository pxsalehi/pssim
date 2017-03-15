package de.tum.msrg.message;

import de.tum.msrg.AllTests;
import de.tum.msrg.topology.NodeInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by pxsalehi on 05.04.16.
 */
public class TestPublication {

    @Test
    public void testClone() {
        NodeInfo pubBroker = new NodeInfo(0, 2);
        Publication p = new Publication(0, new float[]{2.3f, 100f});
        p.setSourceBroker(pubBroker);
        Publication pclone = new Publication(p);
        assertEquals(pclone.getId(), p.getId() + 1);
        assertEquals(pclone.getSourceBroker(), p.getSourceBroker());
        assertEquals(pclone.getPubClass(), p.getPubClass());
        assertEquals(pclone.getAttributes().length, p.getAttributes().length);
        for(int i = 0; i < p.getAttributes().length; ++i)
            assertEquals(p.getAttributes()[i], pclone.getAttributes()[i], AllTests.ASSERT_DELTA);
    }
}
