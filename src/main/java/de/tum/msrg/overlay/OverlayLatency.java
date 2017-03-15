package de.tum.msrg.overlay;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * Represents the out-of-band connection latency of the overlay
 * If two brokers want to communicate via links that are not part of the topology,
 * this object can be used to compute the latency of such communications
 * Here gossip is used to describe any out-of-band communication
 */
public class OverlayLatency {
    private static OverlayLatency instance = new OverlayLatency();

    public static OverlayLatency getInstance() {
        return instance;
    }

    private OverlayLatency() {}

    private int[][] latencies;
    private String filename;
    private int noOfNodes;

    public void initialize(String file, int noOfNodes) throws IOException {
        this.noOfNodes = noOfNodes;
        this.filename = file;
        latencies = read(file, noOfNodes);

    }

    private int[][] read(String filename, int noOfNodes) throws IOException {
        int[][] latencies = new int[noOfNodes][noOfNodes];
        BufferedReader file = new BufferedReader(new FileReader(filename));
        for (int i = 0; i < noOfNodes; i++) {
            String line = file.readLine();
            String[] lats = line.split("\\s+");
            for (int j = 0; j < noOfNodes; j++)
                latencies[i][j] = Integer.parseInt(lats[j]);
        }
        return latencies;
    }

    public int get(BrokerBase from, BrokerBase to) {
        return get(from.getId(), to.getId());
    }

    public int get(int fromId, int toId) {
        return latencies[fromId][toId];
    }

}
