package org.data2semantics.mustard.weisfeilerlehman;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for a Map from Integer to StringBuilder as labels
 * 
 * @author Gerben
 *
 */
public class ApproxMapLabel {
	private Map<Integer, StringBuilder> map;
	private Map<Integer, String> lastAdded;
	private Map<Integer, Integer> lastAddedCount;
	private Map<Integer, String> prevNBH;
	private Map<Integer, Integer> sameAsPrev;


	public ApproxMapLabel(int mapSize) {
		map = new HashMap<Integer, StringBuilder>(mapSize);
		lastAdded = new HashMap<Integer, String>(mapSize);
		lastAddedCount = new HashMap<Integer, Integer>(mapSize);
		prevNBH = new HashMap<Integer, String>(mapSize);
		sameAsPrev = new HashMap<Integer, Integer>(mapSize);
	}


	public ApproxMapLabel() {
		this(4);
	}

	public Set<Integer> keySet() {
		return map.keySet();
	}

	/**
	 * append(), clear() and get() hide the StringBuilder's that are used
	 * 
	 */
	public void append(Integer key, String value) {
		if (!map.containsKey(key)) { // init if we have nothing for this key yet
			map.put(key, new StringBuilder());
			lastAdded.put(key, "");
			lastAddedCount.put(key, 0);
		}
		if (value.equals(lastAdded.get(key))) {
			lastAddedCount.put(key, lastAddedCount.get(key) + 1);
		} else {
			lastAddedCount.put(key, 0);
		}
		lastAdded.put(key, value);

		map.get(key).append(value);
	}

	public String get(Integer key) {
		return map.get(key).toString();
	}

	public boolean containsKey(Integer key) {
		return map.containsKey(key);
	}

	public void clear(Integer key) {
		if (map.containsKey(key)) {
			map.get(key).delete(0, map.get(key).length());
			lastAdded.put(key, "");
			lastAddedCount.put(key, 0);
		}
	}

	@Override
	public String toString() {
		return map.toString();
	}

	public void putPrevNBH(Integer key, String value) {
		prevNBH.put(key, value);
	}

	public String getPrevNBH(Integer key) {
		return prevNBH.get(key);
	}

	public void putSameAsPrev(Integer key, Integer value) {
		sameAsPrev.put(key, value);
	}

	public int getSameAsPrev(Integer key) {
		return sameAsPrev.get(key);
	}

	public String getLastAdded(Integer key) {
		return lastAdded.get(key);
	}

	public int getLastAddedCount(Integer key) {
		return lastAddedCount.get(key);
	}
}

