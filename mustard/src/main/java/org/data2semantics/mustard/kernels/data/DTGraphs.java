package org.data2semantics.mustard.kernels.data;

import java.util.List;

import org.nodes.DTGraph;


/**
 * class to represent graph data in the form of a list of DTGraph's
 * 
 * @author Gerben
 */
public class DTGraphs implements GraphData {
	private List<DTGraph<String, String>> graphs;

	public DTGraphs(List<DTGraph<String, String>> graphs) {
		super();
		this.graphs = graphs;
	}

	public List<DTGraph<String, String>> getGraphs() {
		return graphs;
	}
}
