package de.tum.msrg.message;

import org.junit.Test;
import static org.junit.Assert.*;
import de.tum.msrg.topology.NodeInfo;

/**
 * Created by pxsalehi on 18.12.15.
 */
public class TestMessage {
    private NodeInfo b1 = new NodeInfo(1, 1);
    private NodeInfo b2 = new NodeInfo(2, 2);
    private NodeInfo b3 = new NodeInfo(3, 1);

    @Test
    public void testEquals() {
        Message m1 = new Message(Message.Type.PUB, b1, b2);
        Message m2 = new Message(Message.Type.PUB, b2, b1);
        Message m3 = new Message(Message.Type.PUB, b1, b2);
        m3.setId(m1.getId());
        Message m4 = new Message(Message.Type.PUB, b1, b2);
        assertEquals(m1, m3);
        assertEquals(m3, m1);
        assertFalse(m1.equals(m2));
        assertFalse(m2.equals(m3));
        assertFalse(m1.equals(m4)); // different message ids
    }

    @Test
    public void testPubMessageEquals() {
        Publication p0 = new Publication();
        Publication p1 = new Publication();
        PubMessage pm1 = new PubMessage(b1, b2, p0);
        PubMessage pm2 = new PubMessage(b1, b2, p0);
        PubMessage pm3 = new PubMessage(b1, b2, p0);
        pm3.setId(pm1.getId());
        PubMessage pm4 = new PubMessage(b2, b1, p0);
        pm4.setId(pm2.getId());
        assertEquals(pm1, pm3);
        assertFalse(pm1.equals(pm2));
        assertFalse(pm2.equals(pm4));
    }
}
