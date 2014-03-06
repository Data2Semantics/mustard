package org.data2semantics.mustard.kernels.graphkernels;

import org.nodes.DTNode;

/**
 * Implementation of the Intersection Partial SubTree kernel, directly on the RDF graph, as suggested in the original paper.
 * 
 * @author Gerben
 *
 */
public class RDFDTGraphIntersectionPartialSubTreeKernel extends
		RDFDTGraphIntersectionSubTreeKernel {


	public RDFDTGraphIntersectionPartialSubTreeKernel(int depth, double discountFactor, boolean normalize) {
		super(depth, discountFactor, normalize);
		this.label = "RDF Intersection Partial SubTree Kernel_" + depth + "_" + discountFactor + "_" + normalize;;
	}

	
	protected double subTreeScore(DTNode<String,String> currentVertex, double discountFactor) {
		// Base case of recursion
		if (currentVertex.outDegree() == 0) {
			return 1.0;
		} else { // recursive case
			double score = 1;
			for (DTNode<String,String> leaf: currentVertex.out()) {
				score *= discountFactor * subTreeScore(leaf, discountFactor) + 1;
			}
			return score;
		}
	}
}
