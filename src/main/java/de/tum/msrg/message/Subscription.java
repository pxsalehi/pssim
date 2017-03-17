package de.tum.msrg.message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tum.msrg.overlay.RuntimeSimException;
import de.tum.msrg.topology.NodeInfo;

public class Subscription {

	private static int subCounter = 0;
	protected static int totalClasses;
	protected int subID; // unique ID across the system, not per node
	protected int subClass;
	protected Attribute[] attributes;
	private Set<Advertisement> matchingAdvs = new HashSet<Advertisement>();
	private NodeInfo subscriberNode;

	public Subscription() {
		subID = subCounter++;
	}

	public Subscription(int subClass, Attribute[] attributes) {
		this();
		setSubClass(subClass);
		setAttributes(attributes);
	}
	
	public int getID() {
		return subID;
	}

	public static void setTotalClasses(int classCount) {
		totalClasses = classCount;
	}

	public static int getTotalClasses() {
		return totalClasses;
	}

	public void setSubClass(int sClass) {
		subClass = sClass;
	}

	public int getSubClass() {
		return subClass;
	}

	public void setAttributes(Attribute[] attribs) {
		this.attributes = attribs;
	}

	public Attribute[] getAttributes() {
		if (attributes == null) {
			throw new RuntimeException(String.format("Sub %d: attributes are not set", subID));
		}
		return attributes;
	}

	public boolean isAttributeValid(int attributeID) {
		if (attributes == null) {
			throw new RuntimeException(String.format("Sub %d: attributes are not set", subID));
		}
		return (attributes[attributeID] != null);
	}

	public float[] getCenterPoint() {
		float[] center = new float[attributes.length];
		for (int i = 0; i < attributes.length; i++) {
			center[i] = (attributes[i].lowVal + attributes[i].highVal) / 2;
		}
		return center;
	}

	public boolean isCovers(Subscription coveree) throws RuntimeSimException {
		if (subClass != coveree.getSubClass()) {
			return false;
		}
		Attribute[] covereeAttributes = coveree.getAttributes();
		return checkCover(covereeAttributes);
	}
	
	private boolean checkCover(Attribute[] covereeAttributes) {
		for (int i = 0; i < attributes.length; i++) {
			if ((attributes[i].lowVal > covereeAttributes[i].lowVal)
					|| (attributes[i].highVal < covereeAttributes[i].highVal)) {
				return false;
			}
		}
		return true;		
	}

	public boolean isCovers(Advertisement coveree) throws RuntimeSimException {
		if (subClass != coveree.getAdvClass()) {
			return false;
		}
		Attribute[] covereeAttributes = coveree.getAttributes();
		return checkCover(covereeAttributes);
	}

	public float getArea() {
		float area = 1;
		for (Attribute attrib : attributes) {
			area *= attrib.highVal - attrib.lowVal;
		}
		return area;
	}

	public float getOverlapArea(Subscription sub) throws RuntimeSimException {
		if (subClass != sub.getSubClass()) {
			return 0;
		}
		float area = 1;
		Attribute[] aAttribs = sub.getAttributes();
		for (int i = 0; i < attributes.length; i++) {
			area *= attributes[i].getOverlap(aAttribs[i]);
		}
		return area;
	}
	
	public float getOverlapArea(Advertisement adv) throws RuntimeSimException {
		if (subClass != adv.getAdvClass()) {
			return 0;
		}
		float area = 1;
		Attribute[] aAttribs = adv.getAttributes();
		for (int i = 0; i < attributes.length; i++) {
			area *= attributes[i].getOverlap(aAttribs[i]);
		}
		return area;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + subID;
		return result;
	}

	@Override
	public boolean equals(Object sub) {
		if (sub instanceof Subscription) {
			return subID == ((Subscription) sub).getID();
		}
		return false;
	}

	public String toString() {
		String outStr = String.format("SubID: %05d ", subID);
		outStr += String.format("{SubClass: %d}", subClass);
		for (int i = 0; i < attributes.length; i++) {
			Attribute attr = attributes[i];
			if (attr == null) {
				outStr += String.format(" {attr%d: [%7s, %7s]}", i, "*", "*");
			} else {
				outStr += String.format(" {attr%d: [%7.2f, %7.2f]}", i, attr.lowVal, attr.highVal);
			}
		}
		return outStr;
	}

	public boolean isMatches(Publication pub) {
		if (subClass != pub.getPubClass()) {
			return false;
		}
		float[] pubAttributes = pub.getAttributes();
		for (int i = 0; i < attributes.length; i++) {
			if ((attributes[i].lowVal > pubAttributes[i])
					|| (attributes[i].highVal < pubAttributes[i])) {
				return false;
			}
		}
		return true;
	}

	public Set<Advertisement> getMatchingAdvs() {
		return matchingAdvs;
	}

	public void addMatchingAdvs(Advertisement adv) {
		matchingAdvs.add(adv);
	}

	public static void resetCounter() {
		subCounter = 0;
	}

	public NodeInfo getSubscriberNode() {
		return subscriberNode;
	}

	public void setSubscriberNode(NodeInfo subscriberNode) {
		this.subscriberNode = subscriberNode;
	}
}
