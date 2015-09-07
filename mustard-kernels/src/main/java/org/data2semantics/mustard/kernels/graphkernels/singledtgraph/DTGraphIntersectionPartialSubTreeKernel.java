package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.nodes.DNode;

/**
 * Implementation of the Intersection Partial SubTree kernel, directly on the RDF graph, as suggested in the original paper.
 * 
 * @author Gerben
 *
 */
public class DTGraphIntersectionPartialSubTreeKernel extends
		DTGraphIntersectionSubTreeKernel {

	// Store them also in this class to generate a good label for the kernel
	private int depth; 
	private double discountFactor;
	private boolean normalize;

	public DTGraphIntersectionPartialSubTreeKernel(int depth, double discountFactor, boolean normalize) {
		super(depth, discountFactor, normalize);
		this.depth = depth;
		this.discountFactor = discountFactor;
		this.normalize = normalize;
	}

	@Override
	public String getLabel() {
		return KernelUtils.createLabel(this);		
	}
	
	@Override
	protected double subTreeScore(DNode<String> currentVertex, double discountFactor) {
		// Base case of recursion
		if (currentVertex.outDegree() == 0) {
			return 1.0;
		} else { // recursive case
			double score = 1;
			for (DNode<String> leaf: currentVertex.out()) {
				score *= discountFactor * subTreeScore(leaf, discountFactor) + 1;
			}
			return score;
		}
	}
}
