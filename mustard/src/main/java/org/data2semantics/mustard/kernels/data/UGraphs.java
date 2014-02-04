package org.data2semantics.mustard.kernels.data;

import java.util.List;

import org.nodes.UGraph;


/**
 * class to represent graph data in the form of a list of UGraph's
 * 
 * @author Gerben
 */
public class UGraphs implements GraphData {
	private List<UGraph<String>> graphs;

	public UGraphs(List<UGraph<String>> graphs) {
		super();
		this.graphs = graphs;
	}

	public List<UGraph<String>> getGraphs() {
		return graphs;
	}
}
