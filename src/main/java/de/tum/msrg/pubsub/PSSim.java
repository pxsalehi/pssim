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
	private String topicStatsFile;
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
		topicStatsFile = FileUtils.joinFilePaths(simDir, "topicstats" + seed + ".txt");
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
		// write topic stats
		BufferedWriter topicStatsOut = null;
		try {
			topicStatsOut = new BufferedWriter(new FileWriter(topicStatsFile));
			int[] pop = StatsCollector.getInstance().getPopularityPerTopic();
			int[] deliv = StatsCollector.getInstance().getDeliveryPerTopic();
			topicStatsOut.write("topic \t pop \t deliv\n");
			long totalTreeDeliv = StatsCollector.getInstance().getTotalDeliveredPubCount() - StatsCollector.getInstance().getTotalPubsDeliveredViaGossip();
			double delivCDF = 0.;
			for(int c = 0; c < noOfTopics; ++c) {
				double topicDelivPerc = (double)deliv[c] / totalTreeDeliv;
				delivCDF += topicDelivPerc;
				topicStatsOut.write(c + "\t" + pop[c] + "\t" + delivCDF + '\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(topicStatsOut != null)
					topicStatsOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
		return  "Number of broker: " + overlay.getNoOfNodes() +
				"\nNumber of subscribers : " + StatsCollector.getInstance().getNoOfSubscribers() +
				"\nWorkload distribution: "
				+ config.getStringConfig(ConfigKeys.WLOAD_PUBLISHER_POPULARITY) +
				"\nZipf skewness: " + config.getFloatConfig(ConfigKeys.WLOAD_ZIPF_EXPONENT) +
				"\nPub generated : " + StatsCollector.getInstance().getTotalGeneratedPubCount() +
				"\nPub published : " + StatsCollector.getInstance().getTotalPublishedPubCount() +
				"\nTotal pubs sent     : " + StatsCollector.getInstance().getTotalSentPubCount() +
				"\nGossips sent  : " + StatsCollector.getInstance().getTotalSentGossipCount() +
				"\nGossips sent before last delivery : "
				+ StatsCollector.getInstance().getTotalGossipsSentsBeforeLastDelivery() +
				"\nTotal messages sent   : " + StatsCollector.getInstance().getTotalMsgsSent() +
				"\nTotal packets sent    : "
				+ StatsCollector.getInstance().getTotalSentPacketCount() +
				"\nPackets sent per broker 99th: "
				+ StatsCollector.getInstance().getPacketSentCountPerBrokerPercentile(0.99) +
				"\nPackets sent per broker avg: "
				+ StatsCollector.getInstance().getPacketSentCountPerBrokerAverage() +
				"\nNo Of publisher brokers: " + StatsCollector.getInstance().getNoOfPublisherBrokers() +
				"\nPackets sent per publisher avg: "
				+ StatsCollector.getInstance().getAveragePacketSentCountPerPublisher() +
				"\nGossips received: " + StatsCollector.getInstance().getTotalReceivedGossipCount() +
				"\nTotal pubs delivered : "
				+ StatsCollector.getInstance().getTotalDeliveredPubCount() +
				"\nPercentage of local pub deliveries: "
				+ StatsCollector.getInstance().getPercentageOfLocalDelivery() +
				"\nPubs delivered via gossip: "
				+ StatsCollector.getInstance().getTotalPubsDeliveredViaGossip() +
				"\nPubs delivered via direct link: "
				+ StatsCollector.getInstance().getTotalPubsDeliveredViaDirectLink() +
				"\nPubs delivered via batching: "
				+ StatsCollector.getInstance().getTotalPubsDeliveredViaBatch() +
				"\nGossip delivery false positive rate: "
				+ StatsCollector.getInstance().getGossipDeliveryFalsePositiveRate() +
				"\nTree delivery latency  99th: "
				+ StatsCollector.getInstance().get99thTreeDeliveryLatency() +
				"\nTree delivery hopcount  99th: "
				+ StatsCollector.getInstance().get99thTreeDeliveryHopcount() +
				"\nGossip delivery latency 99th: "
				+ StatsCollector.getInstance().getGossipDeliveryLatencyPercentile(0.99) +
				"\nGossip delivery latency 90th: "
				+ StatsCollector.getInstance().getGossipDeliveryLatencyPercentile(0.90) +
				"\nGossip delivery hopcount 99th: "
				+ StatsCollector.getInstance().get99thGossipDeliveryHopcount() +
				"\nBatch delivery latency 99th: "
				+ StatsCollector.getInstance().getBatchDeliveryLatencyPercentile(0.99) +
				"\nBatch delivery latency 90th: "
				+ StatsCollector.getInstance().getBatchDeliveryLatencyPercentile(0.90) +
				"\nBatch delivery hopcount 99th: "
				+ StatsCollector.getInstance().get99thBatchDeliveryHopcount() +
				"\ndirect link delivery latency 99th: "
				+ StatsCollector.getInstance().getDirectLinkDeliveryLatencyPercentile(0.99) +
				"\ndirect link delivery latency 90th: "
				+ StatsCollector.getInstance().getDirectLinkDeliveryLatencyPercentile(0.90) +
				"\ndirect link delivery hopcount 99th: "
				+ StatsCollector.getInstance().get99thDirectLinkDeliveryHopcount() +
				"\nTotal delivery latency  99th: "
				+ StatsCollector.getInstance().get99thTotalDeliveryLatency() +
				"\nTotal delivery hopcount  99th: "
				+ StatsCollector.getInstance().get99thTotalDeliveryHopcount() +
				"\nAverage match count: " + StatsCollector.getInstance().getAverageUtilRatio().x +
				"\nAverage avg path length: " + StatsCollector.getInstance().getAverageUtilRatio().y +
				"\nAverage gossip per broker: "
				+ StatsCollector.getInstance().getAvgGossipBeforeLastDeliveryPerBroker() +
				"\nNumber of gossip groups: " + StatsCollector.getInstance().getNoOfGossipGroups() +
				"\nAverage queue size: " + StatsCollector.getInstance().getAverageQueueSize() +
				"\n99th max queue size: " + StatsCollector.getInstance().get99thQueueSize() +
				"\nPerc. of direct out of tree delivery per publisher 99th: "
				+ StatsCollector.getInstance().get99thDirectDeliveryPerPublisher() +
				"\nNumber of direct ueberlinks 99th: "
				+ StatsCollector.getInstance().get99thMsgsSentForEachPublishPerPublisher() +
				"\nLast delivery : " + StatsCollector.getInstance().getLastPubDeliveryTime() +
				"\nDelivery rate : " + StatsCollector.getInstance().getPubDeliveryRate() +
				"\nPubs sent via gossip: "
				+ StatsCollector.getInstance().getTotalPubsSentViaGossip() +
				"\nPubs sent via batching: "
				+ StatsCollector.getInstance().getTotalPubsSentViaBatching() +
				"\nPubs sent via direct link: "
				+ StatsCollector.getInstance().getTotalPubsSentViaDirectLink() +
				"\nUnsuccessful retrieve from B0(root): "
				+ StatsCollector.getInstance().getUnsuccessfulRetrieveCount(0) +
				"\nAverage pub match per received gossip: "
				+ StatsCollector.getInstance().getAverageMatchCountPerGossip() +
				"\nAverage pub gain: " + StatsCollector.getInstance().getAveragePubGain() +
				"\n99th perc. pub gain: " + StatsCollector.getInstance().getPercentilePubGain(0.99) +
				"\n50th perc. pub gain: " + StatsCollector.getInstance().getPercentilePubGain(0.50) +
				"\nAverage throughput: " + StatsCollector.getInstance().getAverageThroughput() +
				"\nTotal pure forward msg count: "
				+ StatsCollector.getInstance().getTotalPureForwardCount();
	}
}