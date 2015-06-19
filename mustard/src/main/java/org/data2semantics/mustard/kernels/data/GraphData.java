package org.data2semantics.mustard.kernels.data;


/**
 * Marker interface to indicate whether something is GraphData, all kernels should be able to consume some form of GraphData
 * E.g. a list of graphs, a reference to a triplestore with a list of instances and a blacklist, etc.
 * 
 * @author Gerben
 *
 */
public interface GraphData {
	
	/**
	 * Indicates how many instances there are in the graph data, in terms of the learning task
	 * 
	 * @return
	 */
	public int numInstances();
}
