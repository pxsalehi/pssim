package de.tum.msrg.sim;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Level;

import de.tum.msrg.baseline.ConfigKeys;
import de.tum.msrg.config.ConfigParserException;
import de.tum.msrg.config.Configuration;
import de.tum.msrg.utils.FileUtils;
import de.tum.msrg.utils.SimLogger;

public class TestSim {

	public static void main(String[] args) throws FileNotFoundException, IOException, ConfigParserException {
		if(args.length < 2) {
			System.err.println("Usage: simulation sim_dir seed log_level");
			System.exit(1);
		}
		String simDir = args[0];
		long seed = Long.parseLong(args[1]);
		Level logLevel = Level.toLevel(args[2]);
		System.out.println("Running simulation with " + simDir + " seed: " + seed + " log:" + logLevel);
		SimLogger.setLevel((Level)logLevel);
		String configFilePath = FileUtils.joinFilePaths(simDir, "sim.config");
		System.out.println("Loading sim configuration from " + configFilePath);
		SimLogger.info("Loading sim configuration from " + configFilePath);
		Configuration config = new Configuration(new FileReader(configFilePath));
		config.addProperty(ConfigKeys.SIM_DIR, simDir);
		
		int[] count = new int[100];
		RandomGenerator randGen = new JDKRandomGenerator();
		randGen.setSeed(seed);
		ZipfDistribution zipfDist = new ZipfDistribution(randGen, 99, 0.99);
		Arrays.fill(count, 0);
		for(int i = 0; i < 100; i++)
			++count[zipfDist.sample()];
		System.out.println("****** APACHE ******");
		for(int i = 0; i < 100; i++)
			if(count[i] > 0)
				System.out.println(i + ": " + count[i]);
	}

}
