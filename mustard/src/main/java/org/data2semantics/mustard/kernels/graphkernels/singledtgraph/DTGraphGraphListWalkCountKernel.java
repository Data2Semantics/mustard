package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;

/**
 * This class implements a WL kernel directly on an RDF graph. The difference with a normal WL kernel is that subgraphs are not 
 * explicitly extracted. However we use the idea of subgraph implicitly by tracking for each vertex/edge the distance from an instance vertex.
 * For one thing, this leads to the fact that 1 black list is applied to the entire RDF graph, instead of 1 (small) blacklist per graph. 
 * 
 *
 * 
 * @author Gerben
 *
 */
public class DTGraphGraphListWalkCountKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph> {

	private int depth;
	private int pathLength;
	private String label;
	private boolean normalize;

	public DTGraphGraphListWalkCountKernel(int pathLength, int depth, boolean normalize) {
		this.normalize = normalize;
		this.label = "RDF_DT_Graph_GraphList_PathCount_Kernel_" + pathLength + "_" + depth + "_" + normalize;

		this.depth = depth;
		this.pathLength = pathLength;
	}

	public String getLabel() {
		return label;
	}

	public void add2Label(String add) {
		this.label += add;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}


	public SparseVector[] computeFeatureVectors(SingleDTGraph data) {
		List<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(data.getGraph(), data.getInstances(), depth);		
		WalkCountKernel kernel = new WalkCountKernel(pathLength, normalize);	
		
		return kernel.computeFeatureVectors(new GraphList<DTGraph<String,String>>(graphs));	
	}


	public double[][] compute(SingleDTGraph data) {
		SparseVector[] featureVectors = computeFeatureVectors(data);
		double[][] kernel = KernelUtils.initMatrix(data.getInstances().size(), data.getInstances().size());
		computeKernelMatrix(featureVectors, kernel);
		return kernel;
	}

	private void computeKernelMatrix(SparseVector[] featureVectors, double[][] kernel) {
		for (int i = 0; i < featureVectors.length; i++) {
			for (int j = i; j < featureVectors.length; j++) {
				kernel[i][j] += featureVectors[i].dot(featureVectors[j]);
				kernel[j][i] = kernel[i][j];
			}
		}
	}
}
