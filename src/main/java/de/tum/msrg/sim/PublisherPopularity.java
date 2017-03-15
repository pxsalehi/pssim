package de.tum.msrg.sim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by pxsalehi on 09.02.17.
 */
public class PublisherPopularity {
    enum POPULARITY_TYPE {
        NORMAL, UNIFORM, ZIPF, POISSON, FILE
    }

    public final String popsFiles = "pops_out/pops%d";

    private int noOfPublishers;
    private int noOfSubscribers;
    private POPULARITY_TYPE popType;
    private DistSampler.DistType popDist;
    private double zipfSkew;
    private long seed;
    private boolean minOneSubPerPub;
    private int[] subsPerPublishers;
    private DistSampler distSampler;

    public PublisherPopularity(int noOfPublishers, int noOfSubscribers, POPULARITY_TYPE popType,
                               long seed, boolean minOneSubperPub, double zipfSkew) {
        this.noOfPublishers = noOfPublishers;
        this.noOfSubscribers = noOfSubscribers;
        this.popType = popType;
        if (popType != POPULARITY_TYPE.FILE) {
            popDist = DistSampler.DistType.valueOf(popType.name());
            distSampler = new DistSampler(popDist, seed, noOfPublishers, zipfSkew);
        }
        this.seed = seed;
        this.minOneSubPerPub = minOneSubperPub;
        this.zipfSkew = zipfSkew;
        if (minOneSubperPub && noOfSubscribers < noOfPublishers) {
            throw new RuntimeException("Min of one sub per pub cannot be satisfied!");
        }
        try {
            if (popType == POPULARITY_TYPE.FILE)
                subsPerPublishers = read(String.format(popsFiles, noOfPublishers));
            else
                subsPerPublishers = generate();
        } catch (IOException e) {
            throw new RuntimeException("Cannot calculate popularities!", e);
        }


    }

    public int[] generate() {
        int[] pops = new int[noOfPublishers];
        int subsLeft = noOfSubscribers;
        if (minOneSubPerPub) {
            for (int i = 0; i < noOfPublishers; i++)
                pops[i] = 1;
            subsLeft -= noOfPublishers;
        }
        for (int i = 0; i < subsLeft; i++) {
            int p = distSampler.sample();
            pops[p] += 1;
        }
        return pops;
    }

    public int[] read(String filename) throws IOException {
        int[] pops = new int[noOfPublishers];
        BufferedReader file = new BufferedReader(new FileReader(filename));
        for (int i = 0; i < noOfPublishers; i++) {
            String[] toks = file.readLine().split("\\s+");
            double subs = Double.parseDouble(toks[1]) * noOfSubscribers;
            if (minOneSubPerPub)
                subs = Math.ceil(subs);
            pops[Integer.parseInt(toks[0])] = (int)subs;
        }
        return pops;
    }

    @Override
    public String toString() {
        return Arrays.toString(subsPerPublishers);
    }

    public int[] getSubsPerPublishers() {
        return subsPerPublishers;
    }

    public int getSubsPerPublisher(int id) {
        return subsPerPublishers[id];
    }

    public static void main(String[] args) {
        POPULARITY_TYPE pt = POPULARITY_TYPE.ZIPF;
        DistSampler.DistType dt = DistSampler.DistType.valueOf(pt.name());
        System.out.println(dt);
    }
}
