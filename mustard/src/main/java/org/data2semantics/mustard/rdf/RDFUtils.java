package org.data2semantics.mustard.rdf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.MapDTGraph;
import org.nodes.algorithms.SlashBurn;
import org.nodes.util.MaxObserver;
import org.nodes.util.Functions.Dir;
import org.nodes.util.Pair;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RDFUtils {
	public static final int NO_LITERALS = 1;
	public static final int REPEAT_LITERALS = 2;
	public static final int REGULAR_LITERALS = 3;

	public static List<DTGraph<String,String>> getSubGraphs(DTGraph<String,String> graph, List<DTNode<String,String>> instances, int depth) {
		List<DTGraph<String,String>> subGraphs = new ArrayList<DTGraph<String,String>>();
		Map<DTNode<String,String>,DTNode<String,String>> nodeMap;
		Map<DTLink<String,String>,DTLink<String,String>> linkMap;
		List<DTNode<String,String>> searchNodes, newSearchNodes;

		for (DTNode<String,String> startNode : instances) {
			DTGraph<String,String> newGraph = new MapDTGraph<String,String>();
			searchNodes = new ArrayList<DTNode<String,String>>();
			searchNodes.add(startNode);
			nodeMap = new HashMap<DTNode<String,String>,DTNode<String,String>>();
			linkMap = new HashMap<DTLink<String,String>,DTLink<String,String>>();
			for (int i = 0; i < depth; i++) {
				newSearchNodes = new ArrayList<DTNode<String,String>>();
				for (DTNode<String,String> node : searchNodes) {
					for (DTLink<String,String> link : node.linksOut()) {
						if (!nodeMap.containsKey(link.from())) {
							nodeMap.put(link.from(), newGraph.add(link.from().label()));
						}
						if (!nodeMap.containsKey(link.to())) {
							nodeMap.put(link.to(), newGraph.add(link.to().label()));
							newSearchNodes.add(link.to());
						}
						if (!linkMap.containsKey(link)) {
							linkMap.put(link, nodeMap.get(link.from()).connect(nodeMap.get(link.to()), link.tag()));
						}
					}
				}
				searchNodes = newSearchNodes;
			}
			subGraphs.add(newGraph);
		}
		return subGraphs;
	}

	/**
	 * Return subtrees instead of graphs, i.e. the same nodes get repeated into a tree, so no cycles. 
	 * The first node in the graph is the root, i.e. nodes().get(0) should be the root node. 
	 * 
	 */
	public static List<DTGraph<String,String>> getSubTrees(DTGraph<String,String> graph, List<DTNode<String,String>> instances, int depth) {
		List<DTGraph<String,String>> subTrees = new ArrayList<DTGraph<String,String>>();
		List<Pair<DTNode<String,String>,DTNode<String,String>>> searchNodes, newSearchNodes;

		for (DTNode<String,String> startNode : instances) {
			DTGraph<String,String> newGraph = new MapDTGraph<String,String>();
			searchNodes = new ArrayList<Pair<DTNode<String,String>,DTNode<String,String>>>();

			// root gets index 0
			searchNodes.add(new Pair<DTNode<String,String>,DTNode<String,String>>(startNode, newGraph.add(startNode.label())));

			for (int i = 0; i < depth; i++) {
				newSearchNodes = new ArrayList<Pair<DTNode<String,String>,DTNode<String,String>>>();
				for (Pair<DTNode<String,String>,DTNode<String,String>> nodePair : searchNodes) {				
					for (DTLink<String,String> link : nodePair.first().linksOut()) {
						DTNode<String,String> n2 = newGraph.add(link.to().label());
						newSearchNodes.add(new Pair<DTNode<String,String>,DTNode<String,String>>(link.to(),n2));					
						nodePair.second().connect(n2, link.tag());
					}
				}
				searchNodes = newSearchNodes;
			}
			subTrees.add(newGraph);
		}
		return subTrees;
	}



	public static DTGraph<String,String> simplifyInstanceNodeLabels(DTGraph<String,String> oldGraph, List<DTNode<String,String>> instanceNodes) {
		String rootLabel = KernelUtils.ROOTID;
		Map<DTNode<String,String>, Integer> ns = new HashMap<DTNode<String,String>,Integer>();
		DTGraph<String,String> graph = new MapDTGraph<String,String>();

		for (int i = 0; i < instanceNodes.size(); i++) {
			ns.put(instanceNodes.get(i), i);
		}

		for (DTNode<String,String> n : oldGraph.nodes()) {
			if (ns.containsKey(n)) {
				instanceNodes.set(ns.get(n), graph.add(rootLabel));
			} else {
				graph.add(n.label());
			}
		}
		for (DTLink<String,String> l : oldGraph.links()) {
			graph.nodes().get(l.from().index()).connect(graph.nodes().get(l.to().index()), l.tag());
		}
		return graph;
	}


	public static List<DTNode<String,String>> findSigDegreeHubs(Set<Statement> stmts, List<Resource> instances, int maxHubs) {
		DTGraph<String,String> graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS);

		Comparator<DTNode<String,String>> compSigDeg = new SlashBurn.SignatureComparator<String,String>();
		MaxObserver<DTNode<String,String>> obsSigDeg = new MaxObserver<DTNode<String,String>>(maxHubs + instances.size(), compSigDeg);				
		obsSigDeg.observe(graph.nodes());
		List<DTNode<String,String>> sigDegreeHubs = new ArrayList<DTNode<String,String>>(obsSigDeg.elements());

		// Remove hubs from list that are root nodes
		List<DTNode<String,String>> rn = new ArrayList<DTNode<String,String>>();
		Set<String> is = new HashSet<String>();
		for (Resource r : instances) {
			is.add(r.toString());
		}
		for (DTNode<String,String> n : graph.nodes()) {
			if (is.contains(n.label())) {
				rn.add(n);
			}
		}
		sigDegreeHubs.removeAll(rn);
		return sigDegreeHubs;
	}

	public static Map<String, Integer> createHubMap(List<DTNode<String,String>> hubs, int maxHubs) {
		Map<String,Integer> hubMap = new HashMap<String,Integer>();		
		for (int i = 0; i < hubs.size() && i < maxHubs; i++) {
			org.nodes.util.Pair<Dir,String> sig = SlashBurn.primeSignature(hubs.get(i));

			if (sig.first() == Dir.IN) {
				hubMap.put(sig.second() + hubs.get(i).label(), i);	
			} else {
				hubMap.put(hubs.get(i).label() + sig.second(), i);
			}	
		}
		return hubMap;
	}

	public static DTGraph<String,String> removeHubs(DTGraph<String,String> oldGraph, List<DTNode<String,String>> instanceNodes, Map<String, Integer> hubMap) {
		DTGraph<String,String> graph = new MapDTGraph<String,String>();
		Set<DTLink<String,String>> toRemoveLinks = new HashSet<DTLink<String,String>>();

		Map<DTNode<String,String>,Integer> iNodeMap = new HashMap<DTNode<String,String>,Integer>();	
		for (int i = 0; i < instanceNodes.size(); i++) {
			iNodeMap.put(instanceNodes.get(i), i);
		}	

		for (DTNode<String,String> node : oldGraph.nodes()) {
			String newLabel = null;
			int lowestDepth = 0;
			DTLink<String,String> remLink = null;
			for (DTLink<String,String> inLink : node.linksIn()) {
				String rel = inLink.from().label() + inLink.tag();
				if (hubMap.containsKey(rel) && hubMap.get(rel) >= lowestDepth) {
					newLabel = rel;
					lowestDepth = hubMap.get(rel);
					remLink = inLink;
				}
			}
			for (DTLink<String,String> outLink : node.linksOut()) {
				String rel = outLink.tag() + outLink.to().label();
				if (hubMap.containsKey(rel) && hubMap.get(rel) >= lowestDepth) {
					newLabel = rel;
					lowestDepth = hubMap.get(rel);
					remLink = outLink;
				}
			}
			if (newLabel == null) {
				newLabel = node.label();
			}
			DTNode<String,String> newN = graph.add(newLabel);
			if (iNodeMap.containsKey(node)) { // We also need to replace the instance nodes with new instance nodes in the simplified graph
				instanceNodes.set(iNodeMap.get(node), newN);
			}

			if (remLink != null ) {
				toRemoveLinks.add(remLink);
			}
		}

		for(DTLink<String,String> link : oldGraph.links()) {
			int a = link.from().index();
			int b = link.to().index();

			if (!toRemoveLinks.contains(link)) {
				graph.nodes().get(a).connect(graph.nodes().get(b), link.tag());
			}
		}
		return graph;
	}


	/**
	 * find the instance nodes in a graph based on the list of instance Resource's
	 * 
	 * 
	 * @param graph
	 * @param instances
	 * @return
	 */
	public static List<DTNode<String,String>> findInstances(DTGraph<String,String> graph, List<Resource> instances) {
		List<DTNode<String,String>> iNodes = new ArrayList<DTNode<String,String>>();
		for (Resource inst: instances) {
			iNodes.add(graph.node(inst.toString()));
		}
		return iNodes;
	}

	/**
	 * Convert a set of RDF statements into a DTGraph and set the list of instances to a identical label if boolean is set
	 * The nodes for these instances are put in instanceNodes
	 * 
	 * @param stmts
	 * @param literalOption
	 * @param instances
	 * @return
	 */
	public static DTGraph<String,String> statements2Graph(Set<Statement> stmts, int literalOption, List<Resource> instances, List<DTNode<String,String>> instanceNodes, boolean simplifyInstanceNodes) {
		DTGraph<String,String> graph = new MapDTGraph<String,String>();	
		Map<Resource, DTNode<String,String>> iMap = new HashMap<Resource, DTNode<String,String>>();

		for (Resource instance : instances) {
			if (simplifyInstanceNodes) {
				iMap.put(instance, graph.add(KernelUtils.ROOTID));
			} else {
				iMap.put(instance, graph.add(instance.toString()));
			}
			instanceNodes.add(iMap.get(instance));
		}	

		for (Statement s : stmts) {
			if (s.getObject() instanceof Literal && literalOption != NO_LITERALS) {
				if (literalOption == REGULAR_LITERALS) {
					addStatement(graph, s, false, iMap);
				}
				if (literalOption == REPEAT_LITERALS) {
					addStatement(graph, s, true, iMap);
				}
			} else if (!(s.getObject() instanceof Literal)){
				addStatement(graph, s, false, iMap);
			}
		}	
		return graph;
	}

	private static void addStatement(DTGraph<String,String> graph, Statement stmt, boolean newObject, Map<Resource, DTNode<String,String>> iMap) {

		DTNode<String,String> n1 = iMap.get(stmt.getSubject());
		if (n1 == null) {
			n1 = graph.node(stmt.getSubject().toString());
			if (n1 == null) {
				n1 = graph.add(stmt.getSubject().toString());
			}
		}

		DTNode<String, String> n2 = null;
		if (stmt.getObject() instanceof Resource) {
			n2 = iMap.get((Resource) stmt.getObject());
		}
		if (n2 == null || newObject) {
			n2 = graph.node(stmt.getObject().toString());
			if (n2 == null || newObject) {
				n2 = graph.add(stmt.getObject().toString());
			}			
		}

		// Statements are unique, since they are in a Set, thus we have never seem this particular edge before, we know that.
		n1.connect(n2, stmt.getPredicate().toString());
	}


	/**
	 * Convert a set of RDF statements into a DTGraph. 
	 * There are three possible ways to treat literals, as regular nodes (REGULAR_LITERALS), as unique nodes (i.e. one for each literal even if they are equal) (REPEAT_LITERALS),
	 * or ignore them (NO_LITERALS)
	 * 
	 * @param stmts
	 * @param literalOption
	 * @return
	 */
	public static DTGraph<String,String> statements2Graph(Set<Statement> stmts, int literalOption) {
		DTGraph<String,String> graph = new MapDTGraph<String,String>();

		for (Statement s : stmts) {
			if (s.getObject() instanceof Literal && literalOption != NO_LITERALS) {
				if (literalOption == REGULAR_LITERALS) {
					addStatement(graph, s, false);
				}
				if (literalOption == REPEAT_LITERALS) {
					addStatement(graph, s, true);
				}
			} else if (!(s.getObject() instanceof Literal)){
				addStatement(graph, s, false);
			}
		}	
		return graph;
	}

	private static void addStatement(DTGraph<String,String> graph, Statement stmt, boolean newObject) {
		DTNode<String, String> n1 = graph.node(stmt.getSubject().toString());
		if (n1 == null) {
			n1 = graph.add(stmt.getSubject().toString());
		}
		DTNode<String, String> n2 = graph.node(stmt.getObject().toString());
		if (n2 == null || newObject) {
			n2 = graph.add(stmt.getObject().toString());
		}
		// Statements are unique, since they are in a Set, thus we have never seem this particular edge before, we know that.
		n1.connect(n2, stmt.getPredicate().toString());
	}

	/**
	 * Create a set of statements for a list of instances nodes. For each instance node, the statements upto the specified depth are extracted and put into one set of statements
	 * 
	 * @param ts
	 * @param instances
	 * @param depth
	 * @param inference
	 * @return
	 */
	public static Set<Statement> getStatements4Depth(RDFDataSet ts, List<Resource> instances, int depth, boolean inference) {
		Set<Statement> stmts = new HashSet<Statement>();
		List<Resource> searchFront = new ArrayList<Resource>(instances);
		List<Resource> newSearchFront;

		for (int i = 0; i < depth; i++) {
			newSearchFront = new ArrayList<Resource>();
			for (Resource r : searchFront) {
				List<Statement> res = ts.getStatements(r, null, null, inference);
				stmts.addAll(res);
				for (Statement stmt : res) {
					if (stmt.getObject() instanceof Resource) {
						newSearchFront.add((Resource) stmt.getObject()); 
					}
				}
			}
			searchFront = newSearchFront;
		}
		return stmts;
	}

}
