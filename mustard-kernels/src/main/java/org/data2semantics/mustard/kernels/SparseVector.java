package org.data2semantics.mustard.kernels;

import java.util.Set;
import java.util.TreeMap;


/**
 * <p>
 * Class implementing a Sparse Vector using an interal TreeMap, for long sparse vectors this significantly improves the computation of a dot-product over the naive approach
 * </p>
 * <p>
 * Note that SparseVector's start at index 0. Therefore a newly created SparseVector has lastIndex == -1. 
 * The length of the SparseVector is lastIndex + 1 (if lastIndex is correctly set by the calling code).
 * </p>
 * 
 * 
 * @author Gerben
 */
public class SparseVector {
	private TreeMap<Integer, Double> vector;
	private int[] indices;
	private double[] values;
	private int lastIndex;
	
	private boolean converted;
	
	public SparseVector() {
		vector = new TreeMap<Integer,Double>();
		converted = false;
		lastIndex = -1;
	}
	
	public SparseVector(SparseVector v) {
		this();
		for (int i : v.getIndices()) {
			setValue(i, v.getValue(i));
		}
		this.lastIndex = v.getLastIndex();
	}
	
	
	/**
	 * Add the Sparsevector v to this vector in the form of a concatenation, i.e. the first index of v is the lastindex of this vector + 1.
	 * 
	 * @param v
	 */
	public void addVector(SparseVector v) {
		if (this.size() > 0 && lastIndex == -1) {
			throw new RuntimeException("Vectors cannot be added. Vector is > 0, but lastIndex is not set.");
		}
				
		for (int k : v.getIndices()) {
			vector.put(k + lastIndex + 1, v.getValue(k));
		}
		this.lastIndex += v.getLastIndex();
		converted = false;
	}
	
	
	/**
	 * sum the two vectors, i.e. sum the values at the same index
	 * 
	 * @param v
	 */
	public void sumVector(SparseVector v) {
		for (int i : v.getIndices()) {
			setValue(i, getValue(i) + v.getValue(i));
		}
		this.lastIndex = Math.max(lastIndex, v.getLastIndex());
		converted = false;
	}
	
	/**
	 * Multiply the vector with a scalar
	 * 
	 * @param scalar
	 */
	public void multiplyScalar(double scalar) {
		for (int i : vector.keySet()) {
			vector.put(i, vector.get(i) * scalar);
		}
		converted = false;
	}
	
	/**
	 * Set the value at int index to the given double value
	 * 
	 * @param index
	 * @param value
	 */
	public void setValue(int index, double value) {
		vector.put(index, value);
		converted = false;
	}
	
	/**
	 * Get the value at the given index
	 * 
	 * @param index
	 * @return
	 */
	public double getValue(int index) {
		Double value = vector.get(index);
		if (value != null) {
			return value;
		} else {
			return 0;
		}
	}
	
	/**
	 * Return all the indices that have non-zero values
	 * 
	 * @return
	 */
	public Set<Integer> getIndices() {
		return vector.keySet();
	}
	
	
	/**
	 * returns the number of non-zero elements in the vector (i.e. the size of the internal Map)
	 * 
	 * @return
	 */
	public int size() {
		return vector.size();
	}
		
	/**
	 * Get the last index used for this vector. Note that the value at this index can be zero.
	 * Also, since the implementation is sparse, the last index value has to be set by hand (by the setLastIndex() method).
	 * 
	 * @return
	 */
	public int getLastIndex() {
		return lastIndex;
	}

	
	/**
	 * Set the value of last index that is potentially used (can be 0 for this specific SparseVector). 
	 * Note that the SparseVector does not do this itself, this is the responsibility of the user of the code
	 * Needed when we want to use {@link addVector()}.
	 * 
	 * @param lastIndex
	 */
	public void setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
	}

	
	/**
	 * compute the dot product with the SparseVector v2
	 * 
	 * @param v2
	 * @return
	 */
	public double dot(SparseVector v2) {
		int i = 0, j = 0;
		double ret = 0;
		
		if (!converted) {
			convert2Arrays();
		}
		if (!v2.converted) {
			v2.convert2Arrays();
		}
		
		while (i < indices.length && j < v2.indices.length) {
			if (indices[i] > v2.indices[j]) {
				j++;
			} else if (indices[i] < v2.indices[j]) {
				i++;
			} else {
				ret += values[i] * v2.values[j];
				i++;
				j++;
			}		
		}
		return ret;
	}	
	
	public void clearConversion() {
		converted = false;
		indices = null;
		values = null;
	}
	
	private void convert2Arrays() {
		indices = new int[vector.size()];
		values = new double[vector.size()];
		
		int i = 0;
		for (int key : vector.keySet()) {
			indices[i] = key;
			values[i] = vector.get(key);
			i++;
		}
		converted = true;
	}
	
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		
		for (int key : vector.keySet()) {
			res.append(key +  ":" + vector.get(key) + ", ");
		}
		return res.toString();
	}
	
	
	/*
	svm_node[] convert2svmNodes() {
		svm_node[] ret = new svm_node[vector.size()];
		
		int i = 0;
		for (int key : vector.keySet()) {
			ret[i] = new svm_node();
			ret[i].index = key;
			ret[i].value = vector.get(key);
			i++;
		}
		return ret;
	}
	*/
}
