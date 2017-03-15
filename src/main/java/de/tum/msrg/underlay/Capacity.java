package de.tum.msrg.underlay;

/**
 * Created by pxsalehi on 04.03.16.
 *
 * A general capacity monitor to be used both for brokers and links
 * provides hooks to be updated when a capacity filler is added or removed
 * and whether the new arrival should be queued or not and if so for how long
 *
 * An entity with a bounded capacity can process N elements in T time units
 * Each new element arriving when capacity is full will experience T/N * Q
 * delay where Q is the length of the queue
 *
 */
public class Capacity {
    public interface CapacitySimInterface {
        void add(long time);
    }

    public static class Service {
        public long start = -1;
        public long end = -1;

        public Service(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    private Service[] beingServed;
    private int size;
    private int interval;
    private static int maxQueueSize = 100;
    private Service[] queued;

    public Capacity(int size, int interval) {
        this.size = size;
        this.interval = interval;
        //beingServed = new Service[size];
    }
}
