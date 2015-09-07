package org.data2semantics.mustard.utils;


/**
 * Utility class for the Hub removal methods in {@link org.data2semantics.mustard.utils.HubUtils}.
 * 
 * @author Gerben
 *
 * @param <L>
 * @param <T>
 */
public class LabelTagPair<L,T> {
	public static final int DIR_IN = 1;
	public static final int DIR_OUT = 2;
	
	
	private L label;
	private T tag;
	private int direction;

	public LabelTagPair(L label, T tag, int direction) {
		super();
		this.label = label;
		this.tag = tag;
		this.direction = direction;
	}

	public L getLabel() {
		return label;
	}

	public T getTag() {
		return tag;
	}

	public int getDirection() {		
		return direction;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + label.hashCode();
		result = prime * result + tag.hashCode();
		result = prime * result + direction;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LabelTagPair other = (LabelTagPair) obj;
		if (other.getLabel().equals(label) && other.getTag().equals(tag) && other.getDirection() == direction) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		if (direction == DIR_IN) {
			return "->" + tag + "->" + label;
		}
		else {
			return label + "->" + tag + "->";
		}
	}
}


