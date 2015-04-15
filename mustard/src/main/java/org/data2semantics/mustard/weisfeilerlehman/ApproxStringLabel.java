package org.data2semantics.mustard.weisfeilerlehman;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Wrapper class for StringBuilder, to enable save usage in nodes graph library.
 * The usage is now save, because the hashcode() is different for each StringLabel object, regardless of content.
 * Consequently the methods that select nodes/links based on labels/tags are also not overly useful when using StringLabel as label/tag.
 * 
 * @author Gerben
 *
 */
public class ApproxStringLabel {
	private StringBuilder sb;
	private String prevNBH;
	private Set<String> prevNBHs;
	private int sameAsPrev;
	private String lastAdded;
	private int lastAddedCount;
	private int depth;
	
	private List<String> iterations;
	private Set<Integer> instanceIndexSet;
	
	public ApproxStringLabel() {
		this(new String());
	}

	public ApproxStringLabel(String s, int depth) {
		sb = new StringBuilder(s);
		this.lastAdded = "";
		this.lastAddedCount = 0;
		this.depth = depth;
		this.iterations = new ArrayList<String>();
		this.instanceIndexSet = new HashSet<Integer>();
		this.prevNBHs = new HashSet<String>();
	}
	
	public ApproxStringLabel(String s) {
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
	
	public Set<String> getPrevNBHs() {
		 return prevNBHs;
	}

	public String getPrevNBH() {
		return prevNBH;
	}

	public void setPrevNBH(String prevNBH) {
		this.prevNBH = prevNBH;
		this.prevNBHs.add(prevNBH);
	}

	public int getSameAsPrev() {
		return sameAsPrev;
	}

	public void setSameAsPrev(int sameAsPrev) {
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
	
	public void addIteration(String it) {
		iterations.add(it);
	}
	
	public List<String> getIterations() {
		 return iterations;
	}
	
	public void addInstanceIndex(int index) {
		instanceIndexSet.add(index);
	}
	
	public Set<Integer> getInstanceIndexSet() {
		return instanceIndexSet;
	}
 
}
