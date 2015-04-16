package org.data2semantics.mustard.utils;

import java.util.Map;

public class WalkCountUtils {

	public static String getFeatureDecription(Map<Integer,String> reverseLabelDict, Map<Integer, String> reverseWalkDict, int index) {
		String walk = reverseWalkDict.get(index);
		
		StringBuilder label = new StringBuilder();
		
		String[] split = walk.split("_");
		
		for (int i = 1; i < split.length; i++) { // first element is empty
			if (i > 1) {
				label.append("->");
			} 
			label.append(reverseLabelDict.get(Integer.parseInt(split[i])));
		}
		return label.toString();
	}
}
