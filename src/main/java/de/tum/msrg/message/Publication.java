package de.tum.msrg.message;

import java.util.*;

import de.tum.msrg.overlay.BrokerBase;
import de.tum.msrg.topology.NodeInfo;

public class Publication {

	protected static int pubCounter = 0;
	protected int id;
	protected Advertisement matchingAdv;
	protected int pubClass;
	private float[] attributes;
	protected Set<NodeInfo> subscribers;
	protected Set<Integer> matchingSubscriptions;
	// record subscribers that have already received this pub
	protected Set<NodeInfo> deliveredSubs = new HashSet<NodeInfo>();
	protected long publicationTime;
	protected NodeInfo sourceBroker;

	public Publication() {
		id = pubCounter;
		pubCounter++;
	}

	public Publication(float[] attributes) {
		this();
		this.attributes = attributes;

	}
	
	public Publication(int pubClass, float[] attributes) {
		this(attributes);
		setPubClass(pubClass);
	}

	public Publication(Publication p) {
		this(p.getPubClass(), p.getAttributes());
		sourceBroker = p.getSourceBroker();
	}

	/**
	 * Copies all fields, even the matching data
     */
	public Publication clonePub() {
		Publication clone = new Publication(this);
		clone.matchingAdv = matchingAdv;
//		if(subscribers != null)
//			clone.setSubscribers(new ArrayList<NodeInfo>(subscribers));
		clone.subscribers = subscribers;
//		if(matchingSubscriptions != null)
//			clone.setMatchingSubscriptions(new ArrayList<Integer>(matchingSubscriptions));
		clone.matchingSubscriptions = matchingSubscriptions;
		clone.publicationTime = publicationTime;
		clone.deliveredSubs = deliveredSubs;
		return clone;
	}

	public Advertisement getMatchingAdv() {
		return matchingAdv;
	}

	public int getMatchingAdvId() {
		if(matchingAdv == null)
			throw new RuntimeException("Publication " + this + " does not have a matching adv!");
		return matchingAdv.getID();
	}

	public void setMatchingAdv(Advertisement matchingAdv) {
		this.matchingAdv = matchingAdv;
	}

	public int getId() {
		return id;
	}

	public int getPubClass() {
		return pubClass;
	}

	public void setPubClass(int pubClass) {
		this.pubClass = pubClass;
	}

	public float[] getAttributes() {
		return attributes;
	}

	public void setAttributes(float[] attributes) {
		this.attributes = attributes;
	}

	public void setSubscribers(Set<NodeInfo> targetNodes) {
		subscribers = targetNodes;
	}
	
	public void setMatchingSubscriptions(Set<Integer> subIds) {
		matchingSubscriptions = subIds;
	}
	
	public Set<Integer> getMatchingSubscriptions() {
		return matchingSubscriptions;
	}

	public Set<NodeInfo> getSubscribers() {
		if (subscribers == null) {
			throw new RuntimeException("Subscriber set unset for pub" + id);
		}
		return subscribers;
	}

	public String toString() {
		String outStr = String.format("id:%d (adv:%d) (class:%d)  ", id, getMatchingAdvId(), pubClass);
		for (int i = 0; i < attributes.length; i++) {
			outStr += String.format("[%d,%5.2f]", i, attributes[i]);
		}
		outStr += "  subscribers:{";
		if(subscribers != null)
			for (NodeInfo node : subscribers) {
				outStr += String.format("%d,", node.getId());
			}
		outStr += "}";
		return outStr;
	}

	public void setPublicationTime(long time) {
		publicationTime = time;
	}
	
	public long getPublicationTime() {
		return publicationTime;
	}
	
	public NodeInfo getSourceBroker() {
		return sourceBroker;
	}
	
	public void setSourceBroker(NodeInfo node) {
		this.sourceBroker = node;
	}

	class PublicationTimeComparator implements Comparator<Publication> {
		public int compare(Publication p1, Publication p2) {
			long p1Time = p1.getPublicationTime();
			long p2Time = p2.getPublicationTime();
			if (p1Time > p2Time) {
				return 1;
			} else if (p1Time < p2Time) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	public static void resetCounter() {
		pubCounter = 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Publication that = (Publication) o;

		if (id != that.id) return false;
		if (pubClass != that.pubClass) return false;
		return matchingAdv.equals(that.matchingAdv);

	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + matchingAdv.hashCode();
		result = 31 * result + pubClass;
		return result;
	}

	public Set<NodeInfo> getDeliveredSubs() {
		return deliveredSubs;
	}

	public void updateDeliveredSubs(NodeInfo s) {
		deliveredSubs.add(s);
	}

	public void updateDeliveredSubs(BrokerBase s) {
		deliveredSubs.add(s.getNodeInfo());
	}

	public boolean matches(NodeInfo sub) {
		return subscribers.contains(sub);
	}

	public boolean isDeliveredTo(NodeInfo sub) {
		return deliveredSubs.contains(sub);
	}

	public boolean matches(BrokerBase sub) {
		return subscribers.contains(sub.getNodeInfo());
	}

	public boolean isDeliveredTo(BrokerBase sub) {
		return deliveredSubs.contains(sub.getNodeInfo());
	}
}
