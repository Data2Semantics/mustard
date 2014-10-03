package org.data2semantics.mustard.experiments.utils;

public interface LargeClassificationDataSet extends ClassificationDataSet {
	public void createSubSet(long seed, double fraction, int minClassSize, int maxNumClasses);
}
