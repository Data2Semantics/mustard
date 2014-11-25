package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.util.LabelTagPair;
import org.data2semantics.mustard.util.HubUtils;

public class DTGraphHubRemovalWrapperKernel<K extends GraphKernel<SingleDTGraph>> implements GraphKernel<SingleDTGraph> {
	private boolean normalize;
	private int[] minHubSizes;
	private K kernel;
	
	public DTGraphHubRemovalWrapperKernel(K kernel, int[] minHubSizes, boolean normalize) {
		this.normalize = normalize;
		this.minHubSizes = minHubSizes;
		this.kernel = kernel;
	}

	public DTGraphHubRemovalWrapperKernel(K kernel, int minHubSize, boolean normalize) {
		this.normalize = normalize;
		this.minHubSizes = new int[1];
		this.minHubSizes[0] = minHubSize;
		this.kernel = kernel;
	}

	public String getLabel() {
		return KernelUtils.createLabel(this) +"_"+ kernel.getLabel();		
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public double[][] compute(SingleDTGraph data) {
		double[][] matrix = KernelUtils.initMatrix(data.numInstances(), data.numInstances());
		
		Map<LabelTagPair<String,String>, Integer> hubs = HubUtils.countLabelTagPairs(data);
		System.out.println("Total hubs: " + hubs.size());
		
		List<Entry<LabelTagPair<String,String>, Integer>> sorted = HubUtils.sortHubMap(hubs);	
		List<Integer> hubSizes = HubUtils.getHubSizes(sorted);
		System.out.println("Largest hub: " + hubSizes.get(0));
		
	
		for (int minCount : minHubSizes) {
			Map<LabelTagPair<String,String>, Integer> hubMap = HubUtils.createHubMapFromSortedLabelTagPairsMinCount(sorted, minCount);	
			SingleDTGraph g = HubUtils.removeHubs(data, hubMap);

			matrix = KernelUtils.sum(matrix, kernel.compute(g));
			System.out.println(hubMap.size() + ": #v " + g.getGraph().nodes().size() + ", #e " + g.getGraph().links().size());
		}

		if (this.normalize) {
			matrix = KernelUtils.normalize(matrix);
		}
		return matrix;
	}
}
