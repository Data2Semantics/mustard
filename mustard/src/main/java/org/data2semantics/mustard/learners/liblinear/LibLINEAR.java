package org.data2semantics.mustard.learners.liblinear;


import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.kernels.Kernel;
import org.data2semantics.mustard.learners.Prediction;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.learners.libsvm.LibSVM;
import org.data2semantics.mustard.learners.libsvm.ParameterIterator;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;

/**
 * Static methods to ease the access to the Java LibLINEAR package, similar to the {@link LibSVM} class.
 * Not all solvers are accessible, only the most common L2 types, i.e. primal/dual, of SVC, SVR and LR.
 * 
 * @author Gerben
 *
 */
public class LibLINEAR {

	public static LibLINEARModel trainLinearModelWithMultipleFeatureVectors(Map<Kernel, SparseVector[]> featureVectors, double[] target, LibLINEARParameters params) {
		Map<Kernel, Problem> probs = new HashMap<Kernel, Problem>();
		for (Kernel k : featureVectors.keySet()) {
			probs.put(k, createLinearProblem(featureVectors.get(k), target, params.getBias()));
		}
		return trainLinearModel(probs, params);
	}
	
	public static LibLINEARModel trainLinearModel(SparseVector[] featureVectors, double[] target, LibLINEARParameters params) {
		Problem prob = createLinearProblem(featureVectors, target, params.getBias());
		Map<Kernel, Problem> dummy = new HashMap<Kernel,Problem>();
		dummy.put(null, prob);
		
		return trainLinearModel(dummy, params);
	}

	
	private static LibLINEARModel trainLinearModel(Problem prob, LibLINEARParameters params) {
		Map<Kernel, Problem> dummy = new HashMap<Kernel,Problem>();
		dummy.put(null, prob);		
		return trainLinearModel(dummy, params);
	}
	
	private static LibLINEARModel trainLinearModel(Map<Kernel, Problem> probs, LibLINEARParameters params) {
		if (!params.isVerbose()) {
			Linear.disableDebugOutput();
		}

		Prediction[] prediction;

		double[] target = null;
		Problem trainProb = null;
		Problem testProb = null;

		Parameter linearParams = params.getParamsCopy();
		
		double score = 0, bestScore = 0, bestC = 0, bestP = 0;
		Kernel bestSetting = null;

		for (Kernel setting : probs.keySet()) {
			if (bestSetting == null) { // Initialize best setting
				bestSetting = setting;
			}

			if (params.isDoCrossValidation()) {
				target = probs.get(setting).y;
			} else {
				trainProb = createProblemTrainSplit(probs.get(setting), params.getSplitFraction());
				testProb  = createProblemTestSplit(probs.get(setting), params.getSplitFraction());
				target = testProb.y;
			}

			for (double p : params.getPs()) {
				linearParams.setP(p);
				
				ParameterIterator pi = new ParameterIterator(params.getCs());
				
				while (pi.hasNext()) {
					double c = pi.nextParm();
					linearParams.setC(c);

					if (params.isDoCrossValidation()) {
						prediction = crossValidate(probs.get(setting), linearParams, params.getNumFolds());

					} else {
						prediction = testLinearModel(new LibLINEARModel(Linear.train(trainProb, linearParams)), testProb.x);
					}
					score = params.getEvalFunction().computeScore(target, prediction);

					pi.updateParm(bestC == 0 || params.getEvalFunction().isBetter(score, bestScore));
					
					if (bestC == 0 || params.getEvalFunction().isBetter(score, bestScore)) {
						bestC = c;
						bestP = p;
						bestScore = score;
						bestSetting = setting;
					}	
				}
			}
		}

		linearParams.setC(bestC);	
		linearParams.setP(bestP);
		LibLINEARModel model = new LibLINEARModel(Linear.train(probs.get(bestSetting), linearParams));
		model.setKernelSetting(bestSetting);
		
		String label = "default kernel";
		if (bestSetting != null) {
			label = bestSetting.getLabel();
		} 		
		
		System.out.println("Trained Linear SVM for " + label + ", with C: " + bestC + " and P: " + bestP);

		double avg = 0;
		for (Feature[] v : probs.get(bestSetting).x) {
			avg += v.length;
		}
		avg /= probs.get(bestSetting).x.length;

		System.out.println("#instances:" + probs.get(bestSetting).l + ", #features: " + probs.get(bestSetting).n + ", #avg-non-zero: " + avg);
		
		return model;
	}

	public static Prediction[] testLinearModelWithMultiFeatureVectors(LibLINEARModel model, Map<Kernel, SparseVector[]> testVectors) {
		Map<Kernel, Feature[][]> problems = new HashMap<Kernel, Feature[][]>();
		for (Kernel k : testVectors.keySet()) {
			problems.put(k, createTestProblem(testVectors.get(k), model.getModel().getNrFeature(), model.getModel().getBias()));
		}
		return testLinearModel(model, problems);
	}
	

	public static Prediction[] testLinearModel(LibLINEARModel model, SparseVector[] testVectors) {
		Map<Kernel, Feature[][]> dummy = new HashMap<Kernel, Feature[][]>();
		dummy.put(null, createTestProblem(testVectors, model.getModel().getNrFeature(), model.getModel().getBias()));
		return testLinearModel(model, dummy);
	}


	private static Prediction[] testLinearModel(LibLINEARModel model, Map<Kernel, Feature[][]> problems) {
		Feature[][] problem = problems.get(model.getKernelSetting());		
		return testLinearModel(model, problem);
	}

	private static Prediction[] testLinearModel(LibLINEARModel model, Feature[][] problem) {
		Prediction[] pred = new Prediction[problem.length];		
		for (int i = 0; i < problem.length; i++) {
			double[] decVal = new double[(model.getModel().getNrClass() <= 2) ? 1 : model.getModel().getNrClass()];
			pred[i] = new Prediction(Linear.predictValues(model.getModel(), problem[i], decVal), i);
			pred[i].setDecisionValue(decVal);
		}
		return pred;
	}
	
	public static Prediction[] crossValidateWithMultipleFeatureVectors(Map<Kernel,SparseVector[]> featureVectors, double[] target, LibLINEARParameters params, int numberOfFolds) {
		Prediction[] pred = new Prediction[target.length];
	
		for (int fold = 1; fold <= numberOfFolds; fold++) {
			Map<Kernel, Problem> trainPs = new HashMap<Kernel, Problem>();
			Map<Kernel, Feature[][]> testPs = new HashMap<Kernel, Feature[][]>();
			for (Kernel k : featureVectors.keySet()) {
				Problem p = createLinearProblem(featureVectors.get(k), target, params.getBias());
				trainPs.put(k, createProblemTrainFold(p, numberOfFolds, fold));
				testPs.put(k, createProblemTestFold(p, numberOfFolds, fold));
			}
				pred = addFold2Prediction(testLinearModel(trainLinearModel(trainPs, params), testPs), pred, numberOfFolds, fold);
		}		
		return pred;
	}
	
	
	public static Prediction[] crossValidate(SparseVector[] featureVectors, double[] target, LibLINEARParameters params, int numberOfFolds) {
		Prediction[] pred = new Prediction[target.length];
		Problem trainP;
		Feature[][] testP;
		Problem prob = createLinearProblem(featureVectors, target, params.getBias());

		for (int fold = 1; fold <= numberOfFolds; fold++) {
			trainP = createProblemTrainFold(prob, numberOfFolds, fold);
			testP  = createProblemTestFold(prob, numberOfFolds, fold);
			pred = addFold2Prediction(testLinearModel(trainLinearModel(trainP, params), testP), pred, numberOfFolds, fold);
		}		
		return pred;
	}

	private static Prediction[] crossValidate(Problem prob, Parameter linearParams, int folds) {
		double[] prediction = new double[prob.l];
		Linear.crossValidation(prob, linearParams, folds, prediction);
		Prediction[] pred2 = new Prediction[prob.l];

		for (int i = 0; i < pred2.length; i++) {
			pred2[i] = new Prediction(prediction[i], i);
		}
		return pred2;
	}
	
	
	public static Prediction[] trainTestSplit(Map<Kernel, SparseVector[]> featureVectors, double[] target, LibLINEARParameters params, float splitFraction) {
		Map<Kernel, Problem> trainPs    = new HashMap<Kernel, Problem>();
		Map<Kernel, Feature[][]> testPs = new HashMap<Kernel, Feature[][]>();
		
		for (Kernel k : featureVectors.keySet()) {
			Problem p = createLinearProblem(featureVectors.get(k), target, params.getBias());
			trainPs.put(k, createProblemTrainSplit(p, splitFraction));	
			testPs.put(k, createProblemTestSplit(p, splitFraction).x);
		}

		return testLinearModel(trainLinearModel(trainPs, params), testPs);
	}

	public static Prediction[] trainTestSplit(SparseVector[] featureVectors, double[] target, LibLINEARParameters params, float splitFraction) {
		Problem total  = createLinearProblem(featureVectors, target, params.getBias());
		Problem trainP = createProblemTrainSplit(total, splitFraction);		
		Feature[][] testP  = createProblemTestSplit(total, splitFraction).x;

		return testLinearModel(trainLinearModel(trainP, params), testP);
	}

	public static double[] splitTestTarget(double[] target, double splitFraction) {
		int foldStart = Math.round((float) target.length * (float) splitFraction); 
		int foldEnd   = target.length;

		return Arrays.copyOfRange(target, foldStart, foldEnd);
	}


	public static void featureVectors2File(SparseVector[] featureVectors, double[] target, String filename) {
		try {
			FileWriter fo = new FileWriter(filename);
			StringBuffer line;

			for (int i = 0; i < featureVectors.length; i++) {
				line = new StringBuffer();
				line.append(target[i]);
				line.append(" ");

				for (int index : featureVectors[i].getIndices()) {
					line.append(index);
					line.append(":");
					line.append(featureVectors[i].getValue(index));
					line.append(" ");
				}
				line.append("\n");
				fo.write(line.toString());
			}
			fo.close();

		} catch (Exception e) {
			e.getStackTrace();
		}
	}



	private static Feature[][] createTestProblem(SparseVector[] featureVectors, int numberOfFeatures, double bias) {
		Feature[][] nodes = new FeatureNode[featureVectors.length][];

		for (int i = 0; i < featureVectors.length; i++) {
			Set<Integer> indices = featureVectors[i].getIndices();
			nodes[i] = new FeatureNode[(bias >= 0) ? indices.size() + 1 : indices.size()];

			int j = 0;
			for (int index : indices) {
				nodes[i][j] = new FeatureNode(index, featureVectors[i].getValue(index));
				j++;
			}
			if (bias >= 0) {
				nodes[i][j] = new FeatureNode(numberOfFeatures, bias);
			}
		}	
		return nodes;	
	}


	private static Problem createLinearProblem(SparseVector[] featureVectors, double[] target, double bias) {
		Problem prob = new Problem();
		prob.y = target;
		prob.x = new FeatureNode[featureVectors.length][];	
		prob.l = featureVectors.length;
		
		int maxIndex = 0;
		for (int i = 0; i < featureVectors.length; i++) {
			Set<Integer> indices = featureVectors[i].getIndices();
			prob.x[i] = new FeatureNode[(bias >= 0) ? indices.size() + 1 : indices.size()];
			int j = 0;
			for (int index : indices) {
				prob.x[i][j] = new FeatureNode(index + 1, featureVectors[i].getValue(index));	// Sparse Vectors start at index 0, LibLINEAR needs 1	
				maxIndex = Math.max(maxIndex, index + 1);
				j++;
			}
		}

		if (bias >= 0) {
			maxIndex++;
			for (int i = 0; i < featureVectors.length; i++) {
				prob.x[i][prob.x[i].length - 1] = new FeatureNode(maxIndex, bias);
			}
		}

		prob.n    = maxIndex;
		prob.bias = (bias >= 0) ? 1 : -1;

		return prob;		
	}

	private static Problem createProblemTrainSplit(Problem problem, float splitFrac) {
		int foldStart = 0; 
		int foldEnd   = Math.round(((float) problem.l) * splitFrac);

		Problem prob = new Problem();
		prob.y = Arrays.copyOfRange(problem.y, foldStart, foldEnd);
		prob.x = Arrays.copyOfRange(problem.x, foldStart, foldEnd);
		prob.l = foldEnd;
		prob.n = problem.n;

		return prob;
	}

	private static Problem createProblemTestSplit(Problem problem, float splitFrac) {
		int foldStart = Math.round(((float) problem.l) * splitFrac); 
		int foldEnd   = problem.l;

		Problem prob = new Problem();
		prob.y = Arrays.copyOfRange(problem.y, foldStart, foldEnd);
		prob.x = Arrays.copyOfRange(problem.x, foldStart, foldEnd);
		prob.l = prob.y.length;
		prob.n = problem.n;

		return prob;
	}


	private static Problem createProblemTrainFold(Problem problem, int numberOfFolds, int fold) {
		int foldStart = Math.round((problem.x.length / ((float) numberOfFolds)) * ((float) fold - 1));
		int foldEnd   = Math.round((problem.x.length / ((float) numberOfFolds)) * ((float) fold));
		int foldLength = (foldEnd-foldStart);

		Problem prob = new Problem();
		prob.y = new double[problem.x.length - foldLength];
		prob.x = new FeatureNode[problem.x.length - foldLength][];
		prob.l = problem.x.length - foldLength;
		prob.n = problem.n;


		for (int i = 0; i < foldStart; i++) {
			prob.x[i] = problem.x[i];
			prob.y[i] = problem.y[i];
		}	
		for (int i = foldEnd; i < problem.x.length; i++) {
			prob.x[i - foldLength] = problem.x[i];
			prob.y[i - foldLength] = problem.y[i];
		}			
		return prob;
	}


	static Feature[][] createProblemTestFold(Problem problem, int numberOfFolds, int fold) {
		int foldStart = Math.round((problem.x.length / ((float) numberOfFolds)) * ((float) fold - 1));
		int foldEnd   = Math.round((problem.x.length / ((float) numberOfFolds)) * ((float) fold));
		int foldLength = (foldEnd-foldStart);

		Feature[][] testP = new FeatureNode[foldLength][];

		for (int i = foldStart; i < foldEnd; i++) {
			testP[i - foldStart] = problem.x[i];
		}			
		return testP;
	}

	static Prediction[] addFold2Prediction(Prediction[] foldPred, Prediction[] pred, int numberOfFolds, int fold) {
		int foldStart = Math.round((pred.length / ((float) numberOfFolds)) * ((float) fold - 1));
		int foldEnd   = Math.round((pred.length / ((float) numberOfFolds)) * ((float) fold));

		for (int i = foldStart; i < foldEnd; i++) {
			pred[i] = foldPred[i - foldStart];
		}
		return pred;
	}

}
