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
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.data2semantics.mustard.weisfeilerlehman.StringLabel;
import org.nodes.DTGraph;

public class DTGraphGraphListWLSubTreeApproxKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph>, ComputationTimeTracker, FeatureInspector {
	private int depth;
	private WLSubTreeApproxKernel kernel;

	public DTGraphGraphListWLSubTreeApproxKernel(int iterations, int depth, boolean reverse, boolean trackPrevNBH, boolean skipSamePrevNBH, int maxLabelCard, double minFreq, double depthWeight, boolean normalize) {
		this.depth = depth;
		
		kernel = new WLSubTreeApproxKernel(iterations, reverse, trackPrevNBH, skipSamePrevNBH, maxLabelCard, minFreq, depthWeight, normalize);	
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

	public SparseVector[] computeFeatureVectors(SingleDTGraph data) {
		GraphList<DTGraph<StringLabel,StringLabel>> graphs = RDFUtils.getSubGraphsStringLabel(data.getGraph(), data.getInstances(), depth);				
		SparseVector[] ret =  kernel.computeFeatureVectors(graphs);
		return ret;
	}


	public double[][] compute(SingleDTGraph data) {
		GraphList<DTGraph<StringLabel,StringLabel>> graphs = RDFUtils.getSubGraphsStringLabel(data.getGraph(), data.getInstances(), depth);				
		return kernel.compute(graphs);
	}

	public List<String> getFeatureDescriptions(List<Integer> indicesSV) {
		return kernel.getFeatureDescriptions(indicesSV);
	}	
}
