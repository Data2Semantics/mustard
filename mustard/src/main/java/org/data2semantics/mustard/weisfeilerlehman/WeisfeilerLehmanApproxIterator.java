package org.data2semantics.mustard.weisfeilerlehman;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Class implementing the different steps of the Weisfeiler-Lehman Approx algorithm for different types of graphs
 * 
 * @author Gerben
 *
 */
public abstract class WeisfeilerLehmanApproxIterator<G,L> {
	protected Map<String,String> labelDict;
	protected double minFreq;
	protected int maxLabelCard;

	public WeisfeilerLehmanApproxIterator(int maxLabelCard, double minFreq) {
		this.labelDict = new HashMap<String,String>();
		this.maxLabelCard = maxLabelCard;
		this.minFreq = minFreq;
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
	public abstract void wlIterate(List<G> graphs, Map<L, Double> labelFreq);

	
	public void setMinFreq(double minFreq) {
		this.minFreq = minFreq;
	}

	public void setMaxLabelCard(int maxLabelCard) {
		this.maxLabelCard = maxLabelCard;
	}	
}
