package de.tum.msrg.pubsub;


import java.io.*;

import de.tum.msrg.overlay.OverlayLatency;
import jist.runtime.JistAPI;
import jist.runtime.JistAPI.Entity;

import org.apache.log4j.Level;

import de.tum.msrg.overlay.RuntimeSimException;
import de.tum.msrg.sim.FaultGenerator;
import de.tum.msrg.sim.StatsCollector;
import de.tum.msrg.sim.WorkloadGenerator;
import de.tum.msrg.topology.Topology;
import de.tum.msrg.utils.FileUtils;
import de.tum.msrg.utils.SimLogger;
import de.tum.msrg.config.Configuration;
import de.tum.msrg.config.ConfigParserException;

public class PSSim implements Entity {
	
	private Configuration config;
	private int noOfBrokers;
	private int noOfTopics;
	private PSOverlay overlay;
	private Topology topology;
	private WorkloadGenerator loadGen;
	private FaultGenerator faultGen;
	private long maxSimTime;
	// TODO: read and put seed into config which is accessible globally
	private long seed;
	private String faultsFile;
	private String resultFile;
	private String latenciesFile;
	private String SIM_CONF_FILE_NAME = "sim.config";
	private String TOPOLOGY_FILE_NAME = "topology.txt";
	private String FAULTS_FILE_NAME = "faults.data";
	private String LATENCIES_FILE_NAME = "latencies.txt";
	private boolean skipWorkload = false;
	
	public PSSim(String simDir, long seed) throws ConfigParserException, RuntimeSimException, IOException {
		String configFilePath = FileUtils.joinFilePaths(simDir, SIM_CONF_FILE_NAME);
		System.out.println("Loading sim configuration from " + configFilePath);
		SimLogger.info("Loading sim configuration from " + configFilePath);
		config = new Configuration(new FileReader(configFilePath));
		config.addProperty(ConfigKeys.SIM_DIR, simDir);
		if(seed == -1)
			seed = System.currentTimeMillis();
		this.seed = seed;
		maxSimTime = config.getLongConfig(ConfigKeys.SIM_MAX_SIM_TIME);
		noOfBrokers = config.getIntConfig(ConfigKeys.SIM_NO_OF_NODES);
		noOfTopics = config.getIntConfig(ConfigKeys.WLOAD_NO_OF_CLASSES);
		StatsCollector.getInstance().initialize(noOfBrokers, noOfTopics, maxSimTime);
		// TODO: what if there is already a random seed in the config
		config.addProperty(ConfigKeys.SIM_RANDOM_SEED, seed);
		System.out.println("Running with sim seed = " + seed);
		SimLogger.info("Running with sim seed = " + seed);
		// read and generate topology
		String topologyFilePath = FileUtils.joinFilePaths(simDir, TOPOLOGY_FILE_NAME);
		System.out.println("Loading topology from " + topologyFilePath);
		SimLogger.info("Loading topology from " + topologyFilePath);
		topology = new Topology(new FileReader(topologyFilePath));
		// read and create latencies
		latenciesFile = FileUtils.joinFilePaths(simDir, LATENCIES_FILE_NAME);
		System.out.println("Loading latencies from " + latenciesFile);
		SimLogger.info("Loading latencies from " + latenciesFile);
		OverlayLatency.getInstance().initialize(latenciesFile, noOfBrokers);
		faultsFile = FileUtils.joinFilePaths(simDir, FAULTS_FILE_NAME);
		resultFile = FileUtils.joinFilePaths(simDir, "stats" + seed + ".txt");
		System.out.println("Creating workload generator");
		SimLogger.info("Creating workload generator");
		loadGen = new WorkloadGenerator(config, topology, seed);
		overlay = new PSOverlay(config, topology, seed);
		faultGen = new FaultGenerator();
	}
	
	public void initSim() throws ConfigParserException, RuntimeSimException, JistAPI.Continuation {
		SimLogger.info("Initializing overlay...");
		overlay.initOverlay();
		SimLogger.info("Generating workload...");
		loadGen.generate();
		loadGen.loadOnOverlay(overlay, loadGen.getWorkload());
		if(config.getBooleanConfig(ConfigKeys.WLOAD_LOAD_FAULTS)) {
			System.out.println("Loading faults from " + faultsFile);
			SimLogger.info("Loading faults from " + faultsFile);
			faultGen.loadFromFile(overlay, faultsFile);
		} else
			System.out.println("Skipping fault file...");
	}
	
	public void runSimulation() throws ConfigParserException, RuntimeSimException, JistAPI.Continuation {
		System.out.println("Starting simulation ...");
		for(PSBroker b: overlay.getNodes()) {
			 if(!b.getFailTrace().isEmpty())
				 SimLogger.info(b + "'s scheduled faults:\n" + b.getFailTrace());
		}
		overlay.getSimInterface().startSim();
		JistAPI.sleep(maxSimTime);
	}

	public void writeResults() {
		String stats = getStatsAsString();
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(resultFile));
			out.write(stats);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// write results to screen too
		System.out.println("******************************************************");
		System.out.println(stats);
		System.out.println("******************************************************");
	}


	public static void main(String[] args) throws ConfigParserException, RuntimeSimException, IOException, InterruptedException {
		if(args.length < 3) {
			System.err.println("Usage: simulation config_file seed log_level");
			System.exit(1);
		}
		String config = args[0];
		long seed = Long.parseLong(args[1]);
		Level logLevel = Level.toLevel(args[2]);
		System.out.println("Running baseline simulation with " + config + " seed=" + seed + " log=" + logLevel);
		SimLogger.setLevel(logLevel);
		PSSim sim = new PSSim(config, seed);
		sim.initSim();
		sim.runSimulation();
		sim.writeResults();
	}

	private String getStatsAsString() {
		return  "Number of broker: "
				+ overlay.getNoOfNodes() +
				"\nNumber of subscribers : "
				+ StatsCollector.getInstance().getNoOfSubscribers() +
				"\nWorkload distribution: "
				+ config.getStringConfig(ConfigKeys.WLOAD_PUBLISHER_POPULARITY) +
				"\nZipf skewness: "
				+ config.getFloatConfig(ConfigKeys.WLOAD_ZIPF_EXPONENT) +
				"\nPub generated : "
				+ StatsCollector.getInstance().getTotalGeneratedPubCount() +
				"\nPub published : "
				+ StatsCollector.getInstance().getTotalPublishedPubCount() +
				"\nTotal pubs sent     : "
				+ StatsCollector.getInstance().getTotalSentPubCount() +
				"\nTotal messages sent   : "
				+ StatsCollector.getInstance().getTotalMsgsSent() +
				"\nTotal packets sent    : "
				+ StatsCollector.getInstance().getTotalSentPacketCount() +
				"\nPackets sent per broker 99th: "
				+ StatsCollector.getInstance().getPacketSentCountPerBrokerPercentile(0.99) +
				"\nPackets sent per broker avg: "
				+ StatsCollector.getInstance().getPacketSentCountPerBrokerAverage() +
				"\nNo Of publisher brokers: " + StatsCollector.getInstance().getNoOfPublisherBrokers() +
				"\nPackets sent per publisher avg: "
				+ StatsCollector.getInstance().getAveragePacketSentCountPerPublisher() +
				"\nTotal pubs delivered : "
				+ StatsCollector.getInstance().getTotalDeliveredPubCount() +
				"\nPercentage of local pub deliveries: "
				+ StatsCollector.getInstance().getPercentageOfLocalDelivery() +
				"\nTotal delivery latency  99th: "
				+ StatsCollector.getInstance().get99thTotalDeliveryLatency() +
				"\nTotal delivery hopcount  99th: "
				+ StatsCollector.getInstance().get99thTotalDeliveryHopcount() +
				"\nAverage match count: "
				+ StatsCollector.getInstance().getAverageUtilRatio().x +
				"\nAverage queue size: "
				+ StatsCollector.getInstance().getAverageQueueSize() +
				"\n99th max queue size: "
				+ StatsCollector.getInstance().get99thQueueSize() +
				"\nLast delivery : "
				+ StatsCollector.getInstance().getLastPubDeliveryTime() +
				"\nDelivery rate : "
				+ StatsCollector.getInstance().getPubDeliveryRate() +
				"\nAverage throughput: "
				+ StatsCollector.getInstance().getAverageThroughput() +
				"\nTotal pure forward msg count: "
				+ StatsCollector.getInstance().getTotalPureForwardCount();
	}
}
