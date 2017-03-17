package de.tum.msrg.message;

import java.util.HashSet;
import java.util.Set;

public class Advertisement {
	private static int advCounter = 0;
	// unique ID across the system, not per node
	private int advID;
	private int advClass;
	private Attribute[] attributes;
	private int sourceId = -1;

	public Advertisement() {
		advID = advCounter++;
	}

	public Advertisement(int advClass, Attribute[] attributes) {
		this();
		setAdvClass(advClass);
		setAttributes(attributes);
	}

	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public int getID() {
		return advID;
	}
	
	public void setID(int id) {
		advID = id;
	}

	public int getAdvClass() {
		return advClass;
	}

	public void setAdvClass(int advClass) {
		this.advClass = advClass;
	}

	public Attribute[] getAttributes() {
		return attributes;
	}

	public void setAttributes(Attribute[] attributes) {
		this.attributes = attributes;
	}
	
	public String toString() {
		String outStr = String.format("AdvID: %05d ", advID);
		outStr += String.format("{AdvClass: %d}", advClass);
		for (int i = 0; i < attributes.length; i++) {
			Attribute attr = attributes[i];
			if (attr == null) {
				outStr += String.format(" {attr%d: [%7s, %7s]}", i, "*", "*");
			} else {
				outStr += String.format(" {attr%d: [%7.2f, %7.2f]}", i, attr.lowVal,
						attr.highVal);
			}
		}
		return outStr;
	}

	public float getArea() {
		float area = 1;
		for (Attribute attrib : attributes) {
			area *= attrib.highVal - attrib.lowVal;
		}
		return area;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + advID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Advertisement other = (Advertisement) obj;
		if (advID != other.advID)
			return false;
		return true;
	}

	public boolean isMatches(Subscription sub) {
		if (advClass != sub.getSubClass()) {
			return false;
		}
		Attribute[] subAttributes = sub.getAttributes();
		for (int i = 0; i < attributes.length; i++) {
			// adv.attr[i] overlaps with sub.attr[i]
			if ((attributes[i].lowVal > subAttributes[i].highVal) ||
					(attributes[i].highVal < subAttributes[i].lowVal)) {
				return false;
			}
		}
		return true;
	}

	public static void resetCounter() {
		advCounter = 0;
	}
}
