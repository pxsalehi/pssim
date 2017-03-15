package de.tum.msrg.overlay;

import de.tum.msrg.baseline.ConfigKeys;
import de.tum.msrg.config.ConfigParserException;
import de.tum.msrg.config.Configuration;
import de.tum.msrg.topology.NodeInfo;
import de.tum.msrg.utils.Tuple;
import jist.runtime.JistAPI.Proxiable;
import de.tum.msrg.sim.WorkloadGenerator;
import de.tum.msrg.topology.Topology;
import de.tum.msrg.underlay.Edge;
import de.tum.msrg.underlay.Network;
import de.tum.msrg.underlay.UnderlayException;
import de.tum.msrg.utils.SimLogger;

import java.io.IOException;
import java.util.*;

public abstract class OverlayBase<BrokerType extends BrokerBase> {
	// Overlay simulation interface
	public interface OverlayBaseSimInterface extends Proxiable {
		void startSim();
		void startPublishing();
	}
	protected Configuration config;
	protected int noOfNodes;
	protected int noOfP;
	protected int noOfS;
	protected Random randGen;
	// underlying physical network
	protected Network physicalNetwork;
	// data structures
	protected BrokerType[] brokers;
	protected int[][] spanningTree;
	protected WorkloadGenerator workloadGen;
	protected Topology topology;
	// JiST simulation entity
	protected OverlayBaseSimInterface overlaySim;
	//protected boolean applyLoadDelay;
	// whether teh pub/sub system is advertisement-based,
	// (false = subscription flooding)
	private boolean isAdvBased = false;
	// apply load delay, if overloaded msgs experience extra latency
	private boolean applyLoadDelay = false;
	// set of all paths in the overlay between any two broker
	public Map<Tuple<Integer, Integer>, Set<BrokerBase>> allPaths = null;
	
	
	public OverlayBase(Configuration config, Topology topology, long seed) throws RuntimeSimException, IOException {
		this(config, topology, new Random(seed));
	}

	public OverlayBase(Configuration config, Topology topology, Random randGen) throws RuntimeSimException, IOException {
		try {
			// read configurations
			this.config = config;
			this.topology = topology;
			setConfigurationParameters(config);
			String simDir = config.getStringConfig(ConfigKeys.SIM_DIR);
			physicalNetwork = new Network(config, topology);
			// build the routing tables in the physical network
			physicalNetwork.buildRouteTables();
			this.randGen = randGen;
			// initialize the overlay connection data structures
			spanningTree = new int[noOfNodes][noOfNodes];
			Router.isAdvBased = isAdvBased;
			initUnderlay();
		} catch (ConfigParserException e) {
			e.printStackTrace();
			throw new RuntimeSimException(e.getCause());
		} catch (UnderlayException e) {
			throw new RuntimeSimException(e.getCause());
		}
	}

	protected abstract void initUnderlay() throws ConfigParserException;

	protected void setConfigurationParameters(Configuration config) throws ConfigParserException {
		noOfNodes = config.getIntConfig(ConfigKeys.SIM_NO_OF_NODES);
		noOfP = config.getIntConfig(ConfigKeys.SIM_NO_OF_ADVS);
		noOfS = config.getIntConfig(ConfigKeys.SIM_NO_OF_SUBS);
		isAdvBased = config.getBooleanConfig(ConfigKeys.SIM_IS_ADV_BASED);
		applyLoadDelay = config.getBooleanConfig(ConfigKeys.SIM_APPLY_LOAD_DELAY);
		BrokerOutput.applyLoadDelay = applyLoadDelay;
	}

	public Network getPhysicalNetwork() {
		return physicalNetwork;
	}

	public int getNoOfNodes() {
		return noOfNodes;
	}

	public BrokerType[] getNodes() {
		return brokers;
	}

	public BrokerType getBroker(int id) {
		return getNodes()[id];
	}

	public BrokerType getBroker(NodeInfo node) {
		return getBroker(node.getId());
	}

	public Random getRandGen() {
		return randGen;
	}

	public void initOverlay() throws RuntimeSimException {
		BrokerBase.applyLoadDelay = applyLoadDelay;
		System.out.println("Apply load delay: " + applyLoadDelay);
		SimLogger.info("Calculating spanning tree...");
		findSpanningTree();
		// add overlay links
		SimLogger.info("Adding overlay links...");
		addOverlayLinks();
		// set the overlay routes
		SimLogger.info("Setting overlay routes...");
		//setOverlayRoutes();
		setOverlayRoutesFromUnderlay();
		findAllPaths();
	}

	public void findSpanningTree() throws RuntimeSimException {
		// create same tree as physical layer
		for(BrokerType pNode: getNodes()) {
			for(Edge e: pNode.getUnderlayNode().getOutLinks()) {
				int i = pNode.getId();
				int j = e.getOtherEndNode(pNode.getUnderlayNode()).getID();
				spanningTree[i][j] = 1;
			}
		}
	}

	protected void setOverlayRoutesFromUnderlay() {
		// create same overlay routing table as the brokers underlay
		for(BrokerType src: getNodes()) {
			int[] table = src.getUnderlayNode().getRouteTable();
			int fromID = src.getId();
			for(int toID = 0; toID < table.length; ++toID) {
				BrokerType nextHop = null;
				BrokerType dest = brokers[toID];
				if(fromID != toID)
					nextHop = brokers[table[toID]];
				src.addOverlayRoute(nextHop, dest);
			}
		}
	}

	private void addOverlayLinks() throws RuntimeSimException {
		// create overlay links
		for (int i = 0; i < noOfNodes; i++) {
			BrokerType fromNode = getBroker(i);
			for (int j = i + 1; j < noOfNodes; j++) {
				BrokerType toNode = getBroker(j);
				if ((spanningTree[i][j] == 1) || (spanningTree[j][i] == 1)) {
					try {
						int latency = fromNode.getUnderlayNode().getLatency(toNode.getUnderlayNode());
						OverlayLink link = new OverlayLink(fromNode, toNode, latency);
						fromNode.addOverlayLink(link);
						toNode.addOverlayLink(link);
					} catch (UnderlayException e) {
						throw new RuntimeSimException(e.getCause());
					}
				}
			}
		}
	}
	
	public void printSubscriptions() {
		for (BrokerType oNode : getNodes()) {
			System.out.println("Subscriptions on node: " + oNode.getId() + ":");
			oNode.getPubSubLoad().printAllDetails();
		}
	}

	public void printSpanningTree() {
		for (int i = 0; i < noOfNodes; i++) {
			System.out.printf("%5d: ", i);
			for (int j = 0; j < noOfNodes; j++) {
				System.out.printf("%2d", spanningTree[i][j]);
			}
			System.out.println();
		}
	}

	public void printOverlay() {
		System.out.println("# [node_id] pref_sub_class\t no_of_subs");
		for (BrokerType oNode : getNodes()) {
			System.out.println(oNode);
		}
	}
	
	public OverlayBaseSimInterface getSimInterface() {
		return overlaySim;
	}
	
	public boolean isAdvBased() {
		return isAdvBased;
	}

	/**
	 * calculate all end-to-end paths in the overlay
	 * path(bi, bi) = empty list
	 * path(bi, bj) = [bi, ..., bj]
	 */
	public void findAllPaths() {
		allPaths = new HashMap<Tuple<Integer, Integer>, Set<BrokerBase>>();
		// use linked hashset to maintain insert order like a list and perform fast contain()
		LinkedHashSet<BrokerBase> path;
		BrokerBase next, dest;
		for(int i = 0; i < noOfNodes; ++i) {
			for(int j = 0; j < noOfNodes; ++j) {
				path = new LinkedHashSet<BrokerBase>();
				if(i != j) {
					dest = getBroker(j);
					// get path from node_i to node_j
					next = getBroker(i);
					while (next != null) {
						path.add(next);
						next = next.getNextNodeToRoute(dest);
					}
				}
				allPaths.put(new Tuple(i, j), path);
			}
		}
	}

	public Set<BrokerBase> getPathSteps(BrokerBase src, BrokerBase dest) {
		if(allPaths == null)
			throw new RuntimeException("Overlay paths must first be computed by calling findAllPaths!");
		Set<BrokerBase> res = allPaths.get(new Tuple(src.getId(), dest.getId()));
		return res;
	}

	public Set<BrokerBase> getPathSteps(int srcId, int destId) {
		return getPathSteps(getBroker(srcId), getBroker(destId));
	}

	public Map<Tuple<Integer, Integer>, Set<BrokerBase>> getAllPaths() {
		return allPaths;
	}
}
