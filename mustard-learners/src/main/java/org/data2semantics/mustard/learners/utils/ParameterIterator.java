package org.data2semantics.mustard.learners.utils;

public class ParameterIterator {
	private double[] parms;
	private int testedParms;
	private int maxParms;
	private int lowerBoundary;
	private int upperBoundary;
	private int currentIndex;
	private int bestIndex;
	private double factor;
	private boolean normalIteration;

	public ParameterIterator(double[] parms) {
		this.parms = parms;
		this.maxParms = 11;
		this.testedParms = 0;
		this.lowerBoundary = 0;
		this.upperBoundary = parms.length - 1;
		this.currentIndex = 0;
		this.bestIndex = 0;
		this.normalIteration = true;

		if (parms.length > 1) {
			factor = parms[1] / parms[0];
		} else {
			factor = 10;
		}	
	}

	public double nextParm() {
		if (normalIteration && currentIndex <= upperBoundary) { // Within the given parms
			return parms[currentIndex];
		} else { // Else we reached the end of normalIteration or we are already out of it
			normalIteration = false;
		}
		if (bestIndex == lowerBoundary) {
			lowerBoundary = bestIndex - 1;
			currentIndex = lowerBoundary;
			return Math.pow(factor, bestIndex - 1.0);
		}
		if (bestIndex == upperBoundary) {
			upperBoundary = bestIndex + 1;
			currentIndex = upperBoundary;
			return Math.pow(factor, bestIndex + 1.0);
		}
		return 0.0;
	}

	public void updateParm(boolean bestScore) {
		if (bestScore) {
			bestIndex = currentIndex;
		}
		if (normalIteration) {
			currentIndex++;
		}
		testedParms++;		
	}

	public boolean hasNext() {
		if (testedParms < maxParms) {
			if (normalIteration && currentIndex <= upperBoundary) { // Within the given parms
				return true;
			}
			if (bestIndex == lowerBoundary) {
				return true;
			}
			if (bestIndex == upperBoundary) {
				return true;
			}
		}
		return false;
	}

}
