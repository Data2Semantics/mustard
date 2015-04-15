package org.data2semantics.mustard.learners.evaluation;

import org.data2semantics.mustard.learners.Prediction;

public interface EvaluationFunction {
	public double computeScore(double[] target, Prediction[] prediction);
	public boolean isBetter(double scoreA, double scoreB);
	public String getLabel();
	public boolean isHigherIsBetter();
}
