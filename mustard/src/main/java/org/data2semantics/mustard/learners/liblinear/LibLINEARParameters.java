package org.data2semantics.mustard.learners.liblinear;

import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.MeanSquaredError;

import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.SolverType;

public class LibLINEARParameters {
	public static final int SVC_DUAL = 1;
	public static final int SVC_PRIMAL = 2;
	public static final int SVR_DUAL = 3;
	public static final int SVR_PRIMAL = 4;
	public static final int LR_DUAL = 5;
	public static final int LR_PRIMAL = 6;	

	private int[] weightLabels;
	private double[] weights;
	private int algorithm;
	private Parameter params;	
	private double[] cs;
	private double[] ps;
	private boolean verbose;
	private double bias;
	
	private boolean doCrossValidation;
	private int numFolds;
	private float splitFraction;
	private boolean doWeightLabels;
	
	private SolverType solver;
	private double eps;
	
	private EvaluationFunction evalFunction;
	

	public LibLINEARParameters(int algorithm, double[] cs) {
		this(algorithm);
		this.cs = cs;
	}

	public LibLINEARParameters(int algorithm) {
		this.algorithm = algorithm;
		
		switch (algorithm) {
		case SVC_DUAL: 	solver = SolverType.L2R_L2LOSS_SVC_DUAL;
		evalFunction = new Accuracy();
		break;
		case SVC_PRIMAL: solver = SolverType.L2R_L2LOSS_SVC;
		evalFunction = new Accuracy();
		break;
		case SVR_DUAL: solver = SolverType.L2R_L2LOSS_SVR_DUAL;
		evalFunction = new MeanSquaredError();
		break;
		case SVR_PRIMAL: solver = SolverType.L2R_L2LOSS_SVR;
		evalFunction = new MeanSquaredError();
		break;
		case LR_DUAL: solver = SolverType.L2R_LR_DUAL;
		evalFunction = new Accuracy();
		break;
		case LR_PRIMAL: solver = SolverType.L2R_LR;
		evalFunction = new Accuracy();
		break;
		default: solver = SolverType.L2R_L2LOSS_SVC_DUAL;
		evalFunction = new Accuracy();
		break;
		}

		verbose = false;
		bias = -1;
		doCrossValidation = true;
		doWeightLabels = false;
		numFolds = 5;
		splitFraction = (float) 0.7;
		ps = new double[1];
		ps[0] = 0.1;
		cs = new double[1];
		cs[0] = 1;
		eps = 0.1;
		
		params = new Parameter(solver, cs[0], eps);
	}

	public int getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(int algorithm) {
		this.algorithm = algorithm;
	}

	public void setCs(double[] itParams) {
		this.cs = itParams;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setBias(double bias) {
		this.bias = bias;
	}

	public double getBias() {
		return bias;
	}

	public Parameter getParams() {
		return params;
	}
	
	public Parameter getParamsCopy() {
		Parameter p2 = new Parameter(params.getSolverType(), params.getC(), params.getEps());
		if (weights != null) {
			p2.setWeights(params.getWeights(), params.getWeightLabels());
		}		
		p2.setEps(params.getEps());
		
		return p2;
	}

	public double[] getCs() {
		return cs;
	}
	
	public double[] getPs() {
		return ps;
	}

	public void setPs(double[] ps) {
		this.ps = ps;
	}

	public void setEps(double eps) {
		this.eps = eps;
		params.setEps(eps);
	}

	public double getEps() {
		return eps;
	}

	public boolean isVerbose() {
		return verbose;
	}
	
	public int getNumFolds() {
		return numFolds;
	}

	public float getSplitFraction() {
		return splitFraction;
	}

	public void setNumFolds(int numFolds) {
		this.numFolds = numFolds;
	}

	public void setSplitFraction(float splitFraction) {
		this.splitFraction = splitFraction;
	}
	

	public boolean isDoCrossValidation() {
		return doCrossValidation;
	}

	public void setDoCrossValidation(boolean doCrossValidation) {
		this.doCrossValidation = doCrossValidation;
	}

	public void setEvalFunction(EvaluationFunction evalFunc) {
		this.evalFunction = evalFunc;
	}

	public EvaluationFunction getEvalFunction() {
		return evalFunction;
	}
	
	public void setWeightLabels(int[] labels) {
		weightLabels = labels;
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
		params.setWeights(weights, weightLabels);
		doWeightLabels = true;
	}
	
	public int[] getWeightLabels() {
		return weightLabels;
	}

	public double[] getWeights() {
		return weights;
	}

	public boolean isDoWeightLabels() {
		return doWeightLabels;
	}

	/*
	public void setDoWeightLabels(boolean doWeightLabels) {
		this.doWeightLabels = doWeightLabels;
	}
	*/

	public SolverType getSolver() {
		return solver;
	}	
}
