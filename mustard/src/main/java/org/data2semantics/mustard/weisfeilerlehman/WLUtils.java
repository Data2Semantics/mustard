package org.data2semantics.mustard.weisfeilerlehman;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.utils.Pair;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;

public class WLUtils {
	
	
	public static String getFeatureDecription(Map<String,String> reverseDict, int index) {
		return getFeatureDescriptionRec(reverseDict, Integer.toString(index), false);
	}


	/**
	 * Function to recursively "unravel" the subtree label generated by WL, note that this is based on knowledge of the WL implementation,
	 * ie. "_" are used to concatenate labels.
	 * 
	 * @param reverseDict
	 * @param index
	 * @return
	 */
	private static String getFeatureDescriptionRec(Map<String,String> reverseDict, String index, boolean onlyFirst) {
		String lab = reverseDict.get(index);	

		// can we split the labels on "_"
		if (lab.contains("_")) {
			String[] split = lab.split("_");

			if (reverseDict.containsKey(split[0])) {  // if the first string is in the dict than we can assume that "_" were used for concat
				String output = getFeatureDescriptionRec(reverseDict, split[0], true);

				if (!onlyFirst) { // We only want to unfold the concatenated label once
					output += "->";

					output += "(";				 
					for (int i = 1; i < split.length; i++) {
						if (i != 1) {
							output += ",";
						}
						output += getFeatureDescriptionRec(reverseDict, split[i], false);
					}
					output += ")";
				}	
				return output;		

			} else { // "_" not used for concat, regular label, and also has to be end label, so this is a base case
				return lab;
			}		
		} else {
			if (reverseDict.containsKey(lab)) {
				return getFeatureDescriptionRec(reverseDict, lab, true);
			} else { // Other base case
				return lab;
			}
		}	
	}

}
