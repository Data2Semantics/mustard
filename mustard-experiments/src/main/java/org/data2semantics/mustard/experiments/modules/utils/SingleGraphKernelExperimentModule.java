package org.data2semantics.mustard.experiments.modules.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.learners.Prediction;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.libsvm.LibSVM;
import org.data2semantics.mustard.learners.libsvm.LibSVMParameters;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;


@Module(name="Experiment")
public class SingleGraphKernelExperimentModule {
	private double[][] kernel;
	private List<Double> target;
	private double[] targetA;
	private LibSVMParameters parms;
	private long seed;
	private int folds;
	private Prediction[] pred;

	
	public SingleGraphKernelExperimentModule(
			@In(name="matrix") double[][] kernel, 
			@In(name="target") List<Double> target,
			@In(name="parms") LibSVMParameters parms, 
			@In(name="folds") int folds,
			@In(name="seed") int seed) {
		
		this.kernel = kernel;
		this.target = target;
		this.parms = parms;
		this.seed = seed;
		this.folds = folds;
	}
	
	
	@Main
	public List<Double> runExperiment() {
		KernelUtils.shuffle(kernel, seed);
		
		List<Double> targetL = new ArrayList<Double>();
		targetL.addAll(target);
		Collections.shuffle(targetL, new Random(seed));
		
		targetA = new double[targetL.size()];
		for (int i = 0; i < targetA.length; i++) {
			targetA[i] = targetL.get(i);
		}

		pred = LibSVM.crossValidate(kernel, targetA, parms, folds);
		
		List<Double> res = new ArrayList<Double>();
		
		for (Prediction p : pred) {
			res.add(p.getLabel());
		}
		return res;
	}
	
	
	@Out(name="accuracy")
	public double getAccuracy() {
		return new Accuracy().computeScore(targetA, pred);
	}
	
	@Out(name="f1")
	public double getF1() {
		return new F1().computeScore(targetA, pred);
	}
	
	

}
