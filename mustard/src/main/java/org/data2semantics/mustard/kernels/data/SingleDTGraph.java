package org.data2semantics.mustard.kernels.data;

import java.util.List;

import org.nodes.DTGraph;
import org.nodes.DTNode;


/**
 * Class to represent graph data that is as one (RDF) graph, with a list of instance nodes
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
}
