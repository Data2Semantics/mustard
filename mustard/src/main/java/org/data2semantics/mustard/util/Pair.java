package org.data2semantics.mustard.util;


/**
 * Simple implementation of a pair of two objects of type O datastructure.
 * Note that equals and hashcode methods are overriden, such that two pairs are equal of their first and second object are equal.
 * 
 * @author Gerben
 *
 * @param <O>
 */
public class Pair<O> {
	O first;
	O second;

	public Pair(O first, O second) {
		this.first = first;
		this.second = second;
	}

	public O getFirst() {
		return first;
	}

	public O getSecond() {
		return second;
	}
	
	public String toString() {
		return "(" + first + "," + second + ")";
	}

	public boolean equals(Object obj) {
		if (this == obj)
			 return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Pair<?>))
			return false;
		
		return (first.equals(((Pair<?>) obj).getFirst()) && second.equals(((Pair<?>) obj).getSecond()));
	}

	@Override
	public int hashCode() {
		return (Integer.toString(first.hashCode()) + Integer.toString(second.hashCode())).hashCode();
	}
	
	
	
	
}
