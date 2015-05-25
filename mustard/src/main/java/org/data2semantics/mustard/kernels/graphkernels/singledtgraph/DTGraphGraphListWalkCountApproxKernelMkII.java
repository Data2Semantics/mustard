package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;



import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WalkCountApproxKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WalkCountKernelMkII;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.nodes.DTGraph;

/**
 * 
 * @author Gerben
 *
 */
public class DTGraphGraphListWalkCountApproxKernelMkII implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph>, ComputationTimeTracker {

	private int depth;
	
	private WalkCountApproxKernelMkII kernel;

	public DTGraphGraphListWalkCountApproxKernelMkII(int pathLength, int depth, int minFreq, boolean normalize) {
		this.depth = depth;
		this.kernel = new WalkCountApproxKernelMkII(pathLength, minFreq, normalize);
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
		GraphList<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(data.getGraph(), data.getInstances(), depth);		
		return kernel.computeFeatureVectors(graphs);
	}

	public double[][] compute(SingleDTGraph data) {
		GraphList<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(data.getGraph(), data.getInstances(), depth);		
		return kernel.compute(graphs);
	}
}
