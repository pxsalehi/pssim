package de.tum.msrg.underlay;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;

import de.tum.msrg.baseline.ConfigKeys;
import de.tum.msrg.config.Configuration;
import de.tum.msrg.topology.EdgeInfo;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.topology.Topology;

public class TestNetwork {
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
	private EdgeInfo e0 = new EdgeInfo(0, n0, n1, 52);
	private EdgeInfo e1 = new EdgeInfo(1, n0, n2, 63);
	private EdgeInfo e2 = new EdgeInfo(2, n1, n3, 70);
	private EdgeInfo e3 = new EdgeInfo(3, n2, n4, 10);
	private EdgeInfo e4 = new EdgeInfo(4, n2, n5, 35);
	private Topology topology = null;
	private Topology topologyWithWrongDegree = null;
	private Configuration config = null;
	
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
	public void testCreateNetwork() throws UnderlayException {
		Network network = new Network(config, topology);
		assertEquals(6, network.getNodes().length);
		assertEquals(5, network.getEdges().length);
		assertEquals(network.getEdge(0), network.getEdge(network.getNode(0), network.getNode(1)));
		assertEquals(network.getEdge(3), network.getEdge(network.getNode(2), network.getNode(4)));
		assertNull(network.getEdge(network.getNode(2), network.getNode(1)));
		assertEquals(network.getEdge(network.getNode(2), network.getNode(0)), 
				     network.getEdge(network.getNode(0), network.getNode(2)));
	}
	
	// TODO: this belongs to TestNode
	@Test(expected=UnderlayException.class)
	public void testCreateNetworkFailDueToIncorrectOutDegree() throws UnderlayException {
		Network network = new Network(config, topologyWithWrongDegree);
		network.buildDijkistraTree(network.getNode(0));
	}

	@Test
	public void testBuildDijkstraTree() throws UnderlayException {
		Network network = new Network(config, topology);
		network.buildDijkistraTree(network.getNode(0));
		assertNull(network.getNode(0).getDijkParentNode());
		assertEquals(network.getNode(0), network.getNode(2).getDijkParentNode());
		assertEquals(network.getNode(0), network.getNode(1).getDijkParentNode());
		assertEquals(network.getNode(2), network.getNode(4).getDijkParentNode());
		assertEquals(network.getNode(2), network.getNode(5).getDijkParentNode());
		assertEquals(network.getNode(1), network.getNode(3).getDijkParentNode());
		network.buildDijkistraTree(network.getNode(2));
		assertNull(network.getNode(2).getDijkParentNode());
		assertEquals(network.getNode(2), network.getNode(0).getDijkParentNode());
		assertEquals(network.getNode(0), network.getNode(1).getDijkParentNode());
		assertEquals(network.getNode(1), network.getNode(3).getDijkParentNode());
		assertEquals(network.getNode(2), network.getNode(4).getDijkParentNode());
		assertEquals(network.getNode(2), network.getNode(5).getDijkParentNode());
	}
	
	@Test
	public void testBuildRouteTablesGetRoute() throws UnderlayException {
		Network network = new Network(config, topology);
		network.buildRouteTables();
		Integer[][] nextHop = new Integer[][] {{null, 1, 2, 1, 2, 2}, // from node 0 to all others
											   {0, null, 0, 3, 0, 0}, // ...
											   {0, 0, null, 0, 4, 5},
											   {1, 1, 1, null, 1, 1},
											   {2, 2, 2, 2, null, 2},
											   {2, 2, 2, 2, 2, null}};
		for(int i = 0; i < network.getNoOfNodes(); ++i)
			for(int j = 0; j < network.getNoOfNodes(); ++j)
				if(i == j)
					assertNull(network.getNode(i).getRoute(network.getNode(j)));
				else
					assertEquals(network.getNode(nextHop[i][j]), 
							     network.getNode(i).getRoute(network.getNode(j)));
	}
	
	@Test(expected=UnderlayException.class)
	public void testFindStaticRouteThrowsIfRoutingTableNotBuilt() throws UnderlayException {
		Network network = new Network(config, topology);
		network.findRoute(network.getNode(1), network.getNode(2));
	}
	
	@Test(expected=UnderlayException.class)
	public void testFindRouteSameSrcAndDest() throws UnderlayException {
		Network network = new Network(config, topology);
		network.buildRouteTables();
		RouteChain route = network.findRoute(network.getNode(1), network.getNode(1));
	}
	
	@Test(expected=UnderlayException.class)
	public void testFindRouteNoRoute() throws UnderlayException {
		Network network = new Network(config, topologyWithWrongDegree); // 5 is disconnected
		network.buildRouteTables();
	}
	
	@Test
	public void testFindRouteSuccessful() throws UnderlayException {
		Network network = new Network(config, topology);
		network.buildRouteTables();
		RouteChain route = network.findRoute(network.getNode(1), network.getNode(3));
		assertEquals(2, route.getRouteLength());
		assertEquals(route.getRouterChain().get(0), network.getNode(1));
		assertEquals(route.getRouterChain().get(1), network.getNode(3));
		assertEquals(1, route.getDelayChain().size());
		assertEquals(route.getDelayChain().get(0).floatValue(), 70f, 0.001);
		route = network.findRoute(network.getNode(4), network.getNode(3));
		assertEquals(5, route.getRouteLength());
		assertEquals(route.getRouterChain(), 
				     Arrays.asList(network.getNode(4), network.getNode(2), network.getNode(0), 
				    		       network.getNode(1), network.getNode(3)));
		assertEquals(4, route.getDelayChain().size());
		assertEquals(route.getDelayChain(), Arrays.asList(10f, 63f, 52f, 70f));
	}
}

