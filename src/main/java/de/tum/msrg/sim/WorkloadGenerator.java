package de.tum.msrg.sim;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.*;

import de.tum.msrg.baseline.ConfigKeys;
import de.tum.msrg.config.ConfigParserException;
import de.tum.msrg.config.Configuration;
import de.tum.msrg.message.*;
import de.tum.msrg.overlay.OverlayBase;
import de.tum.msrg.overlay.RuntimeSimException;
import de.tum.msrg.overlay.BrokerBase;
import de.tum.msrg.overlay.PubSubNodeWload;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.topology.Topology;
import de.tum.msrg.utils.SimLogger;
import de.tum.msrg.utils.ZipfGenerator;
import de.tum.msrg.sim.DistSampler.DistType;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

public class WorkloadGenerator {
	// which nodes publish/subscriber
	public enum ClientNodeType {
		ALL, 
		LEAVES,	// only leaf nodes
		ROOT_AND_CHILDREN, // root and its children
		LIST	// a specified list of broker ids
	}
	
	// simulation parameters
	private int noOfSubs;
	private int noOfAdvs;
	private int pubDuration;
	private int noOfClasses;
	/**
	 * The bias factor to be used when distributing subscriptions and advertisements to brokers. 
	 * The value must be in the range [0,100]. A value of 0 implies none of the subscriptions and 
	 * advertisements assigned to a broker will be of its preference class. A value of 100 implies 
	 * all local subscriptions and advertisements will be of the preference class.
	 */
	private short biasFactor = 0;
	private DistType subDistOverNodes;
	private DistType advDistOverNodes;
	private int msgSize;
	private Random rand;
	long seed;
	private DistType attDistOverSubs;
	private long pubRate;
	private ClientNodeType pubNodeType;
	private ClientNodeType subNodeType;
	private List<Integer> pubNodesList;
	private List<Integer> subNodesList;
	private List<PubSubNodeWload> pubNodes = new LinkedList<PubSubNodeWload>();
	private List<PubSubNodeWload> subNodes = new LinkedList<PubSubNodeWload>();
	private List<PubSubNodeWload> allNodes = new LinkedList<PubSubNodeWload>();
	private List<Advertisement> allAdvs;
	private ZipfGenerator zipfGen;
	private float zipfSkew; // exponent
	private AttributeGenerator attributeGenerator;
	RandomGenerator randomGen = new JDKRandomGenerator();
	private Map<Advertisement, List<Subscription>> subsPerAdv =
			new HashMap<Advertisement, List<Subscription>>();

	private PublisherPopularity publisherPopularity;
	private PublisherPopularity.POPULARITY_TYPE publisherPopType;


	public WorkloadGenerator(Configuration config, Topology topology, long seed) throws ConfigParserException {
		readConfig(config);
		Subscription.setTotalClasses(noOfClasses);
		this.seed = seed;
		for(int n = 0; n < topology.getNoOfNodes(); ++n)
			allNodes.add(new PubSubNodeWload(topology.getNode(n)));
		pubNodes = getClientNodes(topology, pubNodeType, pubNodesList);
		subNodes = getClientNodes(topology, subNodeType, subNodesList);
		zipfGen = new ZipfGenerator((int) Attribute.highRange, zipfSkew, seed);
		this.rand = new Random(seed);
		randomGen.setSeed(seed);
		initialize();
	}
	
	private void readConfig(Configuration config) throws ConfigParserException {
		zipfSkew = config.getFloatConfig(ConfigKeys.WLOAD_ZIPF_EXPONENT);
		noOfSubs = config.getIntConfig(ConfigKeys.SIM_NO_OF_SUBS);
		noOfAdvs = config.getIntConfig(ConfigKeys.SIM_NO_OF_ADVS);
		pubDuration = config.getIntConfig(ConfigKeys.SIM_PUB_DURATION);
		pubRate = config.getIntConfig(ConfigKeys.SIM_PUB_RATE);
		msgSize = config.getIntConfig(ConfigKeys.WLOAD_MSG_SIZE);
		Message.MSG_SIZE = msgSize;
		attDistOverSubs = config.getEnumConfig(DistType.class, ConfigKeys.WLOAD_ATTRIBUTE_VALUE_DIST);
		subDistOverNodes = config.getEnumConfig(DistType.class, ConfigKeys.WLOAD_SUB_OVER_NODES_DIST);
		advDistOverNodes = config.getEnumConfig(DistType.class, ConfigKeys.WLOAD_ADV_OVER_NODES_DIST);
		noOfClasses = config.getIntConfig(ConfigKeys.WLOAD_NO_OF_CLASSES);
		publisherPopType = config.getEnumConfig(PublisherPopularity.POPULARITY_TYPE.class,
												ConfigKeys.WLOAD_PUBLISHER_POPULARITY);
		Attribute.noOfAttributes = config.getIntConfig(ConfigKeys.WLOAD_NO_OF_ATTRIBUTES);
		Attribute.lowRange = config.getFloatConfig(ConfigKeys.WLOAD_ATTRIBUTE_MIN_VALUE);
		Attribute.highRange = config.getFloatConfig(ConfigKeys.WLOAD_ATTRIBUTE_MAX_VALUE);
		pubNodeType = config.getEnumConfig(ClientNodeType.class, ConfigKeys.WLOAD_PUB_NODES);
		if(pubNodeType == ClientNodeType.LIST)
			pubNodesList = config.getList(Integer.class, ConfigKeys.WLOAD_PUB_NODES_LIST);
		subNodeType = config.getEnumConfig(ClientNodeType.class, ConfigKeys.WLOAD_SUB_NODES);
		if(subNodeType == ClientNodeType.LIST)
			subNodesList = config.getList(Integer.class, ConfigKeys.WLOAD_SUB_NODES_LIST);
	}

	private void initialize() {
		attributeGenerator = new AttributeGenerator(rand, zipfGen, attDistOverSubs);
		publisherPopularity = new PublisherPopularity(noOfAdvs, noOfSubs, publisherPopType,
				seed, true, zipfSkew);
	}

	public <T extends BrokerBase> void loadOnOverlay(OverlayBase<T> overlay, List<PubSubNodeWload> nodeWorkloads) {
		for(PubSubNodeWload nodeWorkload: nodeWorkloads) {
			int nodeID = nodeWorkload.getNode().getId();
			overlay.getBroker(nodeID).setPubSubLoad(nodeWorkload);
		}
	}

	private List<PubSubNodeWload> getClientNodes(Topology topology, ClientNodeType clientNodeType,
												 List<Integer> nodesList) {
		List<PubSubNodeWload> clients = new LinkedList<PubSubNodeWload>();
		switch(clientNodeType) {
		case ALL:
			clients = allNodes;
			break;
		case LEAVES:
			for(NodeInfo node: topology.getNodes())
				if(node.getDegree() == 1)
					clients.add(allNodes.get(node.getId()));
			break;
		case ROOT_AND_CHILDREN:
			NodeInfo root = topology.getNode(0);
			Set<NodeInfo> children = root.getNeighbors();
			clients.add(allNodes.get(root.getId()));
			for (NodeInfo child: children)
				clients.add(allNodes.get(child.getId()));
			break;
		case LIST:
			for(Integer id: nodesList) {
				if(id > topology.getNoOfNodes())
					throw new RuntimeException("Node " + id + " doesn't exist!");
				clients.add(allNodes.get(id));
			}
			break;
		default:
			throw new RuntimeException("Invalid client node type: " + clientNodeType);
		}
		return clients;
	}
	
	public void generate() throws RuntimeSimException {
		generateAdvertisements(pubNodes);
		generateSubscriptions(subNodes);
		generatePublications(pubNodes);
//		MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
//		System.out.println("Memory usage: " + (int)(heapMemoryUsage.getUsed() / Math.pow(2, 30)));
		System.out.println("Perform matching...");
		performMatching(allNodes);
//		System.out.println("Memory usage: " + (int)(heapMemoryUsage.getUsed() / Math.pow(2, 30)));
		for(PubSubNodeWload node: allNodes)
			for(Publication pub: node.getPubs())
				StatsCollector.getInstance().pubGenerated(pub, node.getNode());
		try {
			if (SimLogger.isInfoEnabled())
				logWorkload(allNodes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void logWorkload(List<PubSubNodeWload> clientNodes) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter("workload.txt"));
		for(PubSubNodeWload n: clientNodes) {
			out.write(n.getNode().getId() + "'s advs:\n");
			for(Advertisement a: n.getAdvs())
				out.write(a + "\n");
			out.write("--------------------------\n");
			out.write(n.getNode().getId() + "'s subs:\n");
			for(Subscription s: n.getSubs()) {
				out.write(s + "\n");
				out.write("Matching advs: \n");
				for(Advertisement a: s.getMatchingAdvs())
					out.write(a.getID() + "   \n");
				out.newLine();
			}
			out.write("--------------------------\n");
			out.write(n.getNode().getId() + "'s pubs:\n");
			for(Publication p: n.getPubs())
				out.write("P" + p.getId() + ": " + p.getMatchingSubscriptions() + "\n");
			out.write("==============================\n");
		}
		out.close();
	}

	public void generateAdvertisements(List<PubSubNodeWload> clients) throws RuntimeSimException {
		allAdvs = new ArrayList<Advertisement>();
		int noOfPublishers = clients.size();
		// make sure each publisher node(broker) has at least one advertisement
		if (noOfAdvs < noOfPublishers)
			System.out.println("WARNING: Not enough advs to cover all brokers!");
		// generate all advertisements
		for (int a = 0; a < noOfAdvs; a++) {
			Attribute[] atts = attributeGenerator.generateAttributeSet();
			// select adv class randomly
			int advClass = rand.nextInt(noOfClasses);
			Advertisement newAdv = new Advertisement(advClass, atts);
			allAdvs.add(newAdv);
			int popularity = publisherPopularity.getSubsPerPublisher(a);
			subsPerAdv.put(newAdv, new ArrayList<Subscription>(popularity + 100));
		}
		// make sure each node is assigned at least one adv, if possible
		for (int i = 0; i < noOfPublishers && i < noOfAdvs; i++)
			clients.get(i).addAdv(allAdvs.get(i));
		// distribute remaining based on distribution
		DistSampler advOverNodeDist =
				new DistSampler(DistType.UNIFORM, seed, noOfPublishers, zipfSkew);
		for (int i = noOfPublishers; i < noOfAdvs; i++) {
			int node = advOverNodeDist.sample();
			clients.get(node).addAdv(allAdvs.get(i));
		}
	}

	/**
	 * generates subs with uniform bias distribution, which are distributed over nodes
	 * noOfSubs should be greater than or equal to noOfNodes
	 */
	public void generateSubscriptions(List<PubSubNodeWload> clients) throws RuntimeSimException {
		List<Subscription> allSubs = new ArrayList<Subscription>();
		List<List<Subscription>> subsByAdv = new ArrayList<List<Subscription>>();
		StatsCollector.getInstance().setNoOfSubscribers(noOfSubs);
		// initialize data structures
		for (int i = 0; i < noOfAdvs; i++)
			subsByAdv.add(new ArrayList<Subscription>());
		// make sure all publishers have at least one subscriber
		if (noOfSubs < noOfAdvs) {
			throw new RuntimeException("There is not enough sub to cover all publishers!");
		}
		for(int a = 0; a < noOfAdvs; ++a) {
			Advertisement adv = allAdvs.get(a);
			Attribute[] atts = adv.getAttributes(); //attributeGenerator.generateDontCareAttributeSet();
			Subscription sub = new Subscription(adv.getAdvClass(), atts);
			subsPerAdv.get(adv).add(sub);
			subsByAdv.get(a).add(sub);
			allSubs.add(sub);
		}
		// generate rest of subs
		int[] subsPerPublisher = publisherPopularity.getSubsPerPublishers();
		for (int a = 0; a < noOfAdvs; a++) {
			for(int s = 0; s < subsPerPublisher[a] - 1; s++) {  // each adv already have one sub
				Attribute[] atts = attributeGenerator.generateAttributeSet();
				Advertisement adv = allAdvs.get(a);
				Subscription sub = new Subscription(adv.getAdvClass(), atts);
				subsPerAdv.get(adv).add(sub);
				subsByAdv.get(a).add(sub);
				allSubs.add(sub);
			}
		}
		// write publisher popularity to output
		System.out.println("Publisher popularities (dist=" + publisherPopType + "):");
		// zipf is easier to the eye when sorted!
		if(publisherPopType == PublisherPopularity.POPULARITY_TYPE.ZIPF)
			Arrays.sort(subsPerPublisher);
		System.out.println(Arrays.toString(subsPerPublisher));
		//distribute subscriptions between brokers
		// let's assume client distribution is rand uniform
		for(Subscription sub: allSubs) {
			int chosen = rand.nextInt(clients.size());
			clients.get(chosen).addSub(sub);
			sub.setSubscriberNode(clients.get(chosen).getNode());
		}
	}

	public void generatePublications(List<PubSubNodeWload> clients) {
		// for each publisher broker create pub_len * pub_rate pubs for each adv
		// since each adv represents one publisher client
		for (PubSubNodeWload node: clients) {
			// create pubs of different advs in order of time they will be published (interleaved!)
			for (int p = 0; p < pubDuration * pubRate; p++) {
				for (Advertisement adv: node.getAdvs()) {
					Publication pub = generateSinglePublication(adv);
					pub.setSourceBroker(node.getNode());
					node.addPub(pub);
				}
			}
			// set pub rate of each broker such that it can accommodate all of its clients
			// if a broker has two clients it has to publish twice as fast
			node.setPubRate(pubRate * node.getNoOfAdvs());
		}
	}

	public Publication generateSinglePublication(Advertisement adv) {
		Publication pub = new Publication();
		pub.setPubClass(adv.getAdvClass());
		pub.setMatchingAdv(adv);
		float[] points = new float[Attribute.noOfAttributes];
		Attribute[] atts = adv.getAttributes();
		for (int j = 0; j < Attribute.noOfAttributes; j++) {
			Attribute att = atts[j];
			points[j] = (rand.nextFloat() * (att.highVal - att.lowVal) + att.lowVal);
		}
		pub.setAttributes(points);
		return pub;
	}

	// save pub subscribers as list of ids rather than interface instances
	public void performMatching(List<PubSubNodeWload> nodes) {
		// match advs and subs
		for(PubSubNodeWload advNode: nodes) {
			for(Advertisement adv: advNode.getAdvs()) {
				for(PubSubNodeWload subNode: nodes) {
					for(Subscription sub: subNode.getSubs()) {
						if (adv.isMatches(sub))
							sub.addMatchingAdvs(adv);
					}
				}
			}
		}
		System.out.println("Matching pubs...");

		for (PubSubNodeWload node : nodes) {
			for (Publication pub : node.getPubs()) {
				Advertisement adv = pub.getMatchingAdv();
				Set<Integer> matchingSubs = new HashSet<Integer>();
				Set<NodeInfo> subscribers = new HashSet<NodeInfo>();
				for (Subscription sub: subsPerAdv.get(adv)) {
					if (sub.isMatches(pub)) {
						matchingSubs.add(sub.getID());
						subscribers.add(sub.getSubscriberNode());
					}
				}
				pub.setSubscribers(subscribers);
				pub.setMatchingSubscriptions(matchingSubs);
			}
		}

		System.out.println("Finished matching!");
		// set matching subscriptions
		// find nodes with at least one matching subscription
		// TODO: change these lists to set
//		List<PubSubNodeWload> targetNodes;
//		List<Integer> matchingSubscriptions;
//		List<NodeInfo> targetNodeInfos;
//		for (PubSubNodeWload node : nodes) {
//			for (Publication pub : node.getPubs()) {
//				targetNodes = new ArrayList<PubSubNodeWload>();
//				matchingSubscriptions = new ArrayList<Integer>();
//				for (PubSubNodeWload target : nodes) {
//					for (Subscription targetSub : target.getSubs()) {
//						if (targetSub.isMatches(pub)) {
//							//targetSub.addMatchingAdvs(pub.getMatchingAdv());
//							if (!targetNodes.contains(target))
//								targetNodes.add(target);
//							if(!matchingSubscriptions.contains(targetSub.getID()))
//								matchingSubscriptions.add(targetSub.getID());
//							pub.incrementMatchingSubs(target.getNode());
//						}
//					}
//				}
//				// create a new subscriber node set, DO NOT clear the old one!
//				targetNodeInfos = new ArrayList<NodeInfo>();
//				for (PubSubNodeWload targetNode : targetNodes)
//					targetNodeInfos.add(targetNode.getNode());
//				pub.setSubscribers(targetNodeInfos);
//				pub.setMatchingSubscriptions(matchingSubscriptions);
//			}
//		}
	}
	
	public List<PubSubNodeWload> getWorkload() {
		List<PubSubNodeWload> wload = new ArrayList<PubSubNodeWload>();
		for(PubSubNodeWload wl: allNodes)
			if(pubNodes.contains(wl) || subNodes.contains(wl))
				wload.add(wl);
		return wload;
	}

	public int getNoOfSubs() {
		return noOfSubs;
	}

	public int getNoOfAdvs() {
		return noOfAdvs;
	}

	public int getPubDuration() {
		return pubDuration;
	}

	public int getNoOfClasses() {
		return noOfClasses;
	}

	public DistType getSubDistOverNodes() {
		return subDistOverNodes;
	}

	public DistType getAdvDistOverNodes() {
		return advDistOverNodes;
	}

	public DistType getAttDistOverSubs() {
		return attDistOverSubs;
	}

	public long getPubRate() {
		return pubRate;
	}

	public ClientNodeType getSubNodeType() {
		return subNodeType;
	}

	public ClientNodeType getPubNodeType() {
		return pubNodeType;
	}

	public float getZipfSkew() {
		return zipfSkew;
	}

	public static void main(String[] args) {
		int noOfClasses = 50;
		int noOfSubs = 10000;
		RandomGenerator randomGen = new JDKRandomGenerator();
		ZipfDistribution classPopZipfDist = new ZipfDistribution(randomGen, noOfClasses, 0.001);
		UniformIntegerDistribution classPopUniformDist = new UniformIntegerDistribution(randomGen, 0, noOfClasses);
		PoissonDistribution classPopPoissonDist = new PoissonDistribution(randomGen, noOfClasses/4,
				PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);
		NormalDistribution classPopGausDist = new NormalDistribution(noOfClasses/2.0, noOfClasses/6.0);
		int[] pops = new int[noOfClasses];
		System.out.println("UNIFORM:");
		for(int i = 0; i < noOfSubs; ++i) {
			int c = classPopUniformDist.sample();
			if(c >= 0 && c < noOfClasses) ++pops[c];
			else --i;
		}
		System.out.println(Arrays.toString(pops));

		pops = new int[noOfClasses];
		System.out.println("ZIPF 0.8:");
		for(int i = 0; i < noOfSubs; ++i) {
			int c = classPopZipfDist.sample()-1;
			if(c >= 0 && c < noOfClasses) ++pops[c];
			else --i;
		}
		Arrays.sort(pops);
		System.out.println(Arrays.toString(pops));

		pops = new int[noOfClasses];
		System.out.println("POISSON 5:");
		for(int i = 0; i < noOfSubs; ++i) {
			int c = classPopPoissonDist.sample();
			if(c >= 0 && c < noOfClasses) ++pops[c];
			else --i;
		}
		//Arrays.sort(pops);
		System.out.println(Arrays.toString(pops));

		pops = new int[noOfClasses];
		System.out.println("GAUSS:");
		for(int i = 0; i < noOfSubs; ++i) {
			int c = (int) classPopGausDist.sample();
			if(c >= 0 && c < noOfClasses) ++pops[c];
			else --i;
		}
		//Arrays.sort(pops);
		System.out.println(Arrays.toString(pops));

	}
}
