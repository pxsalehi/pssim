package de.tum.msrg.message;

import static org.junit.Assert.*;

import org.junit.Test;

import de.tum.msrg.message.Attribute;
import de.tum.msrg.message.Publication;
import de.tum.msrg.message.Subscription;
import de.tum.msrg.message.Attribute.Operator;

public class TestSubscription {

	@Test
	public void testIsMatches() {
		/**
		 * p1: { 0, 10 , 421  } matches s1
		 * p2: { 0, 1  , 99   } matches s1, s3 
		 * p3: { 1, 500, 99.8 } matches s2
		 * p4: { 2, 89, 900.5 } matches nothing
		 * 
		 * s1: { 0, x <= 10 , 90 <= y <= 421 }
		 * s2: { 1, 300 <= x <= 600 , 90 <= y <= 100] }
		 * s3: { 0, 1 <= x <= 2 ,  y >= 90] }
		 * s4: { 3, *, * }
		 */
		Attribute.lowRange = 0f;
		Attribute.highRange = 1000f;
		Publication p1 = new Publication(0, new float[]{10f, 421f});
		Publication p2 = new Publication(0, new float[]{1f, 99f});
		Publication p3 = new Publication(1, new float[]{500f, 99.8f});
		Publication p4 = new Publication(2, new float[]{89f, 900.5f});
		Subscription s1 = new Subscription(0, new Attribute[]{
				new Attribute(Operator.LESS_THAN, Attribute.lowRange, 10f),
				new Attribute(Operator.RANGE, 90f, 421f)});
		Subscription s2 = new Subscription(1, new Attribute[]{
				new Attribute(Operator.RANGE, 300f, 600f),
				new Attribute(Operator.RANGE, 90f, 100f)});
		Subscription s3 = new Subscription(0, new Attribute[]{
				new Attribute(Operator.RANGE, 1f, 2f),
				new Attribute(Operator.GREATER_THAN, 90f, Attribute.highRange)});
		Subscription s4 = new Subscription(3, new Attribute[]{
				new Attribute(Operator.RANGE, Attribute.lowRange, Attribute.highRange),
				new Attribute(Operator.RANGE, Attribute.lowRange, Attribute.highRange)});
		assertTrue(s1.isMatches(p1));
		assertFalse(s2.isMatches(p1));
		assertFalse(s3.isMatches(p1));
		assertFalse(s4.isMatches(p1));
		assertTrue (s1.isMatches(p2));
		assertFalse(s2.isMatches(p2));
		assertTrue(s3.isMatches(p2));
		assertFalse(s4.isMatches(p2));
		assertFalse(s1.isMatches(p3));
		assertTrue(s2.isMatches(p3));
		assertFalse(s3.isMatches(p3));
		assertFalse(s4.isMatches(p3));
		assertFalse(s1.isMatches(p4));
		assertFalse(s2.isMatches(p4));
		assertFalse(s3.isMatches(p4));
		assertFalse(s4.isMatches(p4));
	}

}
