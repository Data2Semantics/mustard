package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.util.LabelTagPair;
import org.data2semantics.mustard.util.HubUtils;

public class DTGraphHubRemovalWrapperFeatureVectorKernel<K extends FeatureVectorKernel<SingleDTGraph>> implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph> {
	private boolean normalize;
	private int[] minHubSizes;
	private K kernel;
	
	private double[] fractions = {0.01, 0.025, 0.05, 0.10, 1.1};
	
	public DTGraphHubRemovalWrapperFeatureVectorKernel(K kernel, int[] minHubSizes, boolean normalize) {
		this.normalize = normalize;
		this.minHubSizes = minHubSizes;
		this.kernel = kernel;
	}

	public DTGraphHubRemovalWrapperFeatureVectorKernel(K kernel, int minHubSize, boolean normalize) {
		this.normalize = normalize;
		this.minHubSizes = new int[1];
		this.minHubSizes[0] = minHubSize;
		this.kernel = kernel;
	}

	public String getLabel() {
		return KernelUtils.createLabel(this) + "_" + kernel.getLabel();		
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public SparseVector[] computeFeatureVectors(SingleDTGraph data) {
		SparseVector[] fvs = new SparseVector[data.numInstances()];
		
		for (int i = 0; i < fvs.length; i++) {
			fvs[i] = new SparseVector();
		}

		Map<LabelTagPair<String,String>, Integer> hubs = HubUtils.countLabelTagPairs(data);
		System.out.println("Total hubs: " + hubs.size());
		
		List<Entry<LabelTagPair<String,String>, Integer>> sorted = HubUtils.sortHubMap(hubs);	
		List<Integer> hubSizes = HubUtils.getHubSizes(sorted);
		System.out.println("Largest hub: " + hubSizes.get(0));
		

		for (int minCount : minHubSizes) {
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
		kernel = KernelUtils.computeKernelMatrix(featureVectors, kernel);
		return kernel;
	}
}
