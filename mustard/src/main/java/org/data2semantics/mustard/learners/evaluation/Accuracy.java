package org.data2semantics.mustard.learners.evaluation;

import java.io.Serializable;

import org.data2semantics.mustard.learners.Prediction;

public class Accuracy implements EvaluationFunction, Serializable {
	
	public double computeScore(double[] target, Prediction[] prediction) {
		double correct = 0;	
		for (int i = 0; i < target.length; i++) {
			if (target[i] == prediction[i].getLabel()) {
				correct += 1;
			}
		}
		return correct / ((double) target.length);	
	}

	public boolean isBetter(double scoreA, double scoreB) {
		if (scoreA > scoreB) {
			return true;
		}
		return false;
	}
	
	public String getLabel() {
		return "Accuracy";
	}
	
	public boolean isHigherIsBetter() {
		return true;
	}
	
	
}
