package de.tum.msrg.sim;

import java.util.*;

import de.tum.msrg.message.PubMessage;
import de.tum.msrg.message.Publication;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.utils.Tuple;

public class StatsCollector {

	private static StatsCollector instance = new StatsCollector();

	public static StatsCollector getInstance() {
		return instance;
	}

	private StatsCollector() {
	}

	public static void reset() {
		instance = new StatsCollector();
	}

	protected int noOfSubscribers = 0;
	protected int noOfBrokers = -1;
	protected int noOfClasses;
	protected long totalGeneratedPubCount = 0;
	protected long totalPublishedPubCount = 0;
	protected long totalSentPubCount = 0;
	protected long totalSentGossipCount = 0;
	protected int totalGossipSentBeforeLastDelivery = -1;
	protected long totalSentPacketCount = 0;
	protected ArrayList<Long> sentPacketCountPerBroker;
	protected Map<Integer, Long> sentPacketCountPerPublisher = new HashMap<Integer, Long>();
	protected long totalReceivedGossipCount = 0;
	protected long totalDeliveredPubCount = 0;
	protected long totalSelfDeliveredPubCount = 0;
	protected long totalPubsDeliveredViaGossip = 0;
	protected long totalPubsDeliveredViaBatch = 0;
	protected long totalPubsDeliveredViaDirectLink = 0;
	// number of pubs received for forwarding/delivery via gossiping from other brokers
	protected long totalPubsReceivedViaGossipForDelivery = 0;
	protected List<Long> treeDeliveryLatencies = new LinkedList<Long>();
	protected List<Integer> treeDeliveryHopcounts = new LinkedList<Integer>();
	protected List<Long> gossipDeliveryLatencies = new LinkedList<Long>();
	protected List<Integer> gossipDeliveryHopcounts = new LinkedList<Integer>();
	protected List<Long> batchDeliveryLatencies = new LinkedList<Long>();
	protected List<Integer> batchDeliveryHopcounts = new LinkedList<Integer>();
	protected List<Long> directLinkDeliveryLatencies = new LinkedList<Long>();
	protected List<Integer> directLinkDeliveryHopcounts = new LinkedList<Integer>();
	// util metric of forwarded pubs for each broker, (match count, avg path len)
	protected Map<Integer, List<Tuple<Integer, Double>>> utilMetricPerBroker =
			new HashMap<Integer, List<Tuple<Integer, Double>>>();
	protected List<Double> pubGains = new LinkedList<Double>();
	// -1: not calculated, we use before last delivery, since gossiping goes on
	// regardless of whether there is a pub or not
	protected double averageGossipSentBeforeLastDelivery = -1;
	protected Map<Integer, List<Integer>> gossipSentTimesPerBroker = new HashMap<Integer, List<Integer>>();
	protected Map<Integer, Integer> gossipSentCountPerBroker = new HashMap<Integer, Integer>();
	protected Set<Integer> gossipGroups = new HashSet<Integer>();
	protected List<Double> gossipFalsePositiveRatio = new LinkedList<Double>();
	protected List<Integer> matchCountPergossip = new LinkedList<Integer>();
	protected Map<Integer, Integer> maxQueueSizePerBroker = new HashMap<Integer, Integer>();
	// direct delivery via gossip or direct link
	protected Map<Integer, Long> directDeliveryPerPublisher = new HashMap<Integer, Long>();
	// record the out degree of each publisher for each publish. in other words
	// count number of sends required to publish a pub
	protected Map<Integer, List<Integer>> MsgsSentForEachPublishPerPublisher = new HashMap<Integer, List<Integer>>();
	protected long lastPubDeliveryTime = -1;
	protected long totalSubscriptionMatchCount = 0;
	protected long totalPubsSentViaGossip = 0;
	protected long totalPubsSentViaBatching = 0;
	protected long totalPubsSentViaDirectLink = 0;
	protected Map<Integer, Long> unsuccessfulRetrievesPerPublisher = new HashMap<Integer, Long>();
	protected int[] popularityPerTopic;
	protected int[] deliveryPerTopic;
	protected long maxSimTime;
	protected int[] throughputRates;
	protected long totalPureForwardCount = 0;

	public void initialize(int noOfBrokers, int noOfClasses, long maxSimTime) {
		this.noOfBrokers = noOfBrokers;
		this.noOfClasses = noOfClasses;
		popularityPerTopic = new int[noOfClasses];
		deliveryPerTopic = new int[noOfClasses];
		this.maxSimTime = maxSimTime;
		throughputRates = new int[(int) (maxSimTime * 1.5 / 1000)];
		sentPacketCountPerBroker = new ArrayList<Long>(noOfBrokers);
		for (int i = 0; i < noOfBrokers; i++)
			sentPacketCountPerBroker.add(0l);
	}

	public void incrementTotalDeliveredPubCount() {
		++totalDeliveredPubCount;
	}

	public void incrementTotalSelfDeliveredPubCount() {
		++totalSelfDeliveredPubCount;
	}

	public void incrementTotalPublishedPubCount() {
		++totalPublishedPubCount;
	}

	public void incrementTotalGeneratedPubCount() {
		++totalGeneratedPubCount;
	}

	public void incrementTotalSubscriberMatchCount() {
		++totalSubscriptionMatchCount;
	}

	public void updateLastPubDeliveryTime(long time) {
		if (time > lastPubDeliveryTime) lastPubDeliveryTime = time;
	}

	public void incrementTotalReceivedGossipCount() {
		++totalReceivedGossipCount;
	}

	public float getPubDeliveryRate() {
		return (float)totalDeliveredPubCount / totalSubscriptionMatchCount;
	}

	public long getTotalSentGossipCount() {
		return totalSentGossipCount;
	}

	// list of values MUST be a sorted list
	public static <T> T getNthPercentile(List<T> values, double percentile) {
		if (percentile > 1 || percentile <= 0 || values.isEmpty())
			throw new RuntimeException("Empty value list or Invalud percentile! should be: 0.0 < percentile <= 1.0");
		int i = (int)Math.ceil(percentile * values.size());
		return values.get(i - 1);
	}

	/* Hooks that can be called for certain events to update the statistics */
	public void pubGenerated(Publication pub, NodeInfo broker) {
		incrementTotalGeneratedPubCount();
		for (NodeInfo b : pub.getSubscribers()) {
            incrementTotalSubscriberMatchCount();
        }
	}

	public void pubPublished(int publisherID, Publication pub, long time) {
		incrementTotalPublishedPubCount();
	}

	public void pubSent(int publisherID, Publication pub, long time) {
		++totalSentPubCount;
	}

	public void pubSent(int publisherID, List<Publication> pubs, long time) {
		for(Publication p: pubs)
			pubSent(publisherID, p, time);
	}

	// pub delivered via tree
	public void pubDeliveredViaTree(int brokerID, PubMessage pub, long time) {
		updatePubThroughput(time);
		treeDeliveryHopcounts.add(pub.getHopCount());
		treeDeliveryLatencies.add(pub.getLatency());
		incrementTotalDeliveredPubCount();
		updateLastPubDeliveryTime(time);
		++deliveryPerTopic[pub.getPublication().getPubClass()];
	}

	public void pubDeliveredViaBatch(int brokerID, Publication pub, long latency, int hopCount, long time) {
		updatePubThroughput(time);
		++totalPubsDeliveredViaBatch;
		batchDeliveryHopcounts.add(hopCount);
		batchDeliveryLatencies.add(latency);
		incrementTotalDeliveredPubCount();
		updateLastPubDeliveryTime(time);
		++deliveryPerTopic[pub.getPubClass()];
	}

	public void pubDeliveredViaDirectLink(int brokerID, PubMessage pmsg, long time) {
		updatePubThroughput(time);
		++totalPubsDeliveredViaDirectLink;
		directLinkDeliveryHopcounts.add(pmsg.getHopCount());
		directLinkDeliveryLatencies.add(pmsg.getLatency());
		incrementTotalDeliveredPubCount();
		updateLastPubDeliveryTime(time);
		++deliveryPerTopic[pmsg.getPublication().getPubClass()];
	}

	public void updatePubThroughput(long time) {
		throughputRates[(int) (time/1000)] += 1;
	}

	public double getAverageThroughput() {
		int tps = (int) (lastPubDeliveryTime / 1000);
		double total = 0;
		for (int i = 0; i <= tps; i++)
			total += throughputRates[i];
		return total / tps;
	}

	// TODO: update gossip sent count periodically and clear list of gossip sent times
	public void gossipSent(int brokerId, long time) {
		++totalSentGossipCount;
		if(time <= lastPubDeliveryTime) {
			// just increment sent count since this counts as sent before last delivery
			Integer sentCount = gossipSentCountPerBroker.get(brokerId);
			sentCount = (sentCount == null ? 1 : ++sentCount);
			gossipSentCountPerBroker.put(brokerId, sentCount);
		} else {
			// keep it for later!
			List<Integer> gossipTs = gossipSentTimesPerBroker.get(brokerId);
			if(gossipTs == null) {
				gossipTs = new LinkedList<Integer>();
				gossipSentTimesPerBroker.put(brokerId, gossipTs);
			}
			gossipTs.add((int) time);
		}
	}

	public void gossipReceived(int brokerId, long gossipId, long time) {
		++totalReceivedGossipCount;
	}

	public void pubSentViaGossip(Publication p) {
		++totalPubsSentViaGossip;
		++totalSentPubCount;
	}

	public void pubSentViaBatching(Publication p) {
		++totalPubsSentViaBatching;
		++totalSentPubCount;
	}

	public void pubSentViaDirectLink(Publication p) {
		++totalPubsSentViaDirectLink;
		++totalSentPubCount;
	}

	public void incrementDirectDeliveryPerPublisher(int publisherID) {
		Long count = directDeliveryPerPublisher.get(publisherID);
		if(count == null)
			count = Long.valueOf(0);
		++count;
		directDeliveryPerPublisher.put(publisherID, count);
	}

	public void addGossipFalsePositiveRatio(double ratio) {
		gossipFalsePositiveRatio.add(ratio);
	}

	public void addMatchCountPerGossip(int count) {
		matchCountPergossip.add(count);
	}

	public double getAverageMatchCountPerGossip() {
		return matchCountPergossip.isEmpty() ? 0 : averageInt(matchCountPergossip);
	}

	public long getTotalDeliveredPubCount() {
		return totalDeliveredPubCount;
	}

	public long getTotalSelfDeliveredPubCount() {
		return totalSelfDeliveredPubCount;
	}

	public double getPercentageOfLocalDelivery() {
		return (double)getTotalSelfDeliveredPubCount() / getTotalDeliveredPubCount();
	}

	public long getTotalPublishedPubCount() {
		return totalPublishedPubCount;
	}

	public long getTotalSentPubCount() {
		return totalSentPubCount;
	}

	public long getTotalGeneratedPubCount() {
		return totalGeneratedPubCount;
	}

	public long getTotalSubscriptionMatchCount() {
		return totalSubscriptionMatchCount;
	}

	public long getLastPubDeliveryTime() {
		return lastPubDeliveryTime;
	}

	public List<Integer> getTreeDeliveryHopcounts() {
		return treeDeliveryHopcounts;
	}

	public long getTotalReceivedGossipCount() {
		return totalReceivedGossipCount;
	}

	public long getTotalPubsSentViaGossip() {
		return totalPubsSentViaGossip;
	}

	public long getTotalPubsDeliveredViaGossip() {
		return totalPubsDeliveredViaGossip;
	}

	public void incrementUnsuccessfulRetrieveCount(int publisherID) {
		Long count = unsuccessfulRetrievesPerPublisher.get(publisherID);
		if(count == null)
			count = Long.valueOf(0);
		++count;
		unsuccessfulRetrievesPerPublisher.put(publisherID, count);
	}

	public long getUnsuccessfulRetrieveCount(int publisherID) {
		Long count = unsuccessfulRetrievesPerPublisher.get(publisherID);
		return count == null ? 0 : count;
	}

	public long getDirectRetrievesPerPublisher(int publisherID) {
		Long count = directDeliveryPerPublisher.get(publisherID);
		return count == null ? 0 : count;
	}

	public double getAvgDirectPubDeliveryPerBroker() {
		int count = directDeliveryPerPublisher.keySet().size(), sum = 0;
		for(long v: directDeliveryPerPublisher.values())
			sum += v;
		return (double) sum / count;
	}

	public int getNoOfSubscribers() {
		return noOfSubscribers;
	}

	public void setNoOfSubscribers(int noOfSubscribers) {
		this.noOfSubscribers = noOfSubscribers;
	}

	public int getNoOfBrokers() {
		return noOfBrokers;
	}

	public int getGossipSentPerBroker(int brokerId) {
		List<Integer> ts = gossipSentTimesPerBroker.get(brokerId);
		Integer sentCount = gossipSentCountPerBroker.get(brokerId);
		if(sentCount == null) sentCount = 0;
		return ts == null ? sentCount : sentCount + ts.size();
	}

	public int getGossipSentBeforeLastDeliveryPerBroker(int brokerId) {
		Integer sentCount = gossipSentCountPerBroker.get(brokerId);
		if(sentCount == null)
			sentCount = 0;
		List<Integer> ts = gossipSentTimesPerBroker.get(brokerId);
		if(ts == null)
			return 0;
		int count = 0;
		for(int t: ts)
			if(t <= lastPubDeliveryTime)
				++count;
		return sentCount + count;
	}

	public double getAvgGossipBeforeLastDeliveryPerBroker() {
		if(averageGossipSentBeforeLastDelivery != -1)
			return averageGossipSentBeforeLastDelivery;
		int count = gossipSentTimesPerBroker.keySet().size(), sum = 0;
		for(int brokerId: gossipSentTimesPerBroker.keySet())
			sum += getGossipSentBeforeLastDeliveryPerBroker(brokerId);
		totalGossipSentBeforeLastDelivery = sum;
		return count == 0 ? 0 : (double)sum / count;
	}

	public int getNoOfGossipGroups() {
		return gossipGroups.size();
	}

	public void gossipGroupCreated(int advId) {
		gossipGroups.add(advId);
	}



	public void addQueueSize(int brokerID, int qsize) {
		if(qsize < 0)
			throw new RuntimeException("Negative queue size is wrong!!");
		Integer brokerQSize = maxQueueSizePerBroker.get(brokerID);
		if(brokerQSize == null || qsize > brokerQSize)
			maxQueueSizePerBroker.put(brokerID, qsize);
	}

	public void addUtilMetric(int brokerId, int matchCount, double avgPathLen) {
		List<Tuple<Integer, Double>> perBroker = utilMetricPerBroker.get(brokerId);
		if(perBroker == null) {
			perBroker = new LinkedList<Tuple<Integer, Double>>();
			utilMetricPerBroker.put(brokerId, perBroker);
		}
		perBroker.add(new Tuple<Integer, Double>(matchCount, avgPathLen));
		pubGains.add(matchCount / avgPathLen);
	}

	public double getAveragePubGain() {
		return pubGains.isEmpty() ? 0 : averageDouble(pubGains);
	}

	public double getPercentilePubGain(double perc) {
		assert (perc > 0 && perc <= 1);
		Collections.sort(pubGains);
		return pubGains.isEmpty() ? 0 : getNthPercentile(pubGains, perc);
	}

	public void incrementTotalSentPacketCount(int count) {
		totalSentPacketCount += count;
	}

	public void updatePacketSentCount(int count, int broker) {
		incrementTotalSentPacketCount(count);
		long v = sentPacketCountPerBroker.get(broker);
		sentPacketCountPerBroker.set(broker, v + count);
	}

	public long getPacketSentCountPerBrokerPercentile(double perc) {
		Collections.sort(sentPacketCountPerBroker);
		return getNthPercentile(sentPacketCountPerBroker, perc);
	}

	public double getPacketSentCountPerBrokerAverage() {
		return averageLong(sentPacketCountPerBroker);
	}

	public void updatePacketSentCountPerPublisher(int count, int broker) {
		Long v = sentPacketCountPerPublisher.get(broker);
		if (v == null) v = 0L;
		sentPacketCountPerPublisher.put(broker, v + count);
	}

	public int getNoOfPublisherBrokers() {
		return sentPacketCountPerPublisher.size();
	}

	public double getAveragePacketSentCountPerPublisher() {
		return averageLong(sentPacketCountPerPublisher.values());
	}

	public void incrementTotalPubsReceivedViaGossipForDelivery() {
		++totalPubsReceivedViaGossipForDelivery;
	}

	public void addMsgsSentForEachPublishPerPublisher(int bid, int count) {
		List<Integer> counts = MsgsSentForEachPublishPerPublisher.get(bid);
		if(counts == null) {
			counts = new LinkedList<Integer>();
			MsgsSentForEachPublishPerPublisher.put(bid, counts);
		}
		counts.add(count);
	}

	public float get99thTreeDeliveryLatency() {
		Collections.sort(treeDeliveryLatencies);
		return getNthPercentile(treeDeliveryLatencies, 0.99);
	}

	public float get99thTreeDeliveryHopcount() {
		Collections.sort(treeDeliveryHopcounts);
		return getNthPercentile(treeDeliveryHopcounts, 0.99);
	}

	public Tuple<Double, Double> getAverageUtilRatio() {
		int count = 0;
		long totalMatch = 0;
		double totalAvgPathLen = 0;
		for(List<Tuple<Integer, Double>> ratios: utilMetricPerBroker.values())
			for(Tuple<Integer, Double> r: ratios) {
				++count;
				totalMatch += r.x;
				totalAvgPathLen += r.y;
			}
		return count == 0 ? new Tuple<Double, Double>(.0, .0)
				: new Tuple<Double, Double>((double)totalMatch / count, (double)totalAvgPathLen / count);
	}

	public double getAverageQueueSize() {
		int count = maxQueueSizePerBroker.values().size();
		long sum = 0;
		for(int qa: maxQueueSizePerBroker.values())
			sum += qa;
		if(sum < 0)
			throw new RuntimeException("Negative sum of queue size is wrong!!");
		return count == 0 ? 0 : (double)sum / count;
	}

	public double get99thQueueSize() {
		if(maxQueueSizePerBroker.values().isEmpty())
			return 0;
		List<Integer> queueSizes = new ArrayList<Integer>(maxQueueSizePerBroker.values());
		Collections.sort(queueSizes);
		return getNthPercentile(queueSizes, 0.99);
	}

	public float getGossipDeliveryLatencyPercentile(double perc) {
		assert (perc > 0 && perc <= 1);
		Collections.sort(gossipDeliveryLatencies);
		return gossipDeliveryLatencies.isEmpty() ?
				0 : getNthPercentile(gossipDeliveryLatencies, perc);
	}

	public float get99thGossipDeliveryHopcount() {
		Collections.sort(gossipDeliveryHopcounts);
		return gossipDeliveryHopcounts.isEmpty() ? 0 : getNthPercentile(gossipDeliveryHopcounts, 0.99);
	}

	public float getBatchDeliveryLatencyPercentile(double perc) {
		assert (perc > 0 && perc <= 1);
		Collections.sort(batchDeliveryLatencies);
		return batchDeliveryLatencies.isEmpty() ?
				0 : getNthPercentile(batchDeliveryLatencies, perc);
	}

	public float get99thBatchDeliveryHopcount() {
		Collections.sort(batchDeliveryHopcounts);
		return batchDeliveryHopcounts.isEmpty() ? 0 : getNthPercentile(batchDeliveryHopcounts, 0.99);
	}

	public float getDirectLinkDeliveryLatencyPercentile(double perc) {
		assert (perc > 0 && perc <= 1);
		Collections.sort(directLinkDeliveryLatencies);
		return directLinkDeliveryLatencies.isEmpty() ?
				0 : getNthPercentile(directLinkDeliveryLatencies, perc);
	}

	public float get99thDirectLinkDeliveryHopcount() {
		Collections.sort(directLinkDeliveryHopcounts);
		return directLinkDeliveryHopcounts.isEmpty() ? 0 : getNthPercentile(directLinkDeliveryHopcounts, 0.99);
	}

	public float get99thTotalDeliveryLatency() {
		List<Long> all = new LinkedList<Long>(treeDeliveryLatencies);
		if(!gossipDeliveryLatencies.isEmpty() && !batchDeliveryLatencies.isEmpty() && !directLinkDeliveryLatencies.isEmpty())
			System.err.println("WARNING: there are deliveries of all three types! Gossip, direct and batch!");
		all.addAll(gossipDeliveryLatencies);
		all.addAll(batchDeliveryLatencies);
		all.addAll(directLinkDeliveryLatencies);
		Collections.sort(all);
		return getNthPercentile(all, 0.99);
	}

	public float get99thTotalDeliveryHopcount() {
		List<Integer> all = new LinkedList<Integer>(treeDeliveryHopcounts);
		if(!gossipDeliveryHopcounts.isEmpty() && !batchDeliveryHopcounts.isEmpty() && !directLinkDeliveryHopcounts.isEmpty())
			System.err.println("WARNING: there are deliveries of all three types! Gossip, direct and batch!");
		all.addAll(gossipDeliveryHopcounts);
		all.addAll(directLinkDeliveryHopcounts);
		all.addAll(batchDeliveryHopcounts);
		Collections.sort(all);
		return getNthPercentile(all, 0.99);
	}

	// percentage of pub deliveries served directly by the publisher
	public double get99thDirectDeliveryPerPublisher() {
		List<Long> all = new LinkedList<Long>(directDeliveryPerPublisher.values());
		Collections.sort(all);
		return all.isEmpty() ? 0 : (double)getNthPercentile(all, 0.99);// / totalDeliveredPubCount;
	}

	public long get99thMsgsSentForEachPublishPerPublisher() {
		List<Integer> all = new LinkedList<Integer>();
		for(List<Integer> counts: MsgsSentForEachPublishPerPublisher.values())
			all.addAll(counts);
		Collections.sort(all);
		return all.isEmpty() ? 0 : getNthPercentile(all, 0.99);
	}

	public int getTotalGossipsSentsBeforeLastDelivery() {
		if(totalGossipSentBeforeLastDelivery == -1)  // call calculate average and total will be there!
			averageGossipSentBeforeLastDelivery = getAvgGossipBeforeLastDeliveryPerBroker();
		return totalGossipSentBeforeLastDelivery;
	}

	public long getTotalPubsReceivedViaGossipForDelivery() {
		return totalPubsReceivedViaGossipForDelivery;
	}

	public double getGossipDeliveryFalsePositiveRate() {
		//return 1 - ((double)totalPubsDeliveredViaGossip / totalPubsReceivedViaGossipForDelivery);
		if (gossipFalsePositiveRatio.size() == 0)
			return 0;
		double total = 0;
		for (double r: gossipFalsePositiveRatio)
			total += r;
		return total / gossipFalsePositiveRatio.size();
	}

	public void setTopicPopularity(int topic, int popularity) {
		popularityPerTopic[topic] = popularity;
	}

	public int[] getPopularityPerTopic() {
		return popularityPerTopic;
	}

	public int[] getDeliveryPerTopic() {
		return deliveryPerTopic;
	}

	public long getTotalSentPacketCount() {
		return totalSentPacketCount;
	}

	public long getTotalPubsSentViaBatching() {
		return totalPubsSentViaBatching;
	}

	public long getTotalPubsSentViaDirectLink() {
		return totalPubsSentViaDirectLink;
	}

	public long getTotalPubsDeliveredViaDirectLink() {
		return totalPubsDeliveredViaDirectLink;
	}

	public long getTotalPubsDeliveredViaBatch() {
		return totalPubsDeliveredViaBatch;
	}

	public long getTotalMsgsSent() {
		return getTotalSentPubCount() + getTotalGossipsSentsBeforeLastDelivery();
	}

	public void incrementTotalPureForwardCount() {
		totalPureForwardCount++;
	}

	public long getTotalPureForwardCount() {
		return totalPureForwardCount;
	}

	public double averageInt(Collection<Integer> nums) {
		double total = 0;
		for (int i: nums)
			total += i;
		return total / nums.size();
	}

	public double averageLong(Collection<Long> nums) {
		double total = 0;
		for (long i: nums)
			total += i;
		return total / nums.size();
	}

	public double averageDouble(Collection<Double> nums) {
		double total = 0;
		for (double i: nums)
			total += i;
		return total / nums.size();
	}
}