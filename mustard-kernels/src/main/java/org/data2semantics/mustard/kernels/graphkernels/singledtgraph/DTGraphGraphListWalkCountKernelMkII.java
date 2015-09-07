package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;



import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WalkCountKernelMkII;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.nodes.DTGraph;

/**
 * 
 * @author Gerben
 *
 */
public class DTGraphGraphListWalkCountKernelMkII implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph>, ComputationTimeTracker {

	private int depth;
	private int pathLength;
	private boolean normalize;
	private long compTime;

	public DTGraphGraphListWalkCountKernelMkII(int pathLength, int depth, boolean normalize) {
		this.normalize = normalize;
		this.depth = depth;
		this.pathLength = pathLength;
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
		WalkCountKernelMkII kernel = new WalkCountKernelMkII(pathLength, normalize);
		SparseVector[] ret = kernel.computeFeatureVectors(graphs);
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
