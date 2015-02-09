package org.data2semantics.mustard.learners.evaluation;

import org.data2semantics.mustard.learners.Prediction;

public interface MarginEvaluationFunction extends EvaluationFunction {
	
	/**
	 * @return the class that we want the (distance to the) margin for
	 */
	public double getMarginClass();
}
