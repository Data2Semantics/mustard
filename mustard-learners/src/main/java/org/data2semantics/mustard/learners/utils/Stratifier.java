package org.data2semantics.mustard.learners.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.learners.Prediction;


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
		int[] foldSizes = new int[numFolds];

		for (int i = 0; i < numFolds; i++) {
			folds[i] = CVUtils.foldStart(labels.length, numFolds, i+1);
			foldStarts[i] = CVUtils.foldStart(labels.length, numFolds, i+1);
			foldEnds[i] = CVUtils.foldEnd(labels.length, numFolds, i+1);
			foldSizes[i] = foldEnds[i] - foldStarts[i];
		}

		// 1. Correct for uneven fold sizes by filling the larger folds with one instance each, according to the distribution of the labels
		List<Set<Double>> foldCheck = new ArrayList<Set<Double>>();
		for (int i = 0; i < numFolds; i++) {
			foldCheck.add(new HashSet<Double>());
		}

		if (labels.length % numFolds > 0) {
			int largeFold = 0;
			for (int size : foldSizes) {
				largeFold = Math.max(largeFold, size);
			}
			double numLF = 0;
			boolean[] largeFolds = new boolean[numFolds];
			for (int i = 0; i < largeFolds.length; i++) {
				if (foldSizes[i] == largeFold) {
					largeFolds[i] = true;
					numLF++;
				} else {
					largeFolds[i] = false;
				}
			}

			List<Integer> adds = new ArrayList<Integer>();
			List<Double> addsLabels = new ArrayList<Double>();

			double tot = 0; 
			double added = 0;
			for (Entry<Double, List<Integer>> entry : entries) {
				tot += entry.getValue().size();
				while (added / numLF < (tot / (double) labels.length)) {
					adds.add(entry.getValue().get(0));
					addsLabels.add(entry.getKey());
					entry.getValue().remove(0);
					added++;
				}
			}

			for (int i = 0; i < numFolds; i++) {
				if (largeFolds[i]) {
					indices.set(folds[i], adds.get(0));
					foldCheck.get(i).add(addsLabels.get(0)); // store that we already put an additional instance with this label in this fold
					adds.remove(0);
					addsLabels.remove(0);
					folds[i]++;
				}
			}
		}

		// 2. Fill the folds evenly
		for (Entry<Double, List<Integer>> entry : entries) {
			while (entry.getValue().size() >= numFolds) { // do as long as we can fill all folds with a new instance
				for (int i = 0; i < numFolds; i++) {
					indices.set(folds[i], entry.getValue().get(0));
					entry.getValue().remove(0);
					folds[i]++;
				}
			}
		}


		//Collections.sort(entries, new EntryComparator()); // sort, we want to start with the biggest class left
		//Collections.reverse(entries);

		// 3. Distribute remaining entries evenly
		// The following loop to handle the remaining entries is way to ugly and complicated.... URGH...		
		boolean[] free = new boolean[numFolds];
		for (int i = 0; i < numFolds; i++) { free[i] = true; }

		for (Entry<Double, List<Integer>> entry : entries) {

			// Reset which fold is used per entry/class/label
			boolean[] used = new boolean[numFolds];
			for (int i = 0; i < numFolds; i++) { used[i] = false; }

			while (!entry.getValue().isEmpty()) {
				int fold = rand.nextInt(numFolds);

				// Check if we are full
				boolean full = true;
				for (boolean p : free) {
					if (p) {full = false; break; }
				}

				if (full) {
					// Free stuff, that is not used for this class/label/entry
					for (int i = 0; i < numFolds; i++) {
						if (!used[i]) { free[i] = true; }
					}
				}

				if (free[fold]	// fold free?
						&& folds[fold] < foldEnds[fold] // still space?
								&& !foldCheck.get(fold).contains(entry.getKey())) { // this fold did not get anything with this label yet during the uneven fold correction

					indices.set(folds[fold], entry.getValue().get(0));
					entry.getValue().remove(0);
					folds[fold]++;
					free[fold] = false;
					used[fold] = true;
				}
			}
		}

		// 4. Reshuffle each fold, just to be safe
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
			return o2.getValue().size() - o1.getValue().size(); // We want to sort big to small
		}

	}


}
