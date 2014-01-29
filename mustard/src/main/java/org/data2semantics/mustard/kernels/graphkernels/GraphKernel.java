package org.data2semantics.mustard.kernels.graphkernels;

import java.util.List;

import org.data2semantics.mustard.kernels.Kernel;




/**
 * GraphKernel interface, compute a kernel matrix on a list of DirectedMultigraphWithRoot's
 * 
 * TODO add compute for train and test split
 * 
 */
public interface GraphKernel<G> extends Kernel {
		
	public double[][] compute(List<G> trainGraphs);
	//public double[][] compute(List<G> trainGraphs, List<G> testGraphs);
	
}
