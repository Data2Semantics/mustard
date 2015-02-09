package org.data2semantics.mustard.learners.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.data2semantics.mustard.learners.Prediction;
import org.data2semantics.mustard.learners.SparseVector;


/**
 * This class is intended to reorder, the target, kernel/SparseVectors so that splitting them up into X consecutive folds leads to stratified folds
 * 
 * @author Gerben
 *
 */
public class Stratifier {
	private static final long seed = 1234;
	
	public static List<Integer> stratifyFolds(double[] labels, int numFolds) {
		Random rand = new Random(seed);
		List<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < labels.length; i++) {
			indices.add(i);
		}

		Map<Double, List<Integer>> classes = new HashMap<Double, List<Integer>>();

		for (int i = 0; i < labels.length; i++) {
			if (!classes.containsKey(labels[i])) {
				classes.put(labels[i], new ArrayList<Integer>());
			}
			classes.get(labels[i]).add(i);
		}

		List<Entry<Double,List<Integer>>> entries = new ArrayList<Entry<Double,List<Integer>>>(classes.entrySet());
		Collections.sort(entries, new EntryComparator());

		int[] folds =  new int[numFolds];
		int[] foldStarts = new int[numFolds];
		int[] foldEnds = new int[numFolds];	

		for (int i = 0; i < numFolds; i++) {
			folds[i] = CVUtils.foldStart(labels.length, numFolds, i+1);
			foldStarts[i] = CVUtils.foldStart(labels.length, numFolds, i+1);
			foldEnds[i] = CVUtils.foldEnd(labels.length, numFolds, i+1);
		}

		for (Entry<Double, List<Integer>> entry : entries) {
			while (entry.getValue().size() >= numFolds) { // do as long as we can fill all folds with a new instance
				for (int i = 0; i < numFolds; i++) {
					indices.set(folds[i], entry.getValue().get(0));
					entry.getValue().remove(0);
					folds[i]++;
				}
			}
		}

		Collections.sort(entries, new EntryComparator()); // sort again, since the amount of instances left per class has likely changed, also for each class, it is now < numFolds

		for (Entry<Double, List<Integer>> entry : entries) {
			boolean[] used = new boolean[numFolds];
			for (int i = 0; i < numFolds; i++) {
				used[i] = false;
			}
			while (entry.getValue().size() > 0) {
				int fold = rand.nextInt(numFolds);
				if (!used[fold] && folds[fold] < foldEnds[fold]) { // we didn't use this fold yet, and we still have space
					indices.set(folds[fold], entry.getValue().get(0));
					entry.getValue().remove(0);
					folds[fold]++;
					used[fold] = true;
				}
			}			
		}

		// Reshuffle each fold
		for (int i = 0; i < numFolds; i++) {
			List<Integer> foldList = new ArrayList<Integer>(indices.subList(foldStarts[i],foldEnds[i]));
			Collections.shuffle(foldList, rand);
			for (int j = 0; j < foldList.size(); j++) {
				indices.set(j + foldStarts[i], foldList.get(j));
			}
		}
		
		return indices;
	}
	
	public static List<Integer> stratifySplit(double[] labels, double fraction) {
		Random rand = new Random(seed);
		List<Integer> indices = new ArrayList<Integer>();

		Map<Double, List<Integer>> classes = new HashMap<Double, List<Integer>>();

		for (int i = 0; i < labels.length; i++) {
			if (!classes.containsKey(labels[i])) {
				classes.put(labels[i], new ArrayList<Integer>());
			}
			classes.get(labels[i]).add(i);
		}

		List<Entry<Double,List<Integer>>> entries = new ArrayList<Entry<Double,List<Integer>>>(classes.entrySet());
		Collections.sort(entries, new EntryComparator());

		List<Integer> train = new ArrayList<Integer>();
		List<Integer> test = new ArrayList<Integer>();
		
		for (Entry<Double, List<Integer>> entry : entries) {
			double assigned = 0;
			double total = entry.getValue().size();
			while (entry.getValue().size() > 0) {
				if (assigned / total < fraction) {
					train.add(entry.getValue().get(0));
				} else {
					test.add(entry.getValue().get(0));
				}
				entry.getValue().remove(0);
				assigned += 1;
			}
		}
		
		// reshuffle the train and test
		Collections.shuffle(train, rand);
		Collections.shuffle(test, rand);
		
		indices.addAll(train);
		indices.addAll(test);
		
		return indices;
	}

	

	public static double[] shuffle(double[] labels, List<Integer> indices) {
		double[] temp = new double[labels.length];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = labels[indices.get(i)];
		}
		return temp;
	}

	public static SparseVector[] shuffle(SparseVector[] fv, List<Integer> indices) {
		SparseVector[] temp = new SparseVector[fv.length];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = fv[indices.get(i)];
		}	
		return temp;
	}

	public static double[][] shuffle(double[][] kernel, List<Integer> indices) {
		double[][] temp = new double[kernel.length][];
		
		for (int i = 0; i < kernel.length; i++) {
			temp[i] = shuffle(kernel[indices.get(i)], indices);
		}
		return temp;
	}

	public static Prediction[] deshuffle(Prediction[] pred, List<Integer> indices) {
		Prediction[] temp = new Prediction[pred.length];
		
		for (int i = 0; i < pred.length; i++) {
			temp[indices.get(i)] = pred[i];
		}
		return temp;
	}
	
	private static class EntryComparator implements Comparator<Entry<Double, List<Integer>>> {

		public int compare(Entry<Double, List<Integer>> o1,
				Entry<Double, List<Integer>> o2) {
			return o2.getValue().size() - o1.getValue().size(); // We want to sort big to small.
		}

	}


}
