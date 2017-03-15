package de.tum.msrg.sim;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Created by pxsalehi on 10.02.17.
 */
public class DistSampler {
    public enum DistType {
        NORMAL, UNIFORM, ZIPF, POISSON
    }

    private DistType dist;
    private ZipfDistribution classPopZipfDist;
    private UniformIntegerDistribution classPopUniformDist;
    private PoissonDistribution classPopPoissonDist;
    private NormalDistribution classPopNormalDist;
    private RandomGenerator randGen = new JDKRandomGenerator();
    private long seed;
    private int maxVal;

    public DistSampler(DistType dist, long seed, int max, double zipfSkew) {
        this.dist = dist;
        this.seed = seed;
        randGen.setSeed(seed);
        this.maxVal = max;
        classPopZipfDist = new ZipfDistribution(randGen, max, zipfSkew);
        classPopUniformDist = new UniformIntegerDistribution(randGen, 0, max);
        classPopPoissonDist = new PoissonDistribution(
                randGen, max/4, PoissonDistribution.DEFAULT_EPSILON,
                PoissonDistribution.DEFAULT_MAX_ITERATIONS);
        classPopNormalDist = new NormalDistribution(max/2.0, max/6.0);
    }

    public int sample() {
        int c;
        switch (dist) {
            case ZIPF:
                do {
                    c = classPopZipfDist.sample() - 1;
                } while (c < 0 || c >= maxVal);
                return c;
            case POISSON:
                do {
                    c = classPopPoissonDist.sample() - 1;
                } while(c < 0 || c >= maxVal);
                return c;
            case UNIFORM:
                do {
                    c = classPopUniformDist.sample() - 1;
                } while(c < 0 || c >= maxVal);
                return c;
            case NORMAL:
                do {
                    c = (int) classPopNormalDist.sample() - 1;
                } while(c < 0 || c >= maxVal);
                return c;
            default:
                throw new RuntimeException("Unsupported distribution " + dist);
        }
    }
}
