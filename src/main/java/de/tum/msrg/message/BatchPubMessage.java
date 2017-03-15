package de.tum.msrg.message;

import de.tum.msrg.topology.NodeInfo;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by pxsalehi on 20.01.17.
 */
public class BatchPubMessage extends Message {
    private List<BatchedPub> pubs;

    public BatchPubMessage(NodeInfo fromNode, NodeInfo toNode, Collection<BatchedPub> pubs) {
        super(Type.PUB, fromNode, toNode);
        this.pubs = new LinkedList<BatchedPub>(pubs);
    }

    public int getBatchSize() {
        return pubs.size();
    }

    public List<BatchedPub> getPubs() {
        return pubs;
    }
}
