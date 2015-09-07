package org.data2semantics.mustard.utils;


/**
 * Simple implementation of a pair of two objects of type O1, O2 datastructure.
 * Note that equals and hashcode methods are overriden, such that two pairs are equal if their first and second object are equal.
 * 
 * @author Gerben
 *
 */
public class Pair<O1,O2> {
	O1 first;
	O2 second;

	public Pair(O1 first, O2 second) {
		this.first = first;
		this.second = second;
	}

	public O1 getFirst() {
		return first;
	}

	public O2 getSecond() {
		return second;
	}
	
	@Override
	public String toString() {
		return "(" + first + "," + second + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			 return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Pair<?,?>))
			return false;
		
		return (first.equals(((Pair<?,?>) obj).getFirst()) && second.equals(((Pair<?,?>) obj).getSecond()));
	}

	@Override
	public int hashCode() {
		return (Integer.toString(first.hashCode()) + Integer.toString(second.hashCode())).hashCode();
	}
	
	
	
	
}
