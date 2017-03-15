package de.tum.msrg.underlay;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.tum.msrg.baseline.ConfigKeys;
import de.tum.msrg.config.Configuration;
import de.tum.msrg.topology.EdgeInfo;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.topology.Topology;

public class TestNode {
	/**
	 *     .--1---3
	 * 0---+--2---4
	 *         '--5
	 */
	private NodeInfo n0 = new NodeInfo(0, 2);
	private NodeInfo n1 = new NodeInfo(1, 2);
	private NodeInfo n2 = new NodeInfo(2, 3);
	private NodeInfo n2WrongDegree = new NodeInfo(2, 2);
	private NodeInfo n3 = new NodeInfo(3, 1);
	private NodeInfo n4 = new NodeInfo(4, 1);
	private NodeInfo n5 = new NodeInfo(5, 1);
	private EdgeInfo e0 = new EdgeInfo(0, n0, n1, 52f);
	private EdgeInfo e1 = new EdgeInfo(1, n0, n2, 63f);
	private EdgeInfo e2 = new EdgeInfo(2, n1, n3, 70f);
	private EdgeInfo e3 = new EdgeInfo(3, n2, n4, 10f);
	private EdgeInfo e4 = new EdgeInfo(4, n2, n5, 35f);
	private Topology topology = null;
	private Topology topologyWithWrongDegree = null;
	private Configuration config = null;
	private static final double DELTA = 1e-5;
	
	@Before
	public void setupNetworkTopology() {
		config = new Configuration();
		config.addProperty(ConfigKeys.NETWORK_RANDOM_SEED, -1);
		topology = new Topology(new NodeInfo[] {n0, n1, n2, n3, n4, n5}, 
		                        new EdgeInfo[] {e0, e1, e2, e3, e4});
		topologyWithWrongDegree = new Topology(new NodeInfo[] {n0, n1, n2WrongDegree, n3, n4, n5}, 
                                               new EdgeInfo[] {e0, e1, e2, e3, e4});
	}
	
	@Test
	public void testGetLatency() throws UnderlayException {
		Network network = new Network(config, topology);
		network.buildRouteTables();
		float routeLatency = network.getNode(1).getLatency(network.getNode(4));
		assertEquals(routeLatency, 125f, DELTA);
		routeLatency = network.getNode(0).getLatency(network.getNode(2));
		assertEquals(routeLatency, 63f, DELTA);
	}

}
