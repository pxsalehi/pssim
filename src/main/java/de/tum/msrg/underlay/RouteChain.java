package de.tum.msrg.underlay;

import java.util.ArrayList;

import de.tum.msrg.utils.Utility;

/**
 * @author p00ya
 * An object that records a path in the underlay network
 */
public class RouteChain {

	private ArrayList<Node> routerChain;

	private ArrayList<Float> delayChain;

	private ArrayList<Float> bwChain;

	public RouteChain(int capacity) {
		// NOTE: routerChain is one item longer than bwChain and delayChain
		routerChain = new ArrayList<Node>(capacity + 1);
		delayChain = new ArrayList<Float>(capacity);
		bwChain = new ArrayList<Float>(capacity);
	}

	public void addLink(Node fromNode, Edge link) {
		Node toNode = link.getOtherEndNode(fromNode);
		if (routerChain.size() == 0)
			routerChain.add(fromNode);
		routerChain.add(toNode);
		delayChain.add(new Float(link.getDelay()));
		bwChain.add(new Float(link.getBandwidth()));
	}

	public void trimToSize() {
		routerChain.trimToSize();
		delayChain.trimToSize();
		bwChain.trimToSize();
	}

	public ArrayList<Node> getRouterChain() {
		return routerChain;
	}

	public int getRouteLength() {
		return routerChain.size();
	}

	public void addRouter(Node router) {
		routerChain.add(router);
	}

	public ArrayList<Float> getDelayChain() {
		return delayChain;
	}

	public void addDelayVal(float delayVal) {
		delayChain.add(new Float(delayVal));
	}

	public ArrayList<Float> getBWChain() {
		return bwChain;
	}

	public void addBWVal(float bwVal) {
		bwChain.add(new Float(bwVal));
	}

	public void reverse() {
		Utility.reverse(routerChain);
		Utility.reverse(delayChain);
		Utility.reverse(bwChain);
	}

	public boolean checkValid() {
		if (bwChain.size() == 0 || delayChain.size() == 0
				|| routerChain.size() == 0)
			return false;
		return true;
	}

	public String getRouterChainStr() {
		String outStr = "";
		for (Node router : routerChain)
			outStr += String.format("%d ", router.getID());
		return outStr;
	}

	public boolean equals(RouteChain anotherChain) {
		ArrayList<Node> otherRouterChain = anotherChain.getRouterChain();
		if (routerChain.size() != otherRouterChain.size())
			return false;
		for (int i = 0; i < routerChain.size(); i++)
			if (routerChain.get(i) != otherRouterChain.get(i))
				return false;
		return true;
	}

	@Override
	public String toString() {
		String str = "Nodes: ";
		for(Node n: routerChain)
			str += n.getID() + "  ";
		str += "\n";
		str += "Delay chain: ";
		for(Float f: delayChain)
			str += f + "  ";
		str += "\n";
		str += "BW chain: ";
		for(Float f: bwChain)
			str += f + "  ";
		return str;
	}

}
