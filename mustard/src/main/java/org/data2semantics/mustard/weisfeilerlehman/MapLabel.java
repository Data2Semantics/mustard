package org.data2semantics.mustard.weisfeilerlehman;

import java.util.Arrays;
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
	private String[] prevNBH;
	private boolean[] sameAsPrev;
	private Set<Integer> keySet;


	public MapLabel(int mapSize) {
		map = new StringBuilder[mapSize];
		prevNBH = new String[mapSize];
		sameAsPrev = new boolean[mapSize];
		keySet = new HashSet<Integer>(mapSize);
	}


	public MapLabel() {
		this(4);
	}

	public Set<Integer> keySet() {
		return keySet;
	}

	/**
	 * TODO rewrite put() and get(), to one method append(index, string) to be in line with StringLabel
	 * 
	 * @param key
	 * @param value
	 */
	public void put(Integer key, StringBuilder value) {
		if (key >= map.length) {
			StringBuilder[] map2 = new StringBuilder[key+1]; 
			String[] prevNBH2 = new String[key+1];
			boolean[] sameAsPrev2 = new boolean[key+1];
			for (int i = 0; i < map.length; i++) {
				map2[i] = map[i];
				prevNBH2[i] = prevNBH[i];
				sameAsPrev2[i] = sameAsPrev[i];
			}

			map = map2;
			prevNBH = prevNBH2;
			sameAsPrev = sameAsPrev2;
		}

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
		return Arrays.toString(map);
	}

	public void putPrevNBH(Integer key, String value) {
		prevNBH[key] = value;
	}

	public String getPrevNBH(Integer key) {
		return prevNBH[key];
	}

	public void putSameAsPrev(Integer key, Boolean value) {
		sameAsPrev[key] = value;
	}

	public boolean getSameAsPrev(Integer key) {
		return sameAsPrev[key];
	}
}

