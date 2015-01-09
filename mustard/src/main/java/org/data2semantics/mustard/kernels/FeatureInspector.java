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
	 * Note that LibLINEAR starts at 1 with the support vectors, but SparseVector starts at 0!
	 * Implementations of this method are responsible for subtracting 1.
	 * 
	 * @param indices
	 * @return
	 */
	public List<String> getFeatureDescriptions(List<Integer> indicesSV);
	
}
