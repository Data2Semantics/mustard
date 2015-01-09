package org.data2semantics.mustard.experiments.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.mustard.kernels.FeatureInspector;
import org.data2semantics.mustard.kernels.Kernel;
import org.data2semantics.mustard.kernels.data.GraphData;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.learners.Prediction;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.liblinear.LibLINEAR;
import org.data2semantics.mustard.learners.liblinear.LibLINEARModel;
import org.data2semantics.mustard.learners.liblinear.LibLINEARParameters;


public class SimpleGraphFeatureVectorKernelExperiment<D extends GraphData> extends KernelExperiment<FeatureVectorKernel<D>> {
	private D data;
	private List<Double> labels;
	private LibLINEARParameters svmParms;
	private Result compR;

	public SimpleGraphFeatureVectorKernelExperiment(List<? extends FeatureVectorKernel<D>> kernels, D data,
			List<Double> labels, LibLINEARParameters svmParms, long[] seeds,
			List<EvaluationFunction> evalFunctions) {
		super(kernels, seeds);
		this.data = data;
		this.labels = labels;
		this.svmParms = svmParms;

		for (EvaluationFunction evalFunc : evalFunctions) {
			Result res = new Result(evalFunc);
			double[] resA = new double[seeds.length]; // add a new empty array with the length of the amount of seeds (i.e. the number of repetitions of the experiment).
			res.setScores(resA);
			results.add(res);
		}

		compR = new Result();
		results.add(compR);
	}

	@Override
	public void run() {
		long tic, toc;

		List<Double> tempLabels = new ArrayList<Double>();
		tempLabels.addAll(labels);

		Map<Kernel, SparseVector[]> fvs = new HashMap<Kernel, SparseVector[]>();

		tic = System.currentTimeMillis();	
		System.out.println("Computing FVs...");	
		for (FeatureVectorKernel<D> kernel : kernels) {
			SparseVector[] fv = kernel.computeFeatureVectors(data);
			fvs.put(kernel, fv);
		}
		toc = System.currentTimeMillis();

		compR.setLabel("kernel comp time");

		System.out.println("Performing CV...");
		for (int j = 0; j < seeds.length; j++) {
			for (Kernel k : fvs.keySet()) {
				List<SparseVector> tempFV = Arrays.asList(fvs.get(k));
				Collections.shuffle(tempFV, new Random(seeds[j]));
				fvs.put(k, tempFV.toArray(new SparseVector[0]));
			}
			Collections.shuffle(tempLabels, new Random(seeds[j]));		

			double[] target = new double[tempLabels.size()];
			for (int i = 0; i < target.length; i++) {
				target[i] = tempLabels.get(i);
			}

			Prediction[] pred = LibLINEAR.crossValidateWithMultipleFeatureVectors(fvs, target, svmParms, svmParms.getNumFolds());

			for (Result res : results) {
				if (res.getEval() != null) {
					res.getScores()[j] = res.getEval().computeScore(target, pred);	
				}
			}	

			LibLINEARModel model = LibLINEAR.trainLinearModelWithMultipleFeatureVectors(fvs, target, svmParms);		
			LibLINEARModel.WeightIndexPair[][] fws = model.getFeatureWeights();

			// Sort them
			for (int i = 0; i < fws.length; i++) {
				if (model.getKernelSetting() instanceof FeatureInspector) {
					FeatureInspector fi = (FeatureInspector) model.getKernelSetting();
					Arrays.sort(fws[i]);
					List<Integer> indices = new ArrayList<Integer>();
					for (int k = 0; k < 10; k++) {
						indices.add(fws[i][k].getIndex());
					}
					
					System.out.println("Class " + i + ": " + fi.getFeatureDescriptions(indices));
				}
			}



		}

		double[] comp = {toc - tic};
		compR.setScores(comp);		
	}
}