package de.tum.msrg;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		de.tum.msrg.baseline.TestBaseline.class,
		de.tum.msrg.baseline.TestBaselineWithLinkCongestion.class,
		de.tum.msrg.baseline.TestBaselineWithBrokerOverloaded.class,
		// test.baseline.TestSim.class,  // does not work with JUnit

		de.tum.msrg.config.TestConfig.class,

		de.tum.msrg.lpbcast.TestGossiper.class,

		de.tum.msrg.message.TestMessage.class,
		de.tum.msrg.message.TestSubscription.class,
		de.tum.msrg.message.TestAdvertisement.class,
		de.tum.msrg.message.TestPublication.class,

		de.tum.msrg.overlay.TestOverlayGossipLatency.class,
		de.tum.msrg.overlay.TestOverlayLink.class,
		de.tum.msrg.overlay.TestPubSubNodeWload.class,

		de.tum.msrg.popsub.TestCommMonitor.class,
		de.tum.msrg.popsub.TestCommManager.class,
		de.tum.msrg.popsub.TestPopsubOverlay.class,
		de.tum.msrg.popsub.TestRateSampler.class,
		de.tum.msrg.popsub.TestUtilRatio.class,

		de.tum.msrg.sim.TestWorkloadGenerator.class,

		de.tum.msrg.topology.TestNodeInfo.class,

		de.tum.msrg.underlay.TestEdge.class,
		de.tum.msrg.underlay.TestNetwork.class,
		de.tum.msrg.underlay.TestNode.class,
		de.tum.msrg.underlay.TestTopology.class,
})

public class AllTests {
	public static double ASSERT_DELTA = 1e-5;
}
