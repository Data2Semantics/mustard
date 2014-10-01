package org.data2semantics.mustard.experiments.utils;

import java.util.List;

import org.data2semantics.mustard.kernels.data.RDFData;

/**
 * Simple interface to define the elements of a classification dataset.
 *  The method create() should be called before calling getRDFData() and getTarget().
 * 
 * @author Gerben
 */
public interface ClassificationDataSet {
	
	public void create();
	public RDFData getRDFData();
	public List<Double> getTarget();
}
