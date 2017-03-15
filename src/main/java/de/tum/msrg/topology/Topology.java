package de.tum.msrg.topology;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

/*
 * Represents a network topology which is a set of nodes and edges
 */
public class Topology {
	private int noOfNodes;
	private int noOfEdges;
	private NodeInfo[] nodes;
	private EdgeInfo[] edges;

	public Topology() {
	}
	
	public Topology(NodeInfo[] nodes, EdgeInfo[] edges) {
		noOfNodes = nodes.length;
		noOfEdges = edges.length;
		this.nodes = nodes.clone();
		this.edges = edges.clone();
	}

	public Topology(FileReader topoFile) throws IOException {
		BufferedReader buffReader = new BufferedReader(topoFile);
		String line, content = "";
		while ((line = buffReader.readLine()) != null)
			content += line + "\n";
		buffReader.close();
		loadFromString(content);
	}
	
	public Topology(String topoString) {
		loadFromString(topoString);
	}
	
	private void loadFromString(String content) {
		Scanner topoFile = new Scanner(content);
		String line = topoFile.nextLine();
		String[] lineParts = line.split(":");
		noOfNodes = Integer.parseInt(lineParts[1].trim());
		nodes = new NodeInfo[noOfNodes];
		// second line has information about number of edges
		line = topoFile.nextLine();
		lineParts = line.split(":");
		noOfEdges = Integer.parseInt(lineParts[1].trim());
		edges = new EdgeInfo[noOfEdges];
		// skip until the start of nodes data
		do {
			line = topoFile.nextLine();
		} while (!line.startsWith("Nodes:"));
		// read the node information
		readNodes(topoFile);
		// skip until the start of edges data
		do {
			line = topoFile.nextLine();
		} while (!line.startsWith("Edges:"));
		// read the edge information
		readEdges(topoFile);
		topoFile.close();
	}
	
	private void readNodes(Scanner inFile) {
		while (true) {
			String line = inFile.nextLine();
			if (line.trim().length() == 0 || line.startsWith("Edges:"))
				break;
			// line format: node_id no_of_neighbors
			StringTokenizer st = new StringTokenizer(line.trim());
			short id = Short.parseShort(st.nextToken());
			short outDegree = Short.parseShort(st.nextToken());
			long capacity = Long.MAX_VALUE;
			if(st.hasMoreTokens())
				capacity = Long.parseLong(st.nextToken());
			NodeInfo newNode = new NodeInfo(id, outDegree, capacity);
			nodes[id] = newNode;
		}
	}

	private void readEdges(Scanner inFile) {
		while (true) {
			String line = null;
			try { // if the content doesn't have an empty line at the end
				line = inFile.nextLine();
				// line format: edge_id from_node_id to_node_id link_delay
				StringTokenizer st = new StringTokenizer(line.trim());
				short id = Short.parseShort(st.nextToken());
				NodeInfo fromNode = nodes[Integer.parseInt(st.nextToken())];
				NodeInfo toNode = nodes[Integer.parseInt(st.nextToken())];
				int delay = Integer.parseInt(st.nextToken());
				int bw = Integer.MAX_VALUE;
				if(st.hasMoreTokens())
					bw = Integer.parseInt(st.nextToken());
				EdgeInfo newEdge = new EdgeInfo(id, fromNode, toNode, bw, delay);
				edges[id] = newEdge;
				fromNode.addNeighbor(toNode);
				toNode.addNeighbor(fromNode);
			} catch(NoSuchElementException e) {
				break;
			}
			if (line == null || line.startsWith("Edges:") || line.trim().length() == 0)
				break;
		}
	}

	public int getNoOfNodes() {
		return noOfNodes;
	}

	public void setNoOfNodes(int noOfNodes) {
		this.noOfNodes = noOfNodes;
	}

	public int getNoOfEdges() {
		return noOfEdges;
	}

	public void setNoOfEdges(int noOfEdges) {
		this.noOfEdges = noOfEdges;
	}

	public NodeInfo[] getNodes() {
		return nodes;
	}

	public NodeInfo getNode(int id) {
		return nodes[id];
	}

	public void setNodes(NodeInfo[] nodes) {
		this.nodes = nodes;
	}

	public EdgeInfo[] getEdges() {
		return edges;
	}

	public void setEdges(EdgeInfo[] edges) {
		this.edges = edges;
	}

	public static void main(String[] args) throws IOException {
		if(args.length < 1) {
			System.out.println("Topology [file.text]");
			System.exit(0);
		}
		FileReader file = new FileReader(args[0]);
		System.out.printf("Reading topology from %s ...\n", args[0]);
		Topology topo = new Topology(file);
		System.out.println("Nodes:");
		for(NodeInfo node: topo.getNodes())
			System.out.printf("\t%s\n", node);
		System.out.println("Edges:");
		for(EdgeInfo edge: topo.getEdges())
			System.out.printf("\t%s\n", edge);
		file.close();
	}
}
