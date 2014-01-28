package org.data2semantics.mustard.kernels;

import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of a simple bucket for the bucket-sort that is used in the Weisfeiler-Lehman algorithm used in the WLSubTree kernels.
 * 
 * @author Gerben
 *
 * @param <T> Type of the Objects in the bucket
 */
public class Bucket<T> {
	private String label;
	private List<T> contents;

	public Bucket(String label) {
		this.label = label;
		contents = new ArrayList<T>();
	}

	public List<T> getContents() {
		return contents;
	}

	public String getLabel() {
		return label;
	}
}
