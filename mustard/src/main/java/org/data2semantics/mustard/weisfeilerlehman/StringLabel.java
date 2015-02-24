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
	
	public StringLabel() {
		this(new String());
		sameAsPrev = false;
	}

	public StringLabel(String s) {
		sb = new StringBuilder(s);
	}
	
	public void append(String s) {
		this.lastAdded = s;
		sb.append(s);
	}
	
	public void clear() {
		sb.delete(0, sb.length());
		this.lastAdded = "";
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
}
