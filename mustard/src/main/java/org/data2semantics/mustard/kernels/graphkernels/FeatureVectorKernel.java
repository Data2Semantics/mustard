package org.data2semantics.mustard.kernels.graphkernels;

import org.data2semantics.mustard.kernels.Kernel;
import org.data2semantics.mustard.kernels.data.GraphData;
import org.data2semantics.mustard.learners.SparseVector;


/**
 * Interface describing a FeatureVectorKernel. Such kernel's should be able to compute SparseVector on the input data (instead of a kernel matrix).
 * 
 * TODO add computeFV method for train and test split
 * 
 * @author Gerben
 *
 */
public interface FeatureVectorKernel<G extends GraphData> extends Kernel {
	public SparseVector[] computeFeatureVectors(G data);
	//public SparseVector[] computeFeatureVectors(List<G> trainGraphs, List<G> testGraphs);
}
