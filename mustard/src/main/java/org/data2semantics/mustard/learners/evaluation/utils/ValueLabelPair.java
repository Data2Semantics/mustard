package org.data2semantics.mustard.learners.evaluation.utils;

public class ValueLabelPair implements Comparable<ValueLabelPair> {
	private double value;
	private boolean label;
	
	public ValueLabelPair(double value, boolean label) {
		this.value = value;
		this.label = label;
	}

	public int compareTo(ValueLabelPair o) {
		if (value - o.value > 0) {
			return 1;
		} else if (value - o.value < 0) {
			return -1;
		} else {
			return 0;
		}
	}

	public double getValue() {
		return value;
	}

	public boolean isLabel() {
		return label;
	}
	
	public String toString() {
		return "(" + value + "," + label + ")";
	}
}