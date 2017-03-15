package de.tum.msrg.message;

public class Attribute {

	public enum Operator {
		RANGE, LESS_THAN, GREATER_THAN;
	}
	
	public static int noOfAttributes;
	public static float lowRange;
	public static float highRange;

	public Operator op;
	public float lowVal;
	public float highVal;

	public Attribute(Operator op, float lowVal, float highVal) {
		this.op = op;
		this.lowVal = lowVal;
		this.highVal = highVal;
	}

	public float getOverlap(Attribute aAttrib) {
		float overlap = Math.min(highVal, aAttrib.highVal) - Math.max(lowVal, aAttrib.lowVal);
		return Math.max(0.0f, overlap);
	}
	
	public float getRange() {
		return highVal - lowVal;
	}
	
	public String toString() {
		StringBuffer strbuf = new StringBuffer();
		strbuf.append("[");
		strbuf.append(lowVal);
		strbuf.append(",");
		strbuf.append(highVal);
		strbuf.append("]");
		return strbuf.toString();
	}

	public static Attribute greaterThan(float lowVal) {
		return new Attribute(Operator.GREATER_THAN, lowVal, highRange);
	}

	public static Attribute lessThan(float highVal) {
		return new Attribute(Operator.LESS_THAN, lowRange, highVal);
	}

	public static Attribute range(float lowVal, float highVal) {
		return new Attribute(Operator.RANGE, lowVal, highVal);
	}

	public static Attribute star() {
		return new Attribute(Operator.RANGE, lowRange, highRange);
	}
}