package org.data2semantics.mustard.learners.evaluation;

import java.io.Serializable;

import org.data2semantics.mustard.learners.Prediction;

public class SingleClassAccuracy implements EvaluationFunction, Serializable {
	private static final long serialVersionUID = -7472116182091479284L;
	private double clazz;
	
	public SingleClassAccuracy(double clazz) {
		this.clazz = clazz;
	}
	
	public double computeScore(double[] target, Prediction[] prediction) {
		double correct = 0;	
		for (int i = 0; i < target.length; i++) {
			if (target[i] == clazz && prediction[i].getLabel() == clazz) { // If this is the class we are interested in, then we are correct
				correct += 1;
			}
			if (target[i] != clazz && prediction[i].getLabel() != clazz) { // Both not class, also correct
				correct += 1;
			}
		}
		return correct / (target.length);	
	}

	public boolean isBetter(double scoreA, double scoreB) {
		if (scoreA > scoreB) {
			return true;
		}
		return false;
	}
	
	public String getLabel() {
		return "Accuracy_" + clazz;
	}
	
	public boolean isHigherIsBetter() {
		return true;
	}
	
	
}
