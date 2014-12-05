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
public class MapLabel {
	private StringBuilder[] map;
	private Map<Integer, String> prevNBH;
	private Map<Integer, Boolean> sameAsPrev;
	private Set<Integer> keySet;


	public MapLabel(int mapSize) {
		map = new StringBuilder[mapSize];
		prevNBH = new HashMap<Integer, String>();
		sameAsPrev = new HashMap<Integer, Boolean>();
		keySet = new HashSet<Integer>();
	}


	public MapLabel() {
		this(3);
	}

	public Set<Integer> keySet() {
		return keySet;
	}

	public void put(Integer key, StringBuilder value) {
		if (map[key] == null) {
			keySet.add(key);
		}
		map[key] = value;
	}

	public StringBuilder get(Integer key) {
		return map[key];
	}

	public boolean containsKey(Integer key) {
		return keySet.contains(key);
	}

	public void clear(Integer key) {
		map[key].delete(0, map[key].length());
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

	public void putSameAsPrev(Integer key, Boolean value) {
		sameAsPrev.put(key, value);
	}

	public boolean getSameAsPrev(Integer key) {
		Boolean ret = sameAsPrev.get(key);
		return (ret == null) ? false : ret;
	}
}

