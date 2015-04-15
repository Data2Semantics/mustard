package org.data2semantics.mustard.learners.utils;

import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.learners.Prediction;

public class CVUtils {
	public static int splitPoint(int length, double splitFrac) {
		return (int) Math.round((length) * splitFrac);
	}
	
	public static int foldStart(int length, int numberOfFolds, int fold) {
		return Math.round((length / ((float) numberOfFolds)) * ((float) fold - 1));
	}
	
	public static int foldEnd(int length, int numberOfFolds, int fold) {
		return Math.round((length / ((float) numberOfFolds)) * (fold));
	}

	public static double[][] createTrainFold(double[][] kernel, int numberOfFolds, int fold) {
		int foldStart = foldStart(kernel.length, numberOfFolds, fold);
		int foldEnd   = foldEnd(kernel.length, numberOfFolds, fold);
		int foldLength = (foldEnd-foldStart);

		double[][] trainKernel = new double[kernel.length - foldLength][kernel.length - foldLength];

		for (int i = 0; i < foldStart; i++) {
			for (int j = 0; j < foldStart; j++) {
				trainKernel[i][j] = kernel[i][j];
			}
		}

		for (int i = foldEnd; i < kernel.length; i++) {
			for (int j = 0; j < foldStart; j++) {
				trainKernel[i - foldLength][j] = kernel[i][j];
			}
		}

		for (int i = 0; i < foldStart; i++) {
			for (int j = foldEnd; j < kernel.length; j++) {
				trainKernel[i][j - foldLength] = kernel[i][j];
			}
		}

		for (int i = foldEnd; i < kernel.length; i++) {
			for (int j = foldEnd; j < kernel.length; j++) {
				trainKernel[i - foldLength][j - foldLength] = kernel[i][j];
			}
		}

		return trainKernel;
	}

	public static double[][] createTestFold(double[][] kernel, int numberOfFolds, int fold) {
		int foldStart = foldStart(kernel.length, numberOfFolds, fold);
		int foldEnd   = foldEnd(kernel.length, numberOfFolds, fold);
		int foldLength = (foldEnd-foldStart);

		double[][] testKernel = new double[foldEnd - foldStart][kernel.length - foldLength];

		for (int i = 0; i < foldEnd - foldStart; i++) {
			for (int j = 0; j < foldStart; j++) {
				testKernel[i][j] = kernel[i + foldStart][j];
			}
			for (int j = foldEnd; j < kernel.length; j++) {
				testKernel[i][j - foldLength] = kernel[i + foldStart][j];
			}
		}

		return testKernel;
	}

	public static double[] createTargetTrainFold(double[] target, int numberOfFolds, int fold) {
		int foldStart = foldStart(target.length, numberOfFolds, fold);
		int foldEnd   = foldEnd(target.length, numberOfFolds, fold);
		int foldLength = (foldEnd-foldStart);

		double[] trainTargets = new double[target.length - foldLength];

		for (int i = 0; i < foldStart; i++) {
			trainTargets[i] = target[i];
		}	
		for (int i = foldEnd; i < target.length; i++) {
			trainTargets[i - foldLength] = target[i];
		}			
		return trainTargets;
	}

	public static SparseVector[] createFeatureVectorsTrainFold(SparseVector[] featureVectors, int numberOfFolds, int fold) {
		int foldStart = foldStart(featureVectors.length, numberOfFolds, fold);
		int foldEnd   = foldEnd(featureVectors.length, numberOfFolds, fold);
		int foldLength = (foldEnd-foldStart);

		SparseVector[] trainFV = new SparseVector[featureVectors.length - foldLength];

		for (int i = 0; i < foldStart; i++) {
			trainFV[i] = featureVectors[i];
		}	
		for (int i = foldEnd; i < featureVectors.length; i++) {
			trainFV[i - foldLength] = featureVectors[i];
		}			
		return trainFV;
	}

	public static SparseVector[] createFeatureVectorsTestFold(SparseVector[] featureVectors, int numberOfFolds, int fold) {
		int foldStart = foldStart(featureVectors.length, numberOfFolds, fold);
		int foldEnd   = foldEnd(featureVectors.length, numberOfFolds, fold);
		int foldLength = (foldEnd-foldStart);

		SparseVector[] testFV = new SparseVector[foldLength];

		for (int i = foldStart; i < foldEnd; i++) {
			testFV[i - foldStart] = featureVectors[i];
		}			
		return testFV;
	}


	public static Prediction[] addFold2Prediction(Prediction[] foldPred, Prediction[] pred, int numberOfFolds, int fold) {
		int foldStart = foldStart(pred.length, numberOfFolds, fold);
		int foldEnd   = foldEnd(pred.length, numberOfFolds, fold);

		for (int i = foldStart; i < foldEnd; i++) {
			pred[i] = foldPred[i - foldStart];
			pred[i].setFold(fold);
		}
		return pred;
	}
	
}

