package org.data2semantics.mustard.weisfeilerlehman;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Class implementing the different steps of the Weisfeiler-Lehman algorithm for different types of graphs
 * 
 * @author Gerben
 *
 */
public abstract class WeisfeilerLehmanIterator<G> {
	protected Map<String,String> labelDict;

	public WeisfeilerLehmanIterator() {
		this.labelDict = new HashMap<String,String>();
	}

	public Map<String, String> getLabelDict() {
		return labelDict;
	}
	
	// Set labels from 0 to n
	public abstract void wlInitialize(List<G> graphs);
	
	// One iteration of the WL algorithm on the list of graphs supplied during initialization
	// Use the labels to create needed buckets on the fly, since there is no garantee that they run neatly from i to i+n
	public abstract void wlIterate(List<G> graphs);
		
	/*
	 * Maybe they are better as objects, such that each can store its own dictionary for instance.
	 * This is likely easier for future version when we want to include computing again for test data separately
	 * 
	 * 
	 */
	

	// Do we copy the graph(s)?
	// NO, the algorithm assumes graphs that is allowed to edit, i.e. have StringLabel or MapLabel labels and tags
	
	// WL set of undirected graphs
	
	// WL set of directed graphs (fwd, rev, bi)
	
	// WL single directed graph with depth labels (fwd, rev, bi)
	
	
	
}
