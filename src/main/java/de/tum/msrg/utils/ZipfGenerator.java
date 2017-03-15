package de.tum.msrg.utils;

import java.util.*;

import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

// slightly modified from http://diveintodata.org/2009/09/zipf-distribution-generator-in-java/

public class ZipfGenerator {
	private Random rnd;
	private int size;
	private double skew;
	private double bottom = 0;

	public ZipfGenerator(int size, double skew, long seed) {
		this.size = size;
		this.skew = skew;
		rnd = new Random(seed);

		for (int i = 1; i < size; i++) {
			this.bottom += (1 / Math.pow(i, this.skew));
		}
	}

	// the next() method returns an rank id. The frequency of returned rank ids
	// are follows Zipf distribution.
	public int next() {
		int rank;
		double friquency = 0;
		double dice;

		rank = rnd.nextInt(size);
		friquency = (1.0d / Math.pow(rank, this.skew)) / this.bottom;
		dice = rnd.nextDouble();

		while (!(dice < friquency)) {
			rank = rnd.nextInt(size);
			friquency = (1.0d / Math.pow(rank, this.skew)) / this.bottom;
			dice = rnd.nextDouble();
		}

		return rank;
	}

	// This method returns a probability that the given rank occurs.
	public double getProbability(int rank) {
		return (1.0d / Math.pow(rank, this.skew)) / this.bottom;
	}

	// TODO: make sure zipf generator is correct, maybe use apache commons
	public static void main(String[] args) {
		int seed = 3258747;
		int samples = 1000;
		int elements = 100;
		float exponent = 1f;
		HashMap<Integer, Integer> popularityPerElement = new HashMap<Integer, Integer>();
		for(int elm = 0; elm < elements; ++elm)
			popularityPerElement.put(elm, 0);
		ZipfGenerator zipf = new ZipfGenerator(elements, exponent, seed);
		for(int i = 0; i < samples; i++) {
			int elm = zipf.next();
			int pop = popularityPerElement.get(elm);
			popularityPerElement.put(elm, pop + 1);
		}
		List<Integer> popularitys = new ArrayList<Integer>(popularityPerElement.values());
		Collections.sort(popularitys, Collections.reverseOrder());
		System.out.println(Arrays.toString(popularitys.toArray()));
//		for(int elm = 1; elm <= elements; ++elm)
//			System.out.println(popularityPerElement.get(elm));

		// Apache common math
		popularityPerElement = new HashMap<Integer, Integer>();
		for(int elm = 0; elm < elements; ++elm)
			popularityPerElement.put(elm, 0);
		RandomGenerator randGen = new JDKRandomGenerator();
		randGen.setSeed(seed);
		ZipfDistribution zipfDist = new ZipfDistribution(randGen, elements, exponent);
		for(int i = 0; i < samples; i++) {
			int elm = zipfDist.sample();
			int pop = popularityPerElement.get(elm-1);
			popularityPerElement.put(elm-1, pop + 1);
		}
		System.out.println("************ APACHE **************");
		popularitys = new ArrayList<Integer>(popularityPerElement.values());
		Collections.sort(popularitys, Collections.reverseOrder());
		System.out.println(Arrays.toString(popularitys.toArray()));
//		for(int elm = 0; elm < elements; ++elm)
//			System.out.println(popularityPerElement.get(elm));
	}
}
