package org.data2semantics.mustard.rdfvault;

import java.util.Map;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.SparseVector;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;


/**
 * Class implementing the Experiment URI Prefix kernel
 * 
 * @author Gerben *
 */
public class URIPrefixKernel implements GraphKernel<GraphList<DTGraph<String,String>>>, FeatureVectorKernel<GraphList<DTGraph<String,String>>>, ComputationTimeTracker {
	protected boolean normalize;
	private long compTime;
	private double lambda;
	
	public URIPrefixKernel(boolean normalize) {
		this(1.0, normalize);
	}
	
	public URIPrefixKernel(double lambda, boolean normalize) {
		this.lambda = lambda;
		this.normalize = normalize;
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

	public SparseVector[] computeFeatureVectors(GraphList<DTGraph<String,String>> data) {
		SparseVector[] featureVectors = new SparseVector[data.numInstances()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}
		
		StringTree st = new StringTree();
		
		long tic = System.currentTimeMillis();

		for (DTGraph<String,String> graph : data.getGraphs()) {
			for (DTNode<String,String> node : graph.nodes()) {
				st.store(node.label());
			}
			for (DTLink<String,String> link : graph.links()) {
				st.store(link.tag());
			}
 		}
		
		StringTree.PrefixStatistics stat = st.getPrefixStatistics(true);
		SparseVector norm = stat.getNormalization();
		
		for (int index : norm.getIndices()) {
			norm.setValue(index, Math.pow(norm.getValue(index), lambda));
		}
		
		int i = 0;
		for (DTGraph<String,String> graph : data.getGraphs()) {
			for (DTNode<String,String> node : graph.nodes()) {
				featureVectors[i].sumVector(stat.createSparseVector(node.label()));
			}
			for (DTLink<String,String> link : graph.links()) {
				featureVectors[i].sumVector(stat.createSparseVector(link.tag()));
			}
			
			// normalize
			for (int index : featureVectors[i].getIndices()) {
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) / norm.getValue(index));
			}			
			i++;
 		}	
		compTime = System.currentTimeMillis() - tic;
	
		if (normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}
		
		return featureVectors;
	}

	public double[][] compute(GraphList<DTGraph<String,String>> data) {
		double[][] kernel = KernelUtils.initMatrix(data.getGraphs().size(), data.getGraphs().size());
		kernel = KernelUtils.computeKernelMatrix(computeFeatureVectors(data), kernel);				
		return kernel;
	}
}
