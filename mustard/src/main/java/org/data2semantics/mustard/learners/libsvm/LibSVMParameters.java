package org.data2semantics.mustard.learners.libsvm;

import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.MeanSquaredError;

/**
 * Wrapper class around the LibSVM svm_parameter object.
 * We allow the choice of all the SVM algorithms implemented in libsvm.
 * However, we only use the precomputed kernel setting and do not support the probability estimates setting.
 * 
 * 
 * @author Gerben
 *
 */
public class LibSVMParameters {
	public static final int ONE_CLASS = svm_parameter.ONE_CLASS;
	public static final int C_SVC = svm_parameter.C_SVC;
	public static final int NU_SVC = svm_parameter.NU_SVC;
	public static final int EPSILON_SVR = svm_parameter.EPSILON_SVR;
	public static final int NU_SVR = svm_parameter.NU_SVR;	


	private svm_parameter params;
	private double[] itParams;
	private double[] ps;
	private boolean verbose;
	private int numFolds;

	private EvaluationFunction evalFunction;
	

	/**
	 * 
	 * @param algorithm, one of the 5 algorithms
	 * @param itParams, an array of doubles, which are the values to iterate over during training. For C-SVC and epsilon-SVR, these are possibilities for the C parameter, so ranges like 0.001, 0.01, 0.1, 1, 10, etc.
	 * make sense. For the other 3 algorithms this is the nu parameter, which should be between 0 and 1.
	 */
	public LibSVMParameters(int algorithm, double[] itParams) {
		this(algorithm);
		this.itParams = itParams;
	}

	/**
	 * Note that the itParams have to be set manually when using this constructor.
	 * 
	 * @param algorithm, one of the 5 algorithms
	 */
	public LibSVMParameters(int algorithm) {
		params = new svm_parameter();
		params.svm_type = algorithm;

		// Fixed parameters
		params.kernel_type = svm_parameter.PRECOMPUTED;
		params.eps = 0.0001;
		params.shrinking = 0;
		params.probability = 0;
		params.cache_size = 300;

		// Weights, SVR epsilon, verbosity, evalFunction can be changed afterwards via setters
		params.nr_weight = 0;
		params.p = 0.1;
		verbose = false;
		ps = new double[1];
		ps[0] = 0.1;

		switch (algorithm) {
		case EPSILON_SVR: evalFunction = new MeanSquaredError(); break;
		case NU_SVR: evalFunction = new MeanSquaredError(); break;
		default: evalFunction = new Accuracy();

		}

		numFolds = 10;
	}

	svm_parameter getParams() {
		return params;
	}
	
	/**
	 * Get a copy of the svm_parameter object
	 * 
	 * @return
	 */
	svm_parameter getParamsCopy() {
		svm_parameter p2 = new svm_parameter();
		
		p2.svm_type = params.svm_type;
		p2.kernel_type = params.kernel_type;
		p2.eps = params.eps;
		p2.shrinking = params.shrinking;
		p2.probability = params.probability;
		p2.cache_size = params.cache_size;
		p2.nr_weight = params.nr_weight;
		p2.weight = params.weight;
		p2.weight_label = params.weight_label;
		p2.p = params.p;
		
		return p2;
	}

	public int getAlgorithm() {
		return params.svm_type;
	}
	
	public void setAlgorithm(int algorithm) {
		this.params.svm_type = algorithm;
	}

	/**
	 * Set the values to try for C (in case of C-SVC and epsilon-SVR) and nu in case of the other 3 algorithms.
	 * 	 * 
	 * @param itParams, range of values to try.
	 */
	public void setItParams(double[] itParams) {
		this.itParams = itParams;
	}

	public double[] getItParams() {
		return itParams;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isVerbose() {
		return verbose;
	}
	
	public void setEps(double eps) {
		params.eps = eps;
	}
	
	public double[] getPs() {
		return ps;
	}

	public void setPs(double[] ps) {
		this.ps = ps;
	}

	/**
	 * Together with setWeights this controls the weights for the different classes, when we have a skewed distribution.
	 * 	 * 
	 * @param labels
	 */
	public void setWeightLabels(int[] labels) {
		params.weight_label = labels;
		params.nr_weight = labels.length;
	}

	public void setWeights(double[] weight) {
		params.weight = weight;
		params.nr_weight = weight.length;
	}

	public void setLinear() {
		params.kernel_type = svm_parameter.LINEAR;
	}

	public void setPrecomputedKernel() {
		params.kernel_type = svm_parameter.PRECOMPUTED;
	}

	public int getNumFolds() {
		return numFolds;
	}

	public void setNumFolds(int numFolds) {
		this.numFolds = numFolds;
	}

	public void setEvalFunction(EvaluationFunction evalFunc) {
		this.evalFunction = evalFunc;
	}

	public EvaluationFunction getEvalFunction() {
		return evalFunction;
	}
}
