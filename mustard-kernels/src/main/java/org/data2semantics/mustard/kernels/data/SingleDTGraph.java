package org.data2semantics.mustard.kernels.data;

import java.util.List;

import org.nodes.DTGraph;
import org.nodes.DTNode;


/**
 * Class to represent graph data that is as one (RDF) graph, with a list of instance nodes in that one graph.
 * The graph used is a DTGraph, which is a directed multigraph with labels on the nodes and links.
 * 
 * @author Gerben
 *
 */
public class SingleDTGraph implements GraphData {
	private DTGraph<String,String> graph;
	private List<DTNode<String,String>> instances;

	public SingleDTGraph(DTGraph<String, String> graph,
			List<DTNode<String, String>> instances) {
		super();
		this.graph = graph;
		this.instances = instances;
	}

	public DTGraph<String, String> getGraph() {
		return graph;
	}

	public List<DTNode<String, String>> getInstances() {
		return instances;
	}


	/**
	 * returns the size of the instances list.
	 * 
	 */
	public int numInstances() {
		return instances.size();
	}


}
