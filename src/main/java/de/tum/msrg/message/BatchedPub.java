package de.tum.msrg.message;

/**
 * Created by pxsalehi on 20.02.17.
 */
public class BatchedPub {
    public long batchLatency;
    public long batchStartTime;
    public Publication pub;

    public BatchedPub(Publication pub, long batchStartTime) {
        this.pub = pub;
        this.batchStartTime = batchStartTime;
        batchLatency = 0;
    }
}
