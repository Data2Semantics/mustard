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
	
	/**
	 * Set labels from 0 to n
	 * 
	 * @param graphs
	 */
	public abstract void wlInitialize(List<G> graphs);
	
	/**
	 * One iteration of the WL algorithm on the list of graphs supplied during initialization
	 * Use the labels to create needed buckets on the fly, since there is no garantee that they run neatly from i to i+n
	 * 
	 * @param graphs
	 */
	public abstract void wlIterate(List<G> graphs);
		

	
	
}
