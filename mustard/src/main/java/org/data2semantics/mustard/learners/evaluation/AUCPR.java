package org.data2semantics.mustard.learners.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.learners.Prediction;
import org.data2semantics.mustard.learners.evaluation.utils.AUCUtils;
import org.data2semantics.mustard.learners.evaluation.utils.ValueLabelPair;
import org.data2semantics.mustard.utils.Pair;

/**
 * Average area under the Precision Recall curve
 * 
 * @author Gerben
 *
 */
public class AUCPR implements EvaluationFunction {

	public double computeScore(double[] target, Prediction[] prediction) {
		double auc = 0;

		Map<Integer,Double> foldSizes = new HashMap<Integer,Double>();

		List<Double> classes = new ArrayList<Double>();
		for (int i = 0; i < target.length; i++) {
			if (!classes.contains(target[i])) { // faster with a set, but classes should be short
				classes.add(target[i]);
			}
			int fold = 1;
			if (prediction[i].getFold() > 0 && !prediction[i].isProbabilities()) { // if we output probabilities, then folds are comparable, if we have SVM decision values, then they might not be.
				fold = prediction[i].getFold();
			} 

			if (!foldSizes.containsKey(fold)) {
				foldSizes.put(fold, 0.0);
			}
			foldSizes.put(fold, foldSizes.get(fold)+1);
		}

		Collections.sort(classes);

		for (int fold : foldSizes.keySet()) {
			double aucTemp = 0;

			if (prediction[0].isPairWise()) {
				Map<Pair<Double,Double>, List<ValueLabelPair>> classifiers = new HashMap<Pair<Double,Double>, List<ValueLabelPair>>();

				for (int i = 0; i < classes.size(); i++) {
					for (int j = i+1; j < classes.size(); j++) {
						classifiers.put(new Pair<Double,Double>(classes.get(i), classes.get(j)), new ArrayList<ValueLabelPair>());
						classifiers.put(new Pair<Double,Double>(classes.get(j), classes.get(i)), new ArrayList<ValueLabelPair>());
					}
				}

				for (int i = 0; i < target.length; i++) {
					if (prediction[i].getFold() == fold) {
						int classIndex = getClassIndex(prediction[i].getClassLabels(), target[i]);
						Map<Pair<Double,Double>, ValueLabelPair> map = getRelevantDecisionValuesPairwise(target[i], classIndex, prediction[i].isProbabilities(), prediction[i].getClassLabels(), prediction[i].getDecisionValue());
						for (Pair<Double,Double> p : map.keySet()) { // add the new decision values to the relevant classifiers
							classifiers.get(p).add(map.get(p)); 
						}
					}
				}

				for (Pair<Double,Double> p : classifiers.keySet()) {
					aucTemp += AUCUtils.computePRAuc(classifiers.get(p));
				}
				aucTemp /= (double) classifiers.size();
				aucTemp *= foldSizes.get(fold) / (double) target.length;
				auc += aucTemp;


			} else { // not pairwise
				Map<Double, List<ValueLabelPair>> classifiers = new HashMap<Double, List<ValueLabelPair>>();
				for (Double label : classes) {
					classifiers.put(label, new ArrayList<ValueLabelPair>());
				}

				for (int i = 0; i < target.length; i++) {
					if (prediction[i].getFold() == fold) {
						Map<Double, ValueLabelPair> map = getRelevantDecisionValues(target[i], prediction[i].getClassLabels(), prediction[i].getDecisionValue());

						for (Double d : map.keySet()) {
							classifiers.get(d).add(map.get(d));
						}
					}
				}

				for (Double d : classifiers.keySet()) {
					aucTemp += AUCUtils.computePRAuc(classifiers.get(d));
				}

				aucTemp /= (double) classifiers.size();
				aucTemp *= foldSizes.get(fold) / (double) target.length;
				auc += aucTemp;
			}
		}

		return auc;
	}

	private Map<Double, ValueLabelPair> getRelevantDecisionValues(double label, int[] classLabels, double[] decVals) {
		Map<Double, ValueLabelPair> map = new HashMap<Double,ValueLabelPair>();

		for (int i = 0; i < classLabels.length; i++) {
			if (classLabels[i] == (int)label) {
				map.put(new Double(classLabels[i]), new ValueLabelPair(decVals[i], true));
			} else {
				map.put(new Double(classLabels[i]), new ValueLabelPair(decVals[i], false));
			}
		}
		return map;
	}


	private Map<Pair<Double,Double>, ValueLabelPair> getRelevantDecisionValuesPairwise(double label, int classIndex, boolean probabilities, int[] classLabels, double[] decVals) {
		Map<Pair<Double,Double>, ValueLabelPair> map = new HashMap<Pair<Double,Double>,ValueLabelPair>();

		int decValIndex = 0; // to keep track of the index in the decVals array
		for (int i = 0; i < classLabels.length; i++) {
			for (int j = i+1; j < classLabels.length; j++) {
				if (i == classIndex || j == classIndex) { // if we are dealing with the current class
					Pair<Double,Double> idPair;
					double decVal;

					// Since the PR curve is not symmetric like the ROC curve, we add both classifiers
					idPair = new Pair<Double,Double>(new Double(classLabels[i]),new Double(classLabels[j]));
					decVal = decVals[decValIndex];

					if (idPair.getFirst() == label) { // if the first one is the current class, then it is positive
						map.put(idPair, new ValueLabelPair(decVal, true));
					} else {
						map.put(idPair, new ValueLabelPair(decVal, false));
					}

					idPair = new Pair<Double,Double>(new Double(classLabels[j]),new Double(classLabels[i]));
					if (!probabilities) {
						decVal = -decVals[decValIndex];
					} else {
						decVal = 1 - decVals[decValIndex];
					}

					if (idPair.getFirst() == label) { // if the first one is the current class, then it is positive
						map.put(idPair, new ValueLabelPair(decVal, true));
					} else {
						map.put(idPair, new ValueLabelPair(decVal, false));
					}
				}			
				decValIndex++;
			}
		}
		return map;
	}

	private int getClassIndex(int[] classLabels, double label) {
		for (int i = 0; i < classLabels.length; i++) {
			if (classLabels[i] == (int) label) {
				return i;
			}
		}
		return -1;
	}

	public boolean isBetter(double scoreA, double scoreB) {
		return (scoreA > scoreB) ? true : false;
	}

	public String getLabel() {
		return "AUC-PR";
	}

	public boolean isHigherIsBetter() {
		return true;
	}
}
