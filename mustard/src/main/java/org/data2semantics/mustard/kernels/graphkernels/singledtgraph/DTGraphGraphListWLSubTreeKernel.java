package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;


import java.util.List;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.FeatureInspector;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.nodes.DTGraph;

public class DTGraphGraphListWLSubTreeKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph>, ComputationTimeTracker, FeatureInspector {
	private int depth;
	private int iterations;
	private boolean normalize;
	private boolean reverse;
	private boolean trackPrevNBH;
	private long compTime;
	private WLSubTreeKernel kernel;

	public DTGraphGraphListWLSubTreeKernel(int iterations, int depth, boolean reverse, boolean trackPrevNBH, boolean normalize) {
		this.reverse = reverse;
		this.trackPrevNBH = trackPrevNBH;
		this.depth = depth;
		this.iterations = iterations;
		this.normalize = normalize;
		
		kernel = new WLSubTreeKernel(iterations, reverse, trackPrevNBH, normalize);	
	}

	public String getLabel() {
		return KernelUtils.createLabel(this);		
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public long getComputationTime() {
		return compTime;
	}

	public SparseVector[] computeFeatureVectors(SingleDTGraph data) {
		GraphList<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(data.getGraph(), data.getInstances(), depth);				
		SparseVector[] ret =  kernel.computeFeatureVectors(graphs);
		compTime = kernel.getComputationTime();
		return ret;
	}


	public double[][] compute(SingleDTGraph data) {
		SparseVector[] featureVectors = computeFeatureVectors(data);
		double[][] kernel = KernelUtils.initMatrix(data.getInstances().size(), data.getInstances().size());
		long tic = System.currentTimeMillis();
		kernel = KernelUtils.computeKernelMatrix(featureVectors, kernel);
		compTime += System.currentTimeMillis() - tic;
		return kernel;
	}

	public List<String> getFeatureDescriptions(List<Integer> indicesSV) {
		return kernel.getFeatureDescriptions(indicesSV);
	}	
}
