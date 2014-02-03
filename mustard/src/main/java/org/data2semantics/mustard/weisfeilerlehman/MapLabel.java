package org.data2semantics.mustard.weisfeilerlehman;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for a Map from Integer to StringBuilder as labels
 * 
 * @author Gerben
 *
 */
public class MapLabel {
	private Map<Integer,StringBuilder> map;
	
	public MapLabel() {
		map = new HashMap<Integer,StringBuilder>();
	}
	
	public Set<Integer> keySet() {
		return map.keySet();
	}
	
	public void put(Integer key, StringBuilder value) {
		map.put(key, value);
	}
	
	public StringBuilder get(Integer key) {
		return map.get(key);
	}

	public boolean containsKey(Integer key) {
		return map.containsKey(key);
	}

	public String toString() {
		return map.toString();
	}
}

