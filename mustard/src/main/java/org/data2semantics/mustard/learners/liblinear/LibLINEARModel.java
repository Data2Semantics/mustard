package org.data2semantics.mustard.learners.liblinear;
import de.bwaldvogel.liblinear.Model;


public class LibLINEARModel {
	private Model model;
	private String kernelSetting;
	
	LibLINEARModel(Model model) {
		this.model = model;
	}
	
	Model getModel() {
		return model;
	}
	
	public String getKernelSetting() {
		return kernelSetting;
	}

	public void setKernelSetting(String kernelSetting) {
		this.kernelSetting = kernelSetting;
	}
	
	public WeightIndexPair[][] getFeatureWeights() {
		int nrClass = model.getNrClass() == 2 ? 1 : model.getNrClass();
		
		WeightIndexPair[][] weights = new WeightIndexPair[nrClass][];
		double[] llw = model.getFeatureWeights();
		
		for (int i = 0; i < weights.length; i++) {
			weights[i] = new WeightIndexPair[model.getNrFeature()];
		}
		
		for (int i = 0; i < llw.length; i++) {
			weights[i % nrClass][i / nrClass] = new WeightIndexPair(llw[i], (i/nrClass)+1);
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
