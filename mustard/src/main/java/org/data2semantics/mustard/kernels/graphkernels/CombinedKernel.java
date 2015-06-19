package org.data2semantics.mustard.kernels.graphkernels;

import java.util.List;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.GraphData;


/**
 * A simple graph kernel to combine different graph kernels, currently we do not even implement a weighing of the different kernels.
 * 
 * @author Gerben
 *
 */
public class CombinedKernel<G extends GraphData> implements GraphKernel<G> {
	private boolean normalize;
	private List<GraphKernel<G>> kernels;

	public CombinedKernel(List<GraphKernel<G>> kernels, boolean normalize) {
		this.kernels = kernels;
		this.normalize = normalize;
	}

	public String getLabel() {
		String label = KernelUtils.createLabel(this);
		for (GraphKernel<G> k : kernels) {
			label += "_" + k.getLabel();
		}
		return label;	
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public double[][] compute(G data) {
		double[][] kernel = KernelUtils.initMatrix(data.numInstances(), data.numInstances());
		
		for (GraphKernel<G> k : kernels) {
			double[][] kTemp = k.compute(data);
			for (int i = 0; i < data.numInstances(); i++) {
				for (int j = i; j < data.numInstances(); j++) {
					kernel[i][j] += kTemp[i][j];
					kernel[j][i] = kernel[i][j];
				}
			}
		}
		
		if (normalize) {
			kernel = KernelUtils.normalize(kernel);
		}
		
		return kernel;
	}

}
