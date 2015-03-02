package org.data2semantics.mustard.weisfeilerlehman;

/**
 * Wrapper class for StringBuilder, to enable save usage in nodes graph library.
 * The usage is now save, because the hashcode() is different for each StringLabel object, regardless of content.
 * Consequently the methods that select nodes/links based on labels/tags are also not overly useful when using StringLabel as label/tag.
 * 
 * @author Gerben
 *
 */
public class StringLabel {
	private StringBuilder sb;
	private String prevNBH;
	private boolean sameAsPrev;
	private String lastAdded;
	private int lastAddedCount;
	private int depth;
	
	public StringLabel() {
		this(new String());
		sameAsPrev = false;
	}

	public StringLabel(String s, int depth) {
		sb = new StringBuilder(s);
		this.lastAdded = "";
		this.lastAddedCount = 0;
		this.depth = depth;
	}
	
	public StringLabel(String s) {
		this(s,0);
	}
	
	public void append(String s) {
		if (s.equals(lastAdded)) {
			lastAddedCount++;
		} else {
			lastAddedCount = 0;
		}
		this.lastAdded = s;
		
		sb.append(s);
	}
	
	public void clear() {
		sb.delete(0, sb.length());
		this.lastAdded = "";
		this.lastAddedCount = 0;
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}

	public String getPrevNBH() {
		return prevNBH;
	}

	public void setPrevNBH(String prevNBH) {
		this.prevNBH = prevNBH;
	}

	public boolean isSameAsPrev() {
		return sameAsPrev;
	}

	public void setSameAsPrev(boolean sameAsPrev) {
		this.sameAsPrev = sameAsPrev;
	}

	public String getLastAdded() {
		return lastAdded;
	}
	
	public int getLastAddedCount() {
		return lastAddedCount;
	}
	
	public int getDepth() {
		return depth;
	}
}
