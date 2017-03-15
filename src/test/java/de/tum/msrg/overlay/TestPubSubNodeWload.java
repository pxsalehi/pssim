package de.tum.msrg.overlay;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import de.tum.msrg.topology.NodeInfo;

public class TestPubSubNodeWload {

	@Test
	public void testEqual() {
		NodeInfo n0 = new NodeInfo(0, 1);
		NodeInfo n1 = new NodeInfo(1, 2);
		NodeInfo n2 = new NodeInfo(2, 1);
		assertFalse(CollectionUtils.isEqualCollection(Arrays.asList(n0, n1), Arrays.asList(n1, n2)));
		assertTrue(CollectionUtils.isEqualCollection(Arrays.asList(n0, n1), Arrays.asList(n1, n0)));
	}

}
