package org.data2semantics.mustard.experiments.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.Kernel;
import org.data2semantics.mustard.kernels.data.GraphData;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.learners.Prediction;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.liblinear.LibLINEAR;
import org.data2semantics.mustard.learners.liblinear.LibLINEARParameters;


public class GraphFeatureVectorKernelComputationTimeExperiment<D extends GraphData> extends KernelExperiment<FeatureVectorKernel<D>> {
	private D data;
	private Result compR;

	public GraphFeatureVectorKernelComputationTimeExperiment(List<? extends FeatureVectorKernel<D>> kernels, D data, long[] seeds) {
		super(kernels, seeds);
		this.data = data;
		compR = new Result();
		results.add(compR);
	}

	@Override
	public void run() {
		long tic, toc;
		long compTime = 0;
	
		System.out.println("Computing FVs...");	
		for (FeatureVectorKernel<D> kernel : kernels) {
			tic = System.currentTimeMillis();
			kernel.computeFeatureVectors(data);
			toc = System.currentTimeMillis();
			
			if (kernel instanceof ComputationTimeTracker) {
				compTime += ((ComputationTimeTracker) kernel).getComputationTime();
			} else {
				compTime += toc - tic;
			}
		}

	double[] comp = {compTime};	
	compR.setScores(comp);	
}

}