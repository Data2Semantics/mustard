package org.data2semantics.mustard.rdfvault;


import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.nodes.DTGraph;

public class DTGraphGraphListURIPrefixKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph>, ComputationTimeTracker {
	private int depth;
	private long compTime;
	private URIPrefixKernel kernel;

	public DTGraphGraphListURIPrefixKernel(int depth, boolean normalize) {
		this.depth = depth;
		
		kernel = new URIPrefixKernel(normalize);	
	}

	public String getLabel() {
		return KernelUtils.createLabel(this);		
	}

	public long getComputationTime() {
		return compTime;
	}

	public void setNormalize(boolean normalize) {
		kernel.setNormalize(normalize);
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

}
