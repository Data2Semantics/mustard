package org.data2semantics.mustard.utils;

import org.nodes.util.Functions.Dir;

public class LabelTagPair<L,T> {
	private L label;
	private T tag;
	private Dir direction;

	public LabelTagPair(L label, T tag, Dir direction) {
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

	public Dir getDirection() {		
		return direction;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + label.hashCode();
		result = prime * result + tag.hashCode();
		result = prime * result + direction.hashCode();
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
		if (other.getLabel().equals(label) && other.getTag().equals(tag) && other.getDirection().equals(direction)) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		if (direction == Dir.IN) {
			return "->" + tag + "->" + label;
		}
		else {
			return label + "->" + tag + "->";
		}
	}
}


