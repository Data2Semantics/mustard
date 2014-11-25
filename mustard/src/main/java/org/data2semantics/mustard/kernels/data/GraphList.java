package org.data2semantics.mustard.kernels.data;

import java.util.List;


/**
 * class to represent graph data in the form of a list of Graph's
 * 
 * @author Gerben
 */
public class GraphList<G> implements GraphData {
	private List<G> graphs;

	public GraphList(List<G> graphs) {
		super();
		this.graphs = graphs;
	}

	public List<G> getGraphs() {
		return graphs;
	}

	public int numInstances() {
		return graphs.size();
	}


}
