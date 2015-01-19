package org.data2semantics.mustard.learners.evaluation;

import java.util.HashMap;
import java.util.Map;

import org.data2semantics.mustard.learners.Prediction;

/**
 * macro Precision
 * 
 * P = TP / (TP + FP)
 * 
 * @author Gerben
 *
 */
public class Precision implements EvaluationFunction {

	public double computeScore(double[] target, Prediction[] prediction) {
		Map<Double, Double> counts = new HashMap<Double, Double>();

		for (int i = 0; i < target.length; i++) {
			if (!counts.containsKey(target[i])) {
				counts.put(target[i], 1.0);
			} else {
				counts.put(target[i], counts.get(target[i]) + 1);
			}
		}

		double p = 0, temp1 = 0, temp2 = 0;
		
		for (double label : counts.keySet()) {
			for (int i = 0; i < prediction.length; i++) {
				if ((prediction[i].getLabel() == label && target[i] == label)) { // TP
					temp1 += 1;
				}
				else if (prediction[i].getLabel() == label) { // FP  (because we have all the TP already)
					temp2 += 1;
				}
			}
			
			if (temp1 != 0.0 || temp2 != 0.0) {
				p += temp1 /(temp1 + temp2);
			}
			temp1 = 0;
			temp2 = 0;
		}	
		return p / (counts.size());
	}

	public boolean isBetter(double scoreA, double scoreB) {
		return (scoreA > scoreB) ? true : false;
	}

	public String getLabel() {
		return "Precision";
	}

	public boolean isHigherIsBetter() {
		return true;
	}
}
