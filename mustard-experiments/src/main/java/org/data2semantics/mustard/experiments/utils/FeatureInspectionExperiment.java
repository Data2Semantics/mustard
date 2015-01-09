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


public class FeatureInspectionExperiment<D extends GraphData, K extends FeatureVectorKernel<D> & FeatureInspector>  {
	private D data;
	private List<Double> labels;
	private LibLINEARParameters svmParms;
	private List<K> kernels;
	private int maxFeatures;

	public FeatureInspectionExperiment(List<K> kernels, D data,	List<Double> labels, LibLINEARParameters svmParms, int maxFeatures) {
		this.data = data;
		this.labels = labels;
		this.svmParms = svmParms;
		this.kernels = kernels;
		this.maxFeatures = maxFeatures;
	}

	public void run() {
		List<Double> tempLabels = new ArrayList<Double>();
		tempLabels.addAll(labels);

		Map<Kernel, SparseVector[]> fvs = new HashMap<Kernel, SparseVector[]>();

		System.out.println("Computing FVs...");	
		for (FeatureVectorKernel<D> kernel : kernels) {
			SparseVector[] fv = kernel.computeFeatureVectors(data);
			fvs.put(kernel, fv);
		}
		for (Kernel k : fvs.keySet()) {
			List<SparseVector> tempFV = Arrays.asList(fvs.get(k));
			Collections.shuffle(tempFV, new Random(11));
			fvs.put(k, tempFV.toArray(new SparseVector[0]));
		}
		Collections.shuffle(tempLabels, new Random(11));		

		double[] target = new double[tempLabels.size()];
		for (int i = 0; i < target.length; i++) {
			target[i] = tempLabels.get(i);
		}

		System.out.println("Computing Model...");	
		LibLINEARModel model = LibLINEAR.trainLinearModelWithMultipleFeatureVectors(fvs, target, svmParms);		
		LibLINEARModel.WeightIndexPair[][] fws = model.getFeatureWeights();

		// Sort them
		for (int i = 0; i < fws.length; i++) {
			FeatureInspector fi = (FeatureInspector) model.getKernelSetting();
			Arrays.sort(fws[i]);
			List<Integer> indices = new ArrayList<Integer>();

			System.out.println("Class " + i);
			System.out.print("Index: Weight - ");

			for (int k = 0; k < maxFeatures; k++) {
				if (fws[i][k].getWeight() > 0){
					indices.add(fws[i][k].getIndex());
					System.out.print(fws[i][k].getIndex() + ": " + fws[i][k].getWeight() + ", ");
				} else {
					break;
				}
			}
			System.out.println("");
			System.out.println("Labels        - " + fi.getFeatureDescriptions(indices));
		}	
	}
}