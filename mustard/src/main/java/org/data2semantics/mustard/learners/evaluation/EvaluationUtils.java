package org.data2semantics.mustard.learners.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationUtils {
	
	public static <T> List<Double> createTarget(List<T> labels) {
		return createTarget(labels, new HashMap<T,Double>());
	}
 
	public static <T> List<Double> createTarget(List<T> labels, Map<T,Double> labelMap) {
		List<Double> target = new ArrayList<Double>();
		double t = 0;
		
		for (T label : labels) {
			if (!labelMap.containsKey(label)) {
				t += 1;
				labelMap.put(label, t);
			} 
			target.add((double) labelMap.get(label));	
		}	
		return target;	
	}
	
	public static double[] target2Doubles(List<Double> target) {
		double[] ret = new double[target.size()];
		for (int i = 0; i < target.size(); i++) {
			ret[i] = target.get(i);
		}
		return ret;
	}
	
	public static List<Double> doubles2target(double[] doubles) {
		List<Double> ret = new ArrayList<Double>();
		for (int i = 0; i < doubles.length; i++) {
			ret.add(doubles[i]);
		}
		return ret;
	}
	
	public static int[] target2Integers(List<Integer> target) {
		int[] ret = new int[target.size()];
		for (int i = 0; i < target.size(); i++) {
			ret[i] = target.get(i);
		}
		return ret;
	}
	
	public static List<Integer> ints2target(int[] ints) {
		List<Integer> ret = new ArrayList<Integer>();
		for (int i = 0; i < ints.length; i++) {
			ret.add(ints[i]);
		}
		return ret;
	}
	
	
	public static <T> Map<Double, T> reverseLabelMap(Map<T,Double> labelMap) {
		Map<Double,T> revMap = new HashMap<Double,T>(); 
	
		for (T k : labelMap.keySet()) {
			revMap.put(labelMap.get(k), k);
		}
		return revMap;
	}
	
	public static Map<Double, Double> computeClassCounts(List<Double> target) {
		Map<Double, Double> counts = new HashMap<Double, Double>();

		for (double t : target) {
			if (!counts.containsKey(t)) {
				counts.put(t, 1.0);
			} else {
				counts.put(t, counts.get(t) + 1);
			}
		}
		return counts;
	}
	
	public static int[] computeWeightLabels(List<Double> target) {
		Map<Double, Double> counts = EvaluationUtils.computeClassCounts(target);
		int[] wLabels = new int[counts.size()];
		
		int index = 0;
		for (double label : counts.keySet()) {
			wLabels[index++] = (int) label;
		}
		return wLabels;
	}
	
	public static double[] computeWeights(List<Double> target) {
		Map<Double, Double> counts = EvaluationUtils.computeClassCounts(target);
		double[] weights = new double[counts.size()];

		int index = 0;
		for (double label : counts.keySet()) {
			weights[index++] = 1 / counts.get(label);
		}
		return weights;
	}
		
	
	public static <O1,O2> void removeSmallClasses(List<O1> instances, List<O2> labels, int smallClassSize) {
		Map<O2, Integer> counts = new HashMap<O2, Integer>();

		for (int i = 0; i < labels.size(); i++) {
			if (!counts.containsKey(labels.get(i))) {
				counts.put(labels.get(i), 1);
			} else {
				counts.put(labels.get(i), counts.get(labels.get(i)) + 1);
			}
		}

		List<O2> keepLabels = new ArrayList<O2>();
		List<O1> keepInstances = new ArrayList<O1>();

		for (int i = 0; i < labels.size(); i++) {
			if (counts.get(labels.get(i)) >= smallClassSize) { 
				keepInstances.add(instances.get(i));
				keepLabels.add(labels.get(i));
			}
		}

		instances.clear();
		instances.addAll(keepInstances);
		labels.clear();
		labels.addAll(keepLabels);
	}
	
}
