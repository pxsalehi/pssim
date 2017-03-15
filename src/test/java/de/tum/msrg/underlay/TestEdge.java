package de.tum.msrg.underlay;

import static org.junit.Assert.*;

import org.junit.Test;

import de.tum.msrg.underlay.Edge;
import de.tum.msrg.underlay.Node;

public class TestEdge {

	@Test
	public void testIsLinkTrue() {
		Node n1 = new Node(null, 1, 1);
		Node n2 = new Node(null, 2, 1);
		Node n3 = new Node(null, 3, 1);
		Edge edge = new Edge(null, n1, n2, 100, 20);
		assertTrue(edge.isLink(n1, n2));
		assertTrue(edge.isLink(n2, n1));
		assertFalse(edge.isLink(n3, n1));
		assertFalse(edge.isLink(n3, n2));
		assertFalse(edge.isLink(n1, n3));
		assertFalse(edge.isLink(n2, n3));
	}
}
