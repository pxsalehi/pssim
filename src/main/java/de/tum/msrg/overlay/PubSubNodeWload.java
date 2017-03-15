package de.tum.msrg.overlay;

import java.util.ArrayList;
import java.util.List;

import de.tum.msrg.message.Advertisement;
import de.tum.msrg.message.Publication;
import de.tum.msrg.message.Subscription;
import de.tum.msrg.topology.NodeInfo;

public class PubSubNodeWload {
    private NodeInfo node;
    private int[] subClassCount = new int[Subscription.getTotalClasses()];
    private List<Subscription> subs = new ArrayList<Subscription>();
    private List<Advertisement> advs = new ArrayList<Advertisement>();
    private List<Publication> pubs = new ArrayList<Publication>();
    private long pubRate = 0;

    public PubSubNodeWload(NodeInfo nodeInfo) {
        this.node = nodeInfo;
    }

    public NodeInfo getNode() {
        return node;
    }

    public void setNode(NodeInfo node) {
        this.node = node;
    }

    public PubSubNodeWload addSub(Subscription sub) {
        subs.add(sub);
        if (subClassCount == null) {
            subClassCount = new int[Subscription.getTotalClasses()];
        }
        subClassCount[sub.getSubClass()]++;
        return this;
    }

    public int getNoOfSubs() {
        return subs.size();
    }

    public PubSubNodeWload addAdv(Advertisement adv) {
        advs.add(adv);
        adv.setSourceId(node.getId());
        return this;
    }

    public int getNoOfAdvs() {
        return advs.size();
    }

    public PubSubNodeWload addPub(Publication pub) {
        pubs.add(pub);
        pub.setSourceBroker(node);
        return this;
    }

    public void addPubs(List<Publication> pubs) {
        pubs.addAll(pubs);
    }

    public int getNoOfPubs() {
        return pubs.size();
    }

    @Override
    public String toString() {
        return "pub_rate=" + pubRate + "\n" + "advs: " + advs + "\n" + "subs: " +
                subs + "\n" + "pubs: " + pubs + "\n";
    }

    public void printAllDetails() {
        System.out.println("pref and counts");
        System.out.printf("\tnoOfSubClasses: %d, subs: %d, advs: %d, pubs: %d, pubRate: %d\n",
                          getSubClassCount().length, getSubs().size(),
                          getAdvs().size(), getPubs().size(), getPubRate());
        System.out.println("Subscriptions");
        for (Subscription s : getSubs())
            System.out.printf("\t%s\n", s.toString());
        System.out.println("Advertisements");
        for (Advertisement a : getAdvs())
            System.out.printf("\t%s\n", a.toString());
        System.out.println("Publications");
        for (Publication p : getPubs())
            System.out.printf("\t%s\n", p.toString());
    }

    public int[] getSubClassCount() {
        return this.subClassCount;
    }

    public List<Subscription> getSubs() {
        return this.subs;
    }

    public List<Advertisement> getAdvs() {
        return this.advs;
    }

    public List<Publication> getPubs() {
        return this.pubs;
    }

    public long getPubRate() {
        return pubRate;
    }

    public void setPubRate(long pubRate) {
        this.pubRate = pubRate;
    }

    public int getDominantSubClass() {
        int domClass = -1;
        int maxCount = 0;
        int[] subClassCount = getSubClassCount();
        for (int i = 0; i < subClassCount.length; i++) {
            if (subClassCount[i] > maxCount) {
                domClass = i;
                maxCount = subClassCount[i];
            }
        }
        return domClass;
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PubSubNodeWload other = (PubSubNodeWload) obj;
        if (node == null) {
            if (other.node != null)
                return false;
        } else if (!node.equals(other.node))
            return false;
        return true;
    }

}