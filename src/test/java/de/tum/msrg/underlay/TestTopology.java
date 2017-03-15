package de.tum.msrg.underlay;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import de.tum.msrg.AllTests;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import de.tum.msrg.topology.EdgeInfo;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.topology.Topology;

public class TestTopology {

	private String correctTopo = "No of nodes:6 \n" 
			                   + "No of edges:5 \n\n"
			                   + "Nodes: \n"
			                   + "0	2 \n"
			                   + "1	2 \n"
			                   + "2	2 \n"
			                   + "3	1 \n"
			                   + "4	1 \n"
			                   + "5	1 \n\n"
			                   + "Edges: \n"
			                   + "0	0	1	52 \n"
			                   + "1	0	2	63 \n"
			                   + "2	1	3	70 \n"
			                   + "3	2	4	10 \n"
			                   + "4	2	5	35 \n";

	private String correctTopoWithBW = "No of nodes:6 \n"
			+ "No of edges:5 \n\n"
			+ "Nodes: \n"
			+ "0	2 \n"
			+ "1	2 \n"
			+ "2	2 \n"
			+ "3	1 \n"
			+ "4	1 \n"
			+ "5	1 \n\n"
			+ "Edges: \n"
			+ "0	0	1	52  100\n"
			+ "1	0	2	63  \n"
			+ "2	1	3	70  500\n"
			+ "3	2	4	10  200\n"
			+ "4	2	5	35 \n";

	NodeInfo n0 = new NodeInfo(0, 2);
	NodeInfo n1 = new NodeInfo(1, 2);
	NodeInfo n2 = new NodeInfo(2, 2);
	NodeInfo n3 = new NodeInfo(3, 1);
	NodeInfo n4 = new NodeInfo(4, 1);
	NodeInfo n5 = new NodeInfo(5, 1);
	EdgeInfo e0 = new EdgeInfo(0, n0, n1, 52f);
	EdgeInfo e1 = new EdgeInfo(1, n0, n2, 63f);
	EdgeInfo e2 = new EdgeInfo(2, n1, n3, 70f);
	EdgeInfo e3 = new EdgeInfo(3, n2, n4, 10f);
	EdgeInfo e4 = new EdgeInfo(4, n2, n5, 35f);

	@Test
	public void testTopologyFromString() {
		Topology topo = new Topology(correctTopo);
		assertEquals(6, topo.getNoOfNodes());
		assertEquals(5, topo.getNoOfEdges());
		List<NodeInfo> nodes = Arrays.asList(topo.getNodes());
		List<EdgeInfo> edges = Arrays.asList(topo.getEdges());
		assertTrue(CollectionUtils.isEqualCollection(nodes, Arrays.asList(n0, n1, n2, n3, n4, n5)));
		assertTrue(CollectionUtils.isEqualCollection(edges, Arrays.asList(e0, e1, e2, e3, e4)));
		for(EdgeInfo e: edges)
			assertEquals(Float.MAX_VALUE, e.getBandwidth(), AllTests.ASSERT_DELTA);
	}

	@Test
	public void testTopologyFromStringWithString() {
		Topology topo = new Topology(correctTopoWithBW);
		assertEquals(6, topo.getNoOfNodes());
		assertEquals(5, topo.getNoOfEdges());
		List<NodeInfo> nodes = Arrays.asList(topo.getNodes());
		List<EdgeInfo> edges = Arrays.asList(topo.getEdges());
		assertTrue(CollectionUtils.isEqualCollection(nodes, Arrays.asList(n0, n1, n2, n3, n4, n5)));
		assertTrue(CollectionUtils.isEqualCollection(edges, Arrays.asList(e0, e1, e2, e3, e4)));

		assertEquals(100, edges.get(0).getBandwidth(), AllTests.ASSERT_DELTA);
		assertEquals(Float.MAX_VALUE, edges.get(1).getBandwidth(), AllTests.ASSERT_DELTA);
		assertEquals(500, edges.get(2).getBandwidth(), AllTests.ASSERT_DELTA);
		assertEquals(200, edges.get(3).getBandwidth(), AllTests.ASSERT_DELTA);
		assertEquals(Float.MAX_VALUE, edges.get(4).getBandwidth(), AllTests.ASSERT_DELTA);
	}
}
