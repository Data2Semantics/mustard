package org.data2semantics.mustard.learners.liblinear;
import org.data2semantics.mustard.kernels.Kernel;

import de.bwaldvogel.liblinear.Model;


public class LibLINEARModel {
	private Model model;
	private Kernel kernelSetting;
	
	LibLINEARModel(Model model) {
		this.model = model;
	}
	
	Model getModel() {
		return model;
	}
	
	public boolean hasProbabilities() {
		return model.isProbabilityModel();
	}
	
	public Kernel getKernelSetting() {
		return kernelSetting;
	}

	public void setKernelSetting(Kernel kernelSetting) {
		this.kernelSetting = kernelSetting;
	}
	
	public int[] getLabels() {
		return model.getLabels();
	}
	
	public WeightIndexPair[][] getFeatureWeights() {
		int nrClass = model.getNrClass() == 2 ? 1 : model.getNrClass();
		
		WeightIndexPair[][] weights = new WeightIndexPair[nrClass][];
		double[] llw = model.getFeatureWeights();
		
		for (int i = 0; i < weights.length; i++) {
			weights[i] = new WeightIndexPair[model.getNrFeature()];
		}
		
		for (int i = 0; i < llw.length; i++) {
			weights[i % nrClass][i / nrClass] = new WeightIndexPair(llw[i], (i/nrClass));
		}
		
		return weights;
	}
	
	
	public class WeightIndexPair implements Comparable<WeightIndexPair> {
		private double weight;
		private int index;
		
		public int compareTo(WeightIndexPair weight2) {
			return -Double.compare(weight, weight2.weight); 
		}

		public WeightIndexPair(double weight, int index) {
			super();
			this.weight = weight;
			this.index = index;
		}

		public double getWeight() {
			return weight;
		}

		public int getIndex() {
			return index;
		}
	}
	
}
