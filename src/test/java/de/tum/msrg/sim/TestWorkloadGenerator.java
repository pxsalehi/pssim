package de.tum.msrg.sim;

import de.tum.msrg.pubsub.ConfigKeys;
import de.tum.msrg.config.ConfigParserException;
import de.tum.msrg.config.Configuration;
import de.tum.msrg.message.Advertisement;
import de.tum.msrg.message.Attribute;
import de.tum.msrg.message.Attribute.Operator;
import de.tum.msrg.message.Publication;
import de.tum.msrg.message.Subscription;
import de.tum.msrg.resources.configs.Configurations;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import de.tum.msrg.overlay.RuntimeSimException;
import de.tum.msrg.overlay.PubSubNodeWload;
import de.tum.msrg.sim.WorkloadGenerator.ClientNodeType;
import de.tum.msrg.sim.WorkloadGenerator.DistType;
import de.tum.msrg.topology.EdgeInfo;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.topology.Topology;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestWorkloadGenerator {
	/**
	 *     .--1---3
	 * 0---+--2---4
	 *         '--5
	 */
	private NodeInfo n0 = new NodeInfo(0, 2);
	private NodeInfo n1 = new NodeInfo(1, 2);
	private NodeInfo n2 = new NodeInfo(2, 3);
	private NodeInfo n3 = new NodeInfo(3, 1);
	private NodeInfo n4 = new NodeInfo(4, 1);
	private NodeInfo n5 = new NodeInfo(5, 1);
	private EdgeInfo e0 = new EdgeInfo(0, n0, n1, 52);
	private EdgeInfo e1 = new EdgeInfo(1, n0, n2, 63);
	private EdgeInfo e2 = new EdgeInfo(2, n1, n3, 70);
	private EdgeInfo e3 = new EdgeInfo(3, n2, n4, 10);
	private EdgeInfo e4 = new EdgeInfo(4, n2, n5, 35);
	private Topology topology;
	private long seed = 54987001;
	private String configStr;
//	private String configStr = "[simulation]         \n"
//			+ "no_of_advs = 6                         \n"
//            + "no_of_subs = 6                         \n"
//            + "no_of_pubs = 10                         \n"
//            + "pub_interval = 1000                    \n"
//            + "[workload]                             \n"
//            + "no_of_classes = 4                      \n"
//            + "no_of_attributes = 3                   \n"
//            + "attribute_min_value = 0                \n"
//            + "attribute_max_value = 1000             \n"
//            + "zipf_exponent = 0.7                    \n"
//            + "pref_class_bias = 80                   \n"
//            + "pref_class_same	 = true               \n"
//            + "attribute_value_dist = ZIPF            \n"
//            + "attribute_std_dev_factor = 2           \n"
//            + "sub_over_nodes_dist = UNIFORM          \n"
//            + "sub_over_nodes_std_dev_factor = 2      \n"
//            + "adv_over_nodes_dist = UNIFORM          \n"
//            + "adv_over_nodes_std_dev_factor = 2      \n"
//            + "pub_over_nodes_dist = UNIFORM          \n"
//            + "pub_over_nodes_std_dev_factor = 2      \n"
//            + "pub_nodes = ALL                 \n"
//            + "pub_nodes_list = 11,13,6               \n"
//			+ "sub_nodes = ALL                 \n"
//			+ "sub_nodes_list = 11,13,6               \n";
	
	@Before
	public void setUp() throws ConfigParserException, IOException {
		topology = new Topology(new NodeInfo[] {n0, n1, n2, n3, n4, n5}, 
				                new EdgeInfo[] {e0, e1, e2, e3, e4});
		configStr = "";
		BufferedReader conf = new BufferedReader(new FileReader(Configurations.SAMPLE_CONF1_PATH));
		String l = conf.readLine();
		while(l != null) {
			configStr += l + "\n";
			l = conf.readLine();
		}
	}
	
	@After
	public void tearDown() {
		StatsCollector.reset();
	}
	
	@Ignore @Test(expected= RuntimeSimException.class)
	public void testNotEnoughSubs() throws ConfigParserException, RuntimeSimException, IOException {
		Configuration config = new Configuration(configStr);
		// six nodes but 4 subs
		config.setProperty(ConfigKeys.SIM_NO_OF_SUBS, 4);
		WorkloadGenerator wlgen = new WorkloadGenerator(config, topology, seed);
		wlgen.generate();
	}

	@Ignore @Test(expected= RuntimeSimException.class)
	public void testNotEnoughAdvs() throws ConfigParserException, RuntimeSimException, IOException {
		Configuration config = new Configuration(configStr);
		// six nodes but 4 advs
		config.setProperty(ConfigKeys.SIM_NO_OF_ADVS, 4);
		WorkloadGenerator wlgen = new WorkloadGenerator(config, topology, seed);
		wlgen.generate();
	}

	@Test @Ignore
	public void testNotEnoughPubs() throws ConfigParserException, RuntimeSimException, IOException {
		Configuration config = new Configuration(configStr);
		// six nodes but 4 pubs
		config.setProperty(ConfigKeys.SIM_NO_OF_PUBS, 4);
		WorkloadGenerator wlgen = new WorkloadGenerator(config, topology, seed);
		wlgen.generate();
	}

	@Test
	public void testConstructorLeafClients() throws ConfigParserException, IOException {
		Configuration config = new Configuration(configStr);
		config.setProperty(ConfigKeys.WLOAD_PUB_NODES, ClientNodeType.LEAVES);
		config.setProperty(ConfigKeys.WLOAD_SUB_NODES, ClientNodeType.LEAVES);
		WorkloadGenerator wlgen = new WorkloadGenerator(config, topology, seed);
		PubSubNodeWload[] clients = {new PubSubNodeWload(n3), new PubSubNodeWload(n4), new PubSubNodeWload(n5)};
		assertTrue(CollectionUtils.isEqualCollection(wlgen.getWorkload(), Arrays.asList(clients)));
	}

	@Ignore @Test
	public void testConstructorAllClients() throws ConfigParserException, RuntimeSimException, IOException {
		Configuration config = new Configuration(configStr);
		StatsCollector.getInstance().initialize(config.getIntConfig(ConfigKeys.SIM_NO_OF_NODES),
												config.getIntConfig(ConfigKeys.WLOAD_NO_OF_CLASSES));
		WorkloadGenerator wlgen = new WorkloadGenerator(config, topology, seed);
		assertEquals(wlgen.getNoOfAdvs(), 6);
		assertEquals(wlgen.getNoOfSubs(), 6);
		assertEquals(wlgen.getNoOfPubs(), 10);
		assertEquals(wlgen.getPublicationInterval(), 1000);
		assertEquals(wlgen.getNoOfClasses(), 4);
		assertEquals(Attribute.noOfAttributes, 3);
		assertEquals(wlgen.getAttDistOverSubs(), DistType.ZIPF);
		assertEquals(wlgen.getPubNodeType(), ClientNodeType.ALL);
		PubSubNodeWload[] clients = {new PubSubNodeWload(n0), new PubSubNodeWload(n1),
				                     new PubSubNodeWload(n2), new PubSubNodeWload(n3),
				                     new PubSubNodeWload(n4), new PubSubNodeWload(n5)};
		assertTrue(CollectionUtils.isEqualCollection(wlgen.getWorkload(), Arrays.asList(clients)));
		wlgen.generate();
		List<PubSubNodeWload> wload = wlgen.getWorkload();
		assertEquals(StatsCollector.getInstance().getTotalGeneratedPubCount(), 10);
		//System.out.println(String.format("-> In test %s:", "testConstructorAllClients()"));
		for(PubSubNodeWload client: wload) {
			// at least one pub per client since at least one adv per client?
			//System.out.println(String.format("N%d has %d pubs.",
			//		           client.getBroker().getEventid(), client.getPubs().size()));
			assertTrue(client.getSubs().size() >= 1);
			assertTrue(client.getAdvs().size() >= 1);
			assertEquals(client.getPubInterval(), 1000);
			for(Advertisement adv: client.getAdvs()) {
				assertEquals(adv.getAttributes().length, 3);
				for(Attribute att: adv.getAttributes()) {
					assertTrue(att.lowVal >= 0);
					assertTrue(att.highVal <= 1000);
				}
			}
			for(Subscription sub: client.getSubs()) {
				assertEquals(sub.getAttributes().length, 3);
				for(Attribute att: sub.getAttributes()) {
					assertTrue(att.lowVal >= 0);
					assertTrue(att.highVal <= 1000);
				}
			}
			for(Publication pub: client.getPubs()) {
				assertEquals(pub.getAttributes().length, 3);
				for(int i = 0; i < pub.getAttributes().length; ++i) {
					float att = pub.getAttributes()[i];
					assertTrue(att >= pub.getMatchingAdv().getAttributes()[i].lowVal);
					assertTrue(att <= pub.getMatchingAdv().getAttributes()[i].highVal);
				}
			}
		}
	}

	@Test @Ignore
    public void testPerformMatchingOnePubOneSubMatch() throws ConfigParserException, IOException, RuntimeSimException {
        Configuration config = new Configuration(configStr);
        config.setProperty(ConfigKeys.SIM_NO_OF_SUBS, 1);
        config.setProperty(ConfigKeys.SIM_NO_OF_PUBS, 1);
        config.setProperty(ConfigKeys.WLOAD_NO_OF_CLASSES, 2);
        config.setProperty(ConfigKeys.WLOAD_NO_OF_ATTRIBUTES, 2);
        WorkloadGenerator wlgen = new WorkloadGenerator(config, topology, seed);
        PubSubNodeWload n0Wload = new PubSubNodeWload(n0);
        PubSubNodeWload n2Wload = new PubSubNodeWload(n2);
        /**
         * p1: { 0, 10 , 421  } matches s1
         * s1: { 0, x <= 10 , 90 <= y <= 421 }
         */
        Publication p1 = new Publication(0, new float[]{10f, 421f});
        Subscription s1 = new Subscription(0, new Attribute[]{
                new Attribute(Operator.LESS_THAN, Attribute.lowRange, 10f),
                new Attribute(Operator.RANGE, 90f, 421f)});
        n0Wload.addPub(p1);
        n2Wload.addSub(s1);
        wlgen.performMatching(Arrays.asList(n0Wload, n2Wload));
        assertEquals(p1.getSubscribers().size(), 1);
        assertEquals(p1.getMatchingSubscriptions().size(), 1);
        assertTrue(CollectionUtils.isEqualCollection(p1.getMatchingSubscriptions(),
                                                     Arrays.asList(s1.getID())));
        assertTrue(CollectionUtils.isEqualCollection(p1.getSubscribers(), Arrays.asList(n2)));
    }

    @Test
    public void testPerformMatchingOnePubTwoSubMatch() throws ConfigParserException, IOException, RuntimeSimException {
        Configuration config = new Configuration(configStr);
        config.setProperty(ConfigKeys.SIM_NO_OF_SUBS, 2);
        config.setProperty(ConfigKeys.SIM_NO_OF_PUBS, 1);
        config.setProperty(ConfigKeys.WLOAD_NO_OF_CLASSES, 4);
        config.setProperty(ConfigKeys.WLOAD_NO_OF_ATTRIBUTES, 2);
        WorkloadGenerator wlgen = new WorkloadGenerator(config, topology, seed);
        PubSubNodeWload n0Wload = new PubSubNodeWload(n0);
        PubSubNodeWload n2Wload = new PubSubNodeWload(n2);
        PubSubNodeWload n3Wload = new PubSubNodeWload(n3);
        /**
         * p1: { 0, 1  , 99   } matches s1, s2
         *
         * s1: { 0, x <= 10 , 90 <= y <= 421 }
         * s2: { 0, 1 <= x <= 2 ,  y >= 90] }
         */
        Publication p1 = new Publication(0, new float[]{1f, 99f});
        Subscription s1 = new Subscription(0, new Attribute[]{
                new Attribute(Operator.LESS_THAN, Attribute.lowRange, 10f),
                new Attribute(Operator.RANGE, 90f, 421f)});
        Subscription s2 = new Subscription(0, new Attribute[]{
                new Attribute(Operator.RANGE, 1f, 2f),
                new Attribute(Operator.GREATER_THAN, 90f, Attribute.highRange)});
        n0Wload.addPub(p1);
        n2Wload.addSub(s1);
        n3Wload.addSub(s2);
        wlgen.performMatching(Arrays.asList(n0Wload, n2Wload, n3Wload));
        assertEquals(p1.getSubscribers().size(), 2);
        assertEquals(p1.getMatchingSubscriptions().size(), 2);
        assertTrue(CollectionUtils.isEqualCollection(p1.getMatchingSubscriptions(),
                Arrays.asList(s1.getID(), s2.getID())));
        assertTrue(CollectionUtils.isEqualCollection(p1.getSubscribers(), Arrays.asList(n2, n3)));
    }

	@Test
	public void testPerformMatchingFourPubFourSubMix() throws ConfigParserException, IOException, RuntimeSimException {
		Configuration config = new Configuration(configStr);
		config.setProperty(ConfigKeys.SIM_NO_OF_SUBS, 4);
		config.setProperty(ConfigKeys.SIM_NO_OF_PUBS, 4);
		config.setProperty(ConfigKeys.WLOAD_NO_OF_CLASSES, 4);
		config.setProperty(ConfigKeys.WLOAD_NO_OF_ATTRIBUTES, 2);
		WorkloadGenerator wlgen = new WorkloadGenerator(config, topology, seed);
		PubSubNodeWload n0Wload = new PubSubNodeWload(n0);
		PubSubNodeWload n2Wload = new PubSubNodeWload(n2);
		PubSubNodeWload n3Wload = new PubSubNodeWload(n3);
		PubSubNodeWload n4Wload = new PubSubNodeWload(n4);
		PubSubNodeWload n5Wload = new PubSubNodeWload(n5);
		/**
		 * p1: { 0, 10 , 421  } matches s1
		 * p2: { 0, 1  , 99   } matches s1, s3 
		 * p3: { 1, 500, 99.8 } matches s2
		 * p4: { 2, 89, 900.5 } matches nothing
		 * 
		 * s1: { 0, x <= 10 , 90 <= y <= 421 }
		 * s2: { 1, 300 <= x <= 600 , 90 <= y <= 100] }
		 * s3: { 0, 1 <= x <= 2 ,  y >= 90] }
		 * s4: { 3, *, * }
		 */
		Publication p1 = new Publication(0, new float[]{10f, 421f});
		Publication p2 = new Publication(0, new float[]{1f, 99f});
		Publication p3 = new Publication(1, new float[]{500f, 99.8f});
		Publication p4 = new Publication(2, new float[]{89f, 900.5f});
		Subscription s1 = new Subscription(0, new Attribute[]{
				new Attribute(Operator.LESS_THAN, Attribute.lowRange, 10f),
				new Attribute(Operator.RANGE, 90f, 421f)});
		Subscription s2 = new Subscription(1, new Attribute[]{
				new Attribute(Operator.RANGE, 300f, 600f),
				new Attribute(Operator.RANGE, 90f, 100f)});
		Subscription s3 = new Subscription(0, new Attribute[]{
				new Attribute(Operator.RANGE, 1f, 2f),
				new Attribute(Operator.GREATER_THAN, 90f, Attribute.highRange)});
		Subscription s4 = new Subscription(3, new Attribute[]{
				new Attribute(Operator.RANGE, Attribute.lowRange, Attribute.highRange),
				new Attribute(Operator.RANGE, Attribute.lowRange, Attribute.highRange)});
		n0Wload.addPub(p1);
		n0Wload.addPub(p2);
		n4Wload.addPub(p3);
		n5Wload.addPub(p4);
		n2Wload.addSub(s1);
		n2Wload.addSub(s3);
		n3Wload.addSub(s2);
		n0Wload.addSub(s4);
		wlgen.performMatching(Arrays.asList(n0Wload, n2Wload, n3Wload, n4Wload, n5Wload));

        assertEquals(p1.getSubscribers().size(), 1);
        assertEquals(p1.getMatchingSubscriptions().size(), 1);
        assertTrue(CollectionUtils.isEqualCollection(p1.getMatchingSubscriptions(), Arrays.asList(s1.getID())));
        assertTrue(CollectionUtils.isEqualCollection(p1.getSubscribers(), Arrays.asList(n2)));

        assertEquals(p2.getSubscribers().size(), 1); // both matching subs are from same node
        assertEquals(p2.getMatchingSubscriptions().size(), 2);
        assertTrue(CollectionUtils.isEqualCollection(p2.getMatchingSubscriptions(),
                                                     Arrays.asList(s1.getID(), s3.getID())));
        assertTrue(CollectionUtils.isEqualCollection(p2.getSubscribers(), Arrays.asList(n2)));

        assertEquals(p3.getSubscribers().size(), 1);
        assertEquals(p3.getMatchingSubscriptions().size(), 1);
        assertTrue(CollectionUtils.isEqualCollection(p3.getMatchingSubscriptions(),
                Arrays.asList(s2.getID())));
        assertTrue(CollectionUtils.isEqualCollection(p3.getSubscribers(), Arrays.asList(n3)));

        assertEquals(p4.getSubscribers().size(), 0);
        assertEquals(p4.getMatchingSubscriptions().size(), 0);
	}

	@Test
	public void testPerformMatchingAdvSub() {
		// a1={0, x > 10, y < 200} a2={1, x < 100, 150 < y < 400} a3={1, x <10}
		// s1={0, x > 0} s2={1, 0 < x < 50, y < 500}, s3={1; x > 999}, s4={1}
		Attribute.noOfAttributes = 2;
		Attribute.lowRange = 0;
		Attribute.highRange = 1000;
		Subscription.setTotalClasses(2);
		Advertisement a1 = new Advertisement(0, new Attribute[]{Attribute.greaterThan(10), Attribute.lessThan(200)});
		Advertisement a2 = new Advertisement(1, new Attribute[]{Attribute.lessThan(100), Attribute.range(150, 400)});
		Advertisement a3 = new Advertisement(1, new Attribute[]{Attribute.lessThan(10), Attribute.star()});
		Subscription s1 = new Subscription(0, new Attribute[]{Attribute.greaterThan(0), Attribute.star()});
		Subscription s2 = new Subscription(1, new Attribute[]{Attribute.range(0, 50), Attribute.lessThan(500)});
		Subscription s3 = new Subscription(1, new Attribute[]{Attribute.greaterThan(999), Attribute.star()});
		Subscription s4 = new Subscription(1, new Attribute[]{Attribute.star(), Attribute.star()});
		PubSubNodeWload psw1 = new PubSubNodeWload(n1), psw2 = new PubSubNodeWload(n2), psw3 = new PubSubNodeWload(n3),
						psw4 = new PubSubNodeWload(n4), psw5 = new PubSubNodeWload(n5);
		psw1.addAdv(a1);
		psw2.addAdv(a2);
		psw2.addAdv(a3);
		psw3.addSub(s1);
		psw4.addSub(s2);
		psw5.addSub(s3);
		psw5.addSub(s4);
		WorkloadGenerator.performMatching(Arrays.asList(psw1, psw2, psw3, psw4, psw5));
		assertTrue(CollectionUtils.isEqualCollection(s1.getMatchingAdvs(), Arrays.asList(a1)));
		assertTrue(CollectionUtils.isEqualCollection(s2.getMatchingAdvs(), Arrays.asList(a2,a3)));
		assertTrue(s3.getMatchingAdvs().isEmpty());
		assertTrue(CollectionUtils.isEqualCollection(s4.getMatchingAdvs(), Arrays.asList(a2,a3)));
	}
}
