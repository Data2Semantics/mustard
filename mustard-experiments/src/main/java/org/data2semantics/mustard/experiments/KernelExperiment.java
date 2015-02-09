package org.data2semantics.mustard.experiments;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.kernels.Kernel;

public abstract class KernelExperiment<K extends Kernel> implements Runnable {
	protected List<? extends K> kernels;
	protected long[] seeds;
	protected List<Result> results;
	
	public KernelExperiment(List<? extends K> kernels, long[] seeds) {
		super();
		this.kernels = kernels;
		this.seeds = seeds;
		results = new ArrayList<Result>();
	}

	public abstract void run();

	public List<Result> getResults() {
		return results;
	}
}
