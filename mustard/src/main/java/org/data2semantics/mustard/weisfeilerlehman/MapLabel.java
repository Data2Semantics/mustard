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
	private Map<Integer, String> prevNBH;
	private Map<Integer, Boolean> sameAsPrev;
	
	public MapLabel() {
		map = new HashMap<Integer,StringBuilder>();
		prevNBH = new HashMap<Integer, String>();
		sameAsPrev = new HashMap<Integer, Boolean>();
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
	
	public void clear(Integer key) {
		map.get(key).delete(0, map.get(key).length());
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

