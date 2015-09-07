package org.data2semantics.mustard.kernels;

import java.util.List;


/**
 * Simple interface to mark that a Kernel allows for inspection of the features, given a list of indices into the feature vector
 * 
 * @author Gerben
 *
 */
public interface FeatureInspector {

	/**
	 * Return a list of Strings describing the features with the given Support Vector indices
	 * 
	 * @param indices
	 * @return
	 */
	public List<String> getFeatureDescriptions(List<Integer> indicesSV);
	
}
