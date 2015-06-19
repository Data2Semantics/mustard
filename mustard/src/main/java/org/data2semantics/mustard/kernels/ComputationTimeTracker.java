package org.data2semantics.mustard.kernels;


/**
 * Simple interface that defines if a kernel tracks the computation time.
 * 
 * @author Gerben
 *
 */
public interface ComputationTimeTracker {
	public long getComputationTime();
}
