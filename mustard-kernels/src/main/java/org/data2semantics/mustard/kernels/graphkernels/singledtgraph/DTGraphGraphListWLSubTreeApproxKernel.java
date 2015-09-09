package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;


import java.util.List;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.FeatureInspector;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeApproxKernel;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.data2semantics.mustard.weisfeilerlehman.ApproxStringLabel;
import org.nodes.DTGraph;

/**
 * Wrapper for {@link org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeApproxKernel}.
 * 
 * @author Gerben
 *
 */
public class DTGraphGraphListWLSubTreeApproxKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph>, ComputationTimeTracker, FeatureInspector {
	private int depth;
	private WLSubTreeApproxKernel kernel;

	public DTGraphGraphListWLSubTreeApproxKernel(int iterations, int depth, boolean reverse, boolean noDuplicateSubtrees, int[] maxPrevNBHs, int[] maxLabelCards, int[] minFreqs, boolean normalize) {
		this.depth = depth;
		
		kernel = new WLSubTreeApproxKernel(iterations, reverse, noDuplicateSubtrees, maxPrevNBHs, maxLabelCards, minFreqs, normalize);	
	}

	public String getLabel() {
		return KernelUtils.createLabel(this) + "_" + kernel.getLabel();		
	}

	public void setNormalize(boolean normalize) {
		kernel.setNormalize(normalize);
	}

	public long getComputationTime() {
		return kernel.getComputationTime();
	}

	public org.data2semantics.mustard.kernels.SparseVector[] computeFeatureVectors(SingleDTGraph data) {
		GraphList<DTGraph<ApproxStringLabel,ApproxStringLabel>> graphs = RDFUtils.getSubGraphsApproxStringLabel(data.getGraph(), data.getInstances(), depth);				
		return kernel.computeFeatureVectors(graphs);
	}

	public double[][] compute(SingleDTGraph data) {
		GraphList<DTGraph<ApproxStringLabel,ApproxStringLabel>> graphs = RDFUtils.getSubGraphsApproxStringLabel(data.getGraph(), data.getInstances(), depth);				
		return kernel.compute(graphs);
	}

	public List<String> getFeatureDescriptions(List<Integer> indicesSV) {
		return kernel.getFeatureDescriptions(indicesSV);
	}	
}
