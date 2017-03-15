package de.tum.msrg.baseline;

import java.io.IOException;

import jist.runtime.JistAPI;
import de.tum.msrg.overlay.OverlayBase;
import de.tum.msrg.overlay.RuntimeSimException;
import de.tum.msrg.topology.Topology;
import de.tum.msrg.config.Configuration;
import de.tum.msrg.config.ConfigParserException;

public class BaselineOverlay extends OverlayBase<BaselineBroker> {
	
	protected long seed;
	protected long startPublishTime = -1;
	
	public BaselineOverlay(Configuration config, Topology topology, long seed) throws RuntimeSimException, ConfigParserException, IOException {
		super(config, topology, seed);
		this.seed = seed;
		startPublishTime = config.getLongConfig(ConfigKeys.SIM_START_PUBLISH_TIME);
		overlaySim = (new BaselineOverlayEntity()).getProxy();
	}
	
	@Override
	protected void initUnderlay() throws ConfigParserException {
		// instantiate underlay nodes and places brokers on underlay nodes
		brokers = new BaselineBroker[noOfNodes];
		int noOfPhysicalNodes = physicalNetwork.getNoOfNodes();
		int[] routerPerNode = new int[noOfPhysicalNodes];
		for (int i = 0; i < noOfNodes; i++) {
			int physicalNodeID = i;
			brokers[i] = new BaselineBroker(topology.getNode(i), physicalNetwork.getNode(physicalNodeID), this, config);
			routerPerNode[physicalNodeID]++;
		}
	}
	
	public class BaselineOverlayEntity implements OverlayBaseSimInterface {
		
		protected OverlayBaseSimInterface overlayInterface;

		public BaselineOverlayEntity() {
			overlayInterface = (OverlayBaseSimInterface) JistAPI.proxy(this, OverlayBaseSimInterface.class);
		}
		
		public OverlayBaseSimInterface getProxy() {
			return overlayInterface;
		}

		@Override
		public void startSim() {
			// stall publishing if specified so
			if(startPublishTime > 0)
				JistAPI.sleep(startPublishTime);
			overlayInterface.startPublishing();
		}
		
		public void startPublishing() {
			for (BaselineBroker node : brokers) {
				node.getNodeSim().startPublishing();
			}
		}
	}
}
