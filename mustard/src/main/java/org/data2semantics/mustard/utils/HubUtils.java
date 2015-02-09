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
import org.data2semantics.mustard.rdf.RDFUtils;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;
import org.nodes.algorithms.SlashBurn;
import org.nodes.util.MaxObserver;
import org.nodes.util.Functions.Dir;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class HubUtils {


	private static class LabelTagPairComparator<L,T> implements Comparator<LabelTagPair<L,T>> {
		private Map<LabelTagPair<L,T>, Integer> counts;

		public LabelTagPairComparator(Map<LabelTagPair<L, T>, Integer> counts) {
			super();
			this.counts = counts;
		}

		public int compare(LabelTagPair<L, T> o1, LabelTagPair<L, T> o2) {
			return counts.get(o1) - counts.get(o2);
		}
	}

	private static final class ValueComparator<V extends Comparable<? super V>>
	implements Comparator<Map.Entry<?, V>> {
		public int compare(Map.Entry<?, V> o1, Map.Entry<?, V> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	}
	
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

	public static List<Map.Entry<LabelTagPair<String,String>, Integer>> sortHubMap(Map<LabelTagPair<String,String>, Integer> hubEdges) {
		List<Map.Entry<LabelTagPair<String,String>, Integer>> list = new ArrayList<Map.Entry<LabelTagPair<String,String>, Integer>>(hubEdges.size());
		list.addAll(hubEdges.entrySet());
		ValueComparator<Integer> cmp = new ValueComparator<Integer>();
		Collections.sort(list, cmp);
		Collections.reverse(list);
		return list;
	}

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


	public static Map<LabelTagPair<String,String>, Integer> countLabelTagPairs(SingleDTGraph graph) {
		Map<LabelTagPair<String,String>, Integer> edgeCounts = new HashMap<LabelTagPair<String,String>, Integer>();
		Set<DTNode<String,String>> iNodes = new HashSet<DTNode<String,String>>(graph.getInstances());

		for (DTLink<String,String> link : graph.getGraph().links()) {
			if (!iNodes.contains(link.from())) { // instance nodes should not be hubs
				LabelTagPair<String,String> heOut = new LabelTagPair<String,String>(link.from().label(), link.tag(), Dir.OUT);
				if (!edgeCounts.containsKey(heOut)) {
					edgeCounts.put(heOut, 0);
				}
				edgeCounts.put(heOut, edgeCounts.get(heOut)+1);	
			}
			if (!iNodes.contains(link.to())) {
				LabelTagPair<String,String> heIn = new LabelTagPair<String,String>(link.to().label(), link.tag(), Dir.IN);
				if (!edgeCounts.containsKey(heIn)) {
					edgeCounts.put(heIn, 0);
				}
				edgeCounts.put(heIn, edgeCounts.get(heIn)+1);
			}		
		}
		return edgeCounts;
	}


	public static List<LabelTagPair<String,String>> findLabelTagPairs(SingleDTGraph graph, int maxHubs) {
		Map<LabelTagPair<String,String>, Integer> edgeCounts = countLabelTagPairs(graph);
		Comparator<LabelTagPair<String,String>> comp = new LabelTagPairComparator<String,String>(edgeCounts);
		MaxObserver<LabelTagPair<String,String>> obs = new MaxObserver<LabelTagPair<String,String>>(maxHubs, comp);
		obs.observe(edgeCounts.keySet());

		return new ArrayList<LabelTagPair<String,String>>(obs.elements());
	}

	public static Map<LabelTagPair<String,String>, Integer> createHubMapFromLabelTagPairs(List<LabelTagPair<String,String>> hubEdges, int maxHubs) {
		Map<LabelTagPair<String,String>,Integer> hubMap = new HashMap<LabelTagPair<String,String>,Integer>();		
		for (int i = 0; i < hubEdges.size() && i < maxHubs; i++) {
			hubMap.put(hubEdges.get(i), i);
		}
		return hubMap;
	}

	public static List<DTNode<String,String>> findSigDegreeHubs(Set<Statement> stmts, List<Resource> instances, int maxHubs) {
		SingleDTGraph g = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, instances, false);
		return findSigDegreeHubs(g, maxHubs);
	}

	public static List<DTNode<String,String>> findSigDegreeHubs(SingleDTGraph graph, int maxHubs) {
		Comparator<DTNode<String,String>> compSigDeg = new SlashBurn.SignatureComparator<String,String>();
		MaxObserver<DTNode<String,String>> obsSigDeg = new MaxObserver<DTNode<String,String>>(maxHubs + graph.numInstances(), compSigDeg);				
		obsSigDeg.observe(graph.getGraph().nodes());
		List<DTNode<String,String>> sigDegreeHubs = new ArrayList<DTNode<String,String>>(obsSigDeg.elements());

		// Remove hubs from list that are root nodes
		sigDegreeHubs.removeAll(graph.getInstances());
		// cut to maxHubs size
		sigDegreeHubs = sigDegreeHubs.subList(0, Math.min(sigDegreeHubs.size(), maxHubs));
		// reverse
		//Collections.reverse(sigDegreeHubs);

		return sigDegreeHubs;
	}

	public static Map<LabelTagPair<String,String>, Integer> createHubMap(List<DTNode<String,String>> hubs, int maxHubs) {
		Map<LabelTagPair<String,String>, Integer> hubMap = new HashMap<LabelTagPair<String,String>, Integer>();		
		for (int i = 0; i < hubs.size() && i < maxHubs; i++) {
			org.nodes.util.Pair<Dir,String> sig = SlashBurn.primeSignature(hubs.get(i));

			hubMap.put(new LabelTagPair<String,String>(hubs.get(i).label(), sig.second(), sig.first()), i);	
		}
		return hubMap;
	}

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
			DTLink<String,String> remLink = null;
			for (DTLink<String,String> inLink : node.linksIn()) {
				LabelTagPair<String,String> rel = new LabelTagPair<String,String>(inLink.from().label(), inLink.tag(), Dir.OUT);
				if (hubMap.containsKey(rel) && hubMap.get(rel) >= lowestDepth) {
					newLabel = inLink.from().label() + inLink.tag();
					lowestDepth = hubMap.get(rel);
					remLink = inLink;
				}
				///*
				if (hubMap.containsKey(rel)) {
					toRemoveLinks.add(inLink);
				}
				//*/

			}
			for (DTLink<String,String> outLink : node.linksOut()) {
				LabelTagPair<String,String> rel = new LabelTagPair<String,String>(outLink.to().label(), outLink.tag(), Dir.IN);
				if (hubMap.containsKey(rel) && hubMap.get(rel) >= lowestDepth) {
					newLabel = outLink.tag() + outLink.to().label();;
					lowestDepth = hubMap.get(rel);
					remLink = outLink;
				}
				///*
				if (hubMap.containsKey(rel)) {
					toRemoveLinks.add(outLink);
				}
				//*/
			}
			if (newLabel == null) {
				newLabel = node.label();
			}
			DTNode<String,String> newN = graph.add(newLabel);
			if (iNodeMap.containsKey(node)) { // We also need to replace the instance nodes with new instance nodes in the simplified graph
				newInstanceNodes.set(iNodeMap.get(node), newN);
			}

			/*
			if (remLink != null ) {
				toRemoveLinks.add(remLink);
			}
			//*/
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
