package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.data2semantics.mustard.util.LabelTagPair;
import org.data2semantics.mustard.util.HubUtils;
import org.nodes.DTGraph;
import org.nodes.DTNode;

public class DTGraphHubRemovalWrapperFeatureVectorKernel<K extends FeatureVectorKernel<SingleDTGraph>> implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph> {
	private String label;
	private boolean normalize;
	private int minHubSize;
	private int stepFactor;
	private K kernel;

	public DTGraphHubRemovalWrapperFeatureVectorKernel(K kernel, int minHubSize, int stepFactor, boolean normalize) {
		this.label = "DT_Graph_HubRemoval_Wrapper_" + kernel.getLabel() + "_" + minHubSize + "_" + stepFactor + "_" + normalize;
		this.normalize = normalize;
		this.minHubSize = minHubSize;
		this.stepFactor = stepFactor;
		this.kernel = kernel;
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
		SparseVector[] fvs = kernel.computeFeatureVectors(data);

		Map<LabelTagPair<String,String>, Integer> hubs = HubUtils.countLabelTagPairs(data);
		List<Entry<LabelTagPair<String,String>, Integer>> sorted = HubUtils.sortHubMap(hubs);
		
		System.out.println("0: #v " + data.getGraph().nodes().size() + ", #e " + data.getGraph().links().size());

		List<Integer> minCounts = new ArrayList<Integer>();
		for (int minC = minHubSize; minC < sorted.get(0).getValue(); minC *= stepFactor) {
			minCounts.add(minC);
		}

		for (int minCount : minCounts) {
			Map<LabelTagPair<String,String>, Integer> hubMap = HubUtils.createHubMapFromSortedLabelTagPairsMinCount(sorted, minCount);
			
			SingleDTGraph g = HubUtils.removeHubs(data, hubMap);

			SparseVector[] fvs2 = kernel.computeFeatureVectors(g);
			for (int j = 0; j < fvs.length; j++) {
				fvs[j].addVector(fvs2[j]);
			}
			System.out.println(hubMap.size() + ": #v " + g.getGraph().nodes().size() + ", #e " + g.getGraph().links().size());
		}

		if (this.normalize) {
			fvs = KernelUtils.normalize(fvs);
		}
		return fvs;
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
