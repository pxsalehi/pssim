package de.tum.msrg.sim;

import java.util.Random;

import de.tum.msrg.sim.DistSampler.DistType;

public class NodeIDGenerator {
	private Random randGen;
	private DistType advDistOverNodes;
	private int advOverNodesStdDevFactor;
	private DistType subDistOverNodes;
	private int subOverNodesStdDevFactor;
	private DistType pubDistOverNodes;
	private int pubOverNodesStdDevFactor;

	public NodeIDGenerator(Random randGen, 
			               DistType advDistOverNodes, int advOverNodesStdDevFactor,
			               DistType subDistOverNodes, int subOverNodesStdDevFactor, 
			               DistType pubDistOverNodes, int pubOverNodesStdDevFactor) {
		this.randGen = randGen;
		this.advDistOverNodes = advDistOverNodes;
		this.advOverNodesStdDevFactor = advOverNodesStdDevFactor;
		this.subDistOverNodes = subDistOverNodes;
		this.subOverNodesStdDevFactor = subOverNodesStdDevFactor;
		this.pubDistOverNodes = pubDistOverNodes;
		this.pubOverNodesStdDevFactor = pubOverNodesStdDevFactor;
	}

	public int getNodeIDForSub(int noOfNodes) {
		return getNodeIDForSub(-1, noOfNodes);
	}

	public int getNodeIDForAdv(int noOfNodes) {
		return getNodeIDForAdv(-1, noOfNodes);
	}

	// TODO: add support for random distribution
	public int getNodeIDForSub(int subID, int noOfNodes) {
		int nodeID = 0;
		// all nodes must have at least one subscription
		if (subID >= 0) {
			if (subID < noOfNodes) {
				return subID;
			}
		}
		if (subDistOverNodes == DistType.NORMAL) {
			float mean = noOfNodes / 1.5f;
			// float mean = noOfNodes / 2f;
			// float mean = randGen.nextInt(noOfNodes);
			// System.out.println("subMean:" + mean);
			float stdDeviation = mean / subOverNodesStdDevFactor;
			do {
				double nodeIDTemp = randGen.nextGaussian() * stdDeviation + mean;
				nodeID = (int) Math.round(nodeIDTemp);
			} while ((nodeID < 0) || (nodeID >= noOfNodes));
		} else {
			nodeID = randGen.nextInt(noOfNodes);
		}
		return nodeID;
	}

	public int getNodeIDForAdv(int advID, int noOfNodes) {
		int nodeID = 0;
		if (advID >= 0) {
			// all nodes must have at least one subscription
			if (advID < noOfNodes) {
				return advID;
			}
		}
		if (advDistOverNodes == DistType.NORMAL) {
			float mean = noOfNodes / 3.0f;
			// float mean = noOfNodes / 2.0f;
			// float mean = randGen.nextInt(noOfNodes);
			// System.out.println("advMean:" + mean);
			float stdDeviation = mean / advOverNodesStdDevFactor;
			do {
				double nodeIDTemp = randGen.nextGaussian() * stdDeviation + mean;
				nodeID = (int) Math.round(nodeIDTemp);
			} while ((nodeID < 0) || (nodeID >= noOfNodes));
		} else {
			nodeID = randGen.nextInt(noOfNodes);
		}
		return nodeID;
	}

	public int getNodeIDForPub(int pubID, int noOfNodes) {
		int nodeID = 0;
		// all nodes must have at least one subscription
		if (pubID < noOfNodes) {
			return pubID;
		}
		if (pubDistOverNodes == DistType.NORMAL) {
			float mean = noOfNodes / 3.0f;
			// float mean = noOfNodes / 2.0f;
			// float mean = randGen.nextInt(noOfNodes);
			// System.out.println("pubMean:" + mean);
			float stdDeviation = mean / pubOverNodesStdDevFactor;
			do {
				double nodeIDTemp = randGen.nextGaussian() * stdDeviation + mean;
				nodeID = (int) Math.round(nodeIDTemp);
			} while ((nodeID < 0) || (nodeID >= noOfNodes));
		} else {
			nodeID = randGen.nextInt(noOfNodes);
		}
		return nodeID;
	}
}
