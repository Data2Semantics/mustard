package org.data2semantics.mustard.learners.evaluation.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AUCUtils {

	public static double computeRocAuc(double[] decVals, boolean[] labels) {
		List<ValueLabelPair> pairs = new ArrayList<ValueLabelPair>();

		for (int i = 0; i < labels.length; i++) {
			pairs.add(new ValueLabelPair(decVals[i], labels[i]));
		}

		return computeRocAuc(pairs);		
	}

	/**
	 * compute area under the ROC curve. Algorithm from "Data mining and analysis" Zaki & Meira, 2014.
	 * 
	 */
	public static double computeRocAuc(List<ValueLabelPair> pairs) {
		double posClassSize = 0, negClassSize = 0;
		for (int i = 0; i < pairs.size(); i++) {
			posClassSize += (pairs.get(i).isLabel()) ? 1 : 0;
			negClassSize += (pairs.get(i).isLabel()) ? 0 : 1;
		}

		if (posClassSize == 0 || negClassSize == 0) { // cannot compute any AUC in this case, dunno if 0.0 is actually correct, maybe 1/2?
			return 0.0;
		}

		Collections.sort(pairs);
		Collections.reverse(pairs);

		double rho = Double.POSITIVE_INFINITY;
		double fp = 0, tp = 0, prevFp = 0, prevTp = 0;
		double auc = 0;

		for (ValueLabelPair pair : pairs) {
			if (pair.getValue() < rho) {
				auc += trapezoidArea(prevFp/negClassSize, prevTp/posClassSize, fp/negClassSize, tp/posClassSize);
				rho = pair.getValue();
				prevFp = fp;
				prevTp = tp;
			}

			if (pair.isLabel()) {
				tp += 1;
			} else {
				fp += 1;
			}
		}
		auc += trapezoidArea(prevFp/negClassSize, prevTp/posClassSize, fp/negClassSize, tp/posClassSize);

		return auc;
	}


	public static double computePRAuc(double[] decVals, boolean[] labels) {
		List<ValueLabelPair> pairs = new ArrayList<ValueLabelPair>();

		for (int i = 0; i < labels.length; i++) {
			pairs.add(new ValueLabelPair(decVals[i], labels[i]));
		}

		return computePRAuc(pairs);		
	}


	/**
	 * compute area under the Precision-Recall curve. Algorithm from "Data mining and analysis" Zaki & Meira, 2014.
	 * Adapted according to IMCL2006 paper by Davis & Goadrich.
	 * 
	 */
	public static double computePRAuc(List<ValueLabelPair> pairs) {
		double posClassSize = 0;
		for (int i = 0; i < pairs.size(); i++) {
			posClassSize += (pairs.get(i).isLabel()) ? 1 : 0;
		}

		if (posClassSize == 0) { // cannot compute any AUC in this case, dunno if 0.0 is actually correct, maybe 1/2?
			return 0.0;
		}

		Collections.sort(pairs);
		Collections.reverse(pairs);

		double rho = Double.POSITIVE_INFINITY;
		double fp = 0, tp = 0, prevFp = 0, prevTp = 0;
		double auc = 0;

		for (ValueLabelPair pair : pairs) {
			if (pair.getValue() < rho) {

				double skew = (fp-prevFp) / (tp-prevTp);

				// We have to have some tp's else there is no recall and hence no area under the curve
				for (int i = 0; i < tp-prevTp; i++) {
					if (prevTp+prevFp+i+(i*skew) == 0) { // if we previously had no precision defined (no tp and no fp), then we fix this to 1
						auc += trapezoidArea((prevTp+i)/posClassSize, 1, (prevTp+i+1)/posClassSize, (prevTp+i+1)/(prevTp+i+1+prevFp+((i+1)*skew)));
					} else {
						auc += trapezoidArea((prevTp+i)/posClassSize, (prevTp+i)/(prevTp+i+prevFp+(i*skew)), (prevTp+i+1)/posClassSize, (prevTp+i+1)/(prevTp+i+1+prevFp+((i+1)*skew)));
					}
				}


				rho = pair.getValue();
				prevFp = fp;
				prevTp = tp;
			}

			if (pair.isLabel()) {
				tp += 1;
			} else {
				fp += 1;
			}
		}
		double skew = (fp-prevFp) / (tp-prevTp);
		for (int i = 0; i < tp-prevTp; i++) {
			if (prevTp+prevFp+i+(i*skew) == 0) { // if we previously had no precision defined (no tp and no fp), then we fix this to 1
				auc += trapezoidArea((prevTp+i)/posClassSize, 1, (prevTp+i+1)/posClassSize, (prevTp+i+1)/(prevTp+i+1+prevFp+((i+1)*skew)));
			} else {
				auc += trapezoidArea((prevTp+i)/posClassSize, (prevTp+i)/(prevTp+i+prevFp+(i*skew)), (prevTp+i+1)/posClassSize, (prevTp+i+1)/(prevTp+i+1+prevFp+((i+1)*skew)));
			}
		}

		return auc;
	}


	private static double trapezoidArea(double x1, double y1, double x2, double y2) {
		return Math.abs(x2 - x1) * ((y2 + y1)/2);
	}
}
