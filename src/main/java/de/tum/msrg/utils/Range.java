package de.tum.msrg.utils;

import static org.junit.Assert.*;

public class Range {

	public float start;
	public float end;
	
	public Range() {
		start = end = -1;
	}
	
	public Range(float start, float end) {
		if(end < start)
			throw new RuntimeException("End(" + end + ") of a range should be greater than the start(" + start + ") of the range!");
		this.start = start;
		this.end = end;
	}
	
	public boolean isCovered(float point) {
		return point >= start && point <= end;
	}
	
	public boolean doesOverlap(Range r) {
		if(isCovered(r.start) || isCovered(r.end))
			return true;
		if(r.isCovered(start) || r.isCovered(end))
			return true;
		return false;
	}
	
	public String toString() {
		return "(" + start + ", " + end + ")";
	}
	
	// TODO: move to TestRange
	public static void main(String[] args) {
		Range r = new Range(3, 100);
		assertTrue(r.isCovered(3));
		assertTrue(r.isCovered(100));
		assertTrue(r.isCovered(50));
		assertTrue(!r.isCovered(-1));
		assertTrue(!r.isCovered(101));
		assertTrue(r.doesOverlap(new Range(3, 4)));
		assertTrue(r.doesOverlap(new Range(1, 101)));
	}
}
