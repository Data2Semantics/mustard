package org.data2semantics.mustard.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;


/**
 * 
 * Utility functions to do hub removal
 * 
 * @author Gerben
 *
 */
public class HubUtils {

	private static final class ValueComparator<V extends Comparable<? super V>>
	implements Comparator<Map.Entry<?, V>> {
		public int compare(Map.Entry<?, V> o1, Map.Entry<?, V> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	}
	
	/**
	 * Get a sorted list of the hub sizes (descending) from a list of hub frequencies
	 * 
	 * 
	 * @param sortedHubEdges
	 * @return
	 */
	public static List<Integer> getHubSizes(List<Map.Entry<LabelTagPair<String,String>, Integer>> sortedHubEdges) {
		Set<Integer> sizes = new HashSet<Integer>();
		
		for (Map.Entry<LabelTagPair<String,String>, Integer> e : sortedHubEdges) {
			sizes.add(e.getValue());
		}
		
		List<Integer> sizesList = new ArrayList<Integer>(sizes);
		Collections.sort(sizesList);
		Collections.reverse(sizesList);
		return sizesList;
	}

	/**
	 * Sort the entries in the a hubmap on size
	 * 
	 * @param hubEdges
	 * @return
	 */
	public static List<Map.Entry<LabelTagPair<String,String>, Integer>> sortHubMap(Map<LabelTagPair<String,String>, Integer> hubEdges) {
		List<Map.Entry<LabelTagPair<String,String>, Integer>> list = new ArrayList<Map.Entry<LabelTagPair<String,String>, Integer>>(hubEdges.size());
		list.addAll(hubEdges.entrySet());
		ValueComparator<Integer> cmp = new ValueComparator<Integer>();
		Collections.sort(list, cmp);
		Collections.reverse(list);
		return list;
	}
	
	
	/**
	 * Convert a sorted list of hub entries into a hub map
	 * 
	 * @param sortedHubEdges
	 * @param maxHubs
	 * @return
	 */
	public static Map<LabelTagPair<String,String>, Integer> createHubMapFromSortedLabelTagPairs(List<Map.Entry<LabelTagPair<String,String>, Integer>> sortedHubEdges, int maxHubs) {
		Map<LabelTagPair<String,String>,Integer> hubMap = new HashMap<LabelTagPair<String,String>,Integer>();		

		int i = 0;
		for (Map.Entry<LabelTagPair<String,String>,Integer> he : sortedHubEdges) {
			if (i < maxHubs) {
				hubMap.put(he.getKey(), i++);
			} 
			else {
				break;
			}
		}
		return hubMap;
	}

	/**
	 * Convert a sorted list of hubmap entries into a hub map, including only those entries that have a frequency over minCount
	 * 
	 * @param sortedHubEdges
	 * @param minCount
	 * @return
	 */
	public static Map<LabelTagPair<String,String>, Integer> createHubMapFromSortedLabelTagPairsMinCount(List<Map.Entry<LabelTagPair<String,String>, Integer>> sortedHubEdges, int minCount) {
		Map<LabelTagPair<String,String>,Integer> hubMap = new HashMap<LabelTagPair<String,String>,Integer>();		

		int i = 0;
		for (Map.Entry<LabelTagPair<String,String>,Integer> he : sortedHubEdges) {
			if (he.getValue() >= minCount) {
				hubMap.put(he.getKey(), i++);
			} 
			else {
				break;
			}
		}
		return hubMap;
	}


	/**
	 * Create a hubmap from a graph
	 * 
	 * @param graph
	 * @return
	 */
	public static Map<LabelTagPair<String,String>, Integer> countLabelTagPairs(SingleDTGraph graph) {
		Map<LabelTagPair<String,String>, Integer> edgeCounts = new HashMap<LabelTagPair<String,String>, Integer>();
		Set<DTNode<String,String>> iNodes = new HashSet<DTNode<String,String>>(graph.getInstances());

		for (DTLink<String,String> link : graph.getGraph().links()) {
			if (!iNodes.contains(link.from())) { // instance nodes should not be hubs
				LabelTagPair<String,String> heOut = new LabelTagPair<String,String>(link.from().label(), link.tag(), LabelTagPair.DIR_OUT);
				if (!edgeCounts.containsKey(heOut)) {
					edgeCounts.put(heOut, 0);
				}
				edgeCounts.put(heOut, edgeCounts.get(heOut)+1);	
			}
			if (!iNodes.contains(link.to())) {
				LabelTagPair<String,String> heIn = new LabelTagPair<String,String>(link.to().label(), link.tag(), LabelTagPair.DIR_IN);
				if (!edgeCounts.containsKey(heIn)) {
					edgeCounts.put(heIn, 0);
				}
				edgeCounts.put(heIn, edgeCounts.get(heIn)+1);
			}		
		}
		return edgeCounts;
	}



	/**
	 * Remove the hubs in the hubmap from the oldGraph and return a new graph with the hubs removed
	 * 
	 * @param oldGraph
	 * @param hubMap
	 * @return
	 */
	public static SingleDTGraph removeHubs(SingleDTGraph oldGraph, Map<LabelTagPair<String,String>, Integer> hubMap) {
		DTGraph<String,String> graph = new LightDTGraph<String,String>();
		List<DTNode<String,String>> newInstanceNodes = new ArrayList<DTNode<String,String>>();
		Set<DTLink<String,String>> toRemoveLinks = new HashSet<DTLink<String,String>>();

		Map<DTNode<String,String>,Integer> iNodeMap = new HashMap<DTNode<String,String>,Integer>();	
		for (int i = 0; i < oldGraph.getInstances().size(); i++) {
			iNodeMap.put(oldGraph.getInstances().get(i), i);
			newInstanceNodes.add(null);
		}	

		for (DTNode<String,String> node : oldGraph.getGraph().nodes()) {
			String newLabel = null;
			int lowestDepth = 0; //hubMap.size();
			for (DTLink<String,String> inLink : node.linksIn()) {
				LabelTagPair<String,String> rel = new LabelTagPair<String,String>(inLink.from().label(), inLink.tag(), LabelTagPair.DIR_OUT);
				if (hubMap.containsKey(rel) && hubMap.get(rel) >= lowestDepth) {
					newLabel = inLink.from().label() + inLink.tag();
					lowestDepth = hubMap.get(rel);
				}
				if (hubMap.containsKey(rel)) {
					toRemoveLinks.add(inLink);
				}
				
			}
			for (DTLink<String,String> outLink : node.linksOut()) {
				LabelTagPair<String,String> rel = new LabelTagPair<String,String>(outLink.to().label(), outLink.tag(), LabelTagPair.DIR_IN);
				if (hubMap.containsKey(rel) && hubMap.get(rel) >= lowestDepth) {
					newLabel = outLink.tag() + outLink.to().label();;
					lowestDepth = hubMap.get(rel);
				}
				if (hubMap.containsKey(rel)) {
					toRemoveLinks.add(outLink);
				}
			}
			if (newLabel == null) {
				newLabel = node.label();
			}
			DTNode<String,String> newN = graph.add(newLabel);
			if (iNodeMap.containsKey(node)) { // We also need to replace the instance nodes with new instance nodes in the simplified graph
				newInstanceNodes.set(iNodeMap.get(node), newN);
			}
		}

		for(DTLink<String,String> link : oldGraph.getGraph().links()) {
			int a = link.from().index();
			int b = link.to().index();

			if (!toRemoveLinks.contains(link)) {
				graph.nodes().get(a).connect(graph.nodes().get(b), link.tag());
			}
		}
		return new SingleDTGraph(graph, newInstanceNodes);
	}

}
