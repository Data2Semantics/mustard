package org.data2semantics.mustard.learners;


/**
 * Wrapper class for LibSVM/LibLINEAR prediction, this class stores all the different possible predictions
 * 
 * @author Gerben
 *
 */
public class Prediction implements Comparable<Prediction> {
	private double label;
	private double[] decisionValue;
	private double probability;
	private int index;
	
	public Prediction(double label, int index) {
		this.label = label;
		this.index = index;
	}
	
	public double getLabel() {
		return label;
	}
	public void setLabel(double label) {
		this.label = label;
	}
	public double[] getDecisionValue() {
		return decisionValue;
	}
	public void setDecisionValue(double[] decisionValue) {
		this.decisionValue = decisionValue;
	}
	public double getProbability() {
		return probability;
	}
	public void setProbability(double probability) {
		this.probability = probability;
	}
	
	@Override
	public String toString() {
		return "Test index: " + index + ", " + label + ", " + decisionValue[0];
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	/*
	 *  Compare two predictions so that they can be sorted on decision values.
	 *  Using this sort a ranking for a binary classification problem can be computed.
	 *  However, the sign of the decision value is arbitrary. Thus we assume that the two class labels are +1 and -1, 
	 *  and that positive classes should be ranked higher.
	 *  
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Prediction arg0) {
		return Double.compare(-1 * decisionValue[0] * label, -1 * arg0.getDecisionValue()[0] * arg0.getLabel());
	}
	
	
}
