package de.tum.msrg.pubsub;

import de.tum.msrg.utils.Range;

/**
 * Created by pxsalehi on 20.04.16.
 */
public interface Failable {
    void addFailTrace(long start, long end);
    boolean isDown();
    boolean isDown(Range period);
}
