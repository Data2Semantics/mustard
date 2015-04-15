package org.data2semantics.mustard.learners.libsvm;

import org.data2semantics.mustard.kernels.Kernel;

/**
 * Very simple wrapper for the svm_model class, this just stores the model.
 * Contents are only accessible within the libsvm package.
 * 
 * @author Gerben
 *
 */
public class LibSVMModel {
	private svm_model model;
	private Kernel kernelSetting;
	
	LibSVMModel(svm_model model) {
		this.model = model;
	}
		
	svm_model getModel() {
		return model;
	}

	public Kernel getKernelSetting() {
		return kernelSetting;
	}

	public void setKernelSetting(Kernel kernelSetting) {
		this.kernelSetting = kernelSetting;
	}
	
	public boolean hasProbabilities() {
		return model.param.probability == 1;
	}
	
	/*
	public double[] getRho() {
		return model.rho;
	}
	*/

}
