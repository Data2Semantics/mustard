package org.data2semantics.mustard.rdf;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.weisfeilerlehman.ApproxStringLabel;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;
import org.nodes.util.Pair;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class RDFUtils {
	// TODO replace these with an Enum
	public static final int NO_LITERALS = 1;
	public static final int REPEAT_LITERALS = 2;
	public static final int REGULAR_LITERALS = 3;
	public static final int REPEAT_SPLIT_LITERALS = 4;
	public static final int REGULAR_SPLIT_LITERALS = 5;


	public static GraphList<DTGraph<ApproxStringLabel,ApproxStringLabel>> getSubGraphsApproxStringLabel(DTGraph<String,String> graph, List<DTNode<String,String>> instances, int depth) {
		List<DTGraph<ApproxStringLabel,ApproxStringLabel>> subGraphs = new ArrayList<DTGraph<ApproxStringLabel,ApproxStringLabel>>();
		Map<DTNode<String,String>,DTNode<ApproxStringLabel,ApproxStringLabel>> nodeMap;
		Map<DTLink<String,String>,DTLink<ApproxStringLabel,ApproxStringLabel>> linkMap;
		List<DTNode<String,String>> searchNodes, newSearchNodes;

		for (DTNode<String,String> startNode : instances) {
			DTGraph<ApproxStringLabel,ApproxStringLabel> newGraph = new LightDTGraph<ApproxStringLabel,ApproxStringLabel>();
			searchNodes = new ArrayList<DTNode<String,String>>();
			searchNodes.add(startNode);
			nodeMap = new HashMap<DTNode<String,String>,DTNode<ApproxStringLabel,ApproxStringLabel>>();
			linkMap = new HashMap<DTLink<String,String>,DTLink<ApproxStringLabel,ApproxStringLabel>>();
			for (int i = 0; i < depth; i++) {
				newSearchNodes = new ArrayList<DTNode<String,String>>();
				for (DTNode<String,String> node : searchNodes) {
					for (DTLink<String,String> link : node.linksOut()) {
						if (!nodeMap.containsKey(link.from())) {
							nodeMap.put(link.from(), newGraph.add(new ApproxStringLabel(link.from().label(), depth - i)));
						}
						if (!nodeMap.containsKey(link.to())) {
							nodeMap.put(link.to(), newGraph.add(new ApproxStringLabel(link.to().label(), depth - (i+1))));
							newSearchNodes.add(link.to());
						}
						if (!linkMap.containsKey(link)) {
							linkMap.put(link, nodeMap.get(link.from()).connect(nodeMap.get(link.to()), new ApproxStringLabel(link.tag(),depth - (i+1))));
						}
					}
				}
				searchNodes = newSearchNodes;
			}
			subGraphs.add(newGraph);
		}
		return new GraphList<DTGraph<ApproxStringLabel,ApproxStringLabel>>(subGraphs);
	}
	

	public static GraphList<DTGraph<String,String>> getSubGraphs(DTGraph<String,String> graph, List<DTNode<String,String>> instances, int depth) {
		List<DTGraph<String,String>> subGraphs = new ArrayList<DTGraph<String,String>>();
		Map<DTNode<String,String>,DTNode<String,String>> nodeMap;
		Map<DTLink<String,String>,DTLink<String,String>> linkMap;
		List<DTNode<String,String>> searchNodes, newSearchNodes;

		for (DTNode<String,String> startNode : instances) {
			DTGraph<String,String> newGraph = new LightDTGraph<String,String>();
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
		return new GraphList<DTGraph<String,String>>(subGraphs);
	}
	
	
	public static GraphList<DTGraph<ApproxStringLabel,ApproxStringLabel>> getSubTreesApproxStringLabel(DTGraph<String,String> graph, List<DTNode<String,String>> instances, int depth) {
		List<DTGraph<ApproxStringLabel,ApproxStringLabel>> subTrees = new ArrayList<DTGraph<ApproxStringLabel,ApproxStringLabel>>();
		List<Pair<DTNode<String,String>,DTNode<ApproxStringLabel,ApproxStringLabel>>> searchNodes, newSearchNodes;

		for (DTNode<String,String> startNode : instances) {
			DTGraph<ApproxStringLabel,ApproxStringLabel> newGraph = new LightDTGraph<ApproxStringLabel,ApproxStringLabel>();
			searchNodes = new ArrayList<Pair<DTNode<String,String>,DTNode<ApproxStringLabel,ApproxStringLabel>>>();

			// root gets index 0
			searchNodes.add(new Pair<DTNode<String,String>,DTNode<ApproxStringLabel,ApproxStringLabel>>(startNode, newGraph.add(new ApproxStringLabel(startNode.label(), depth))));

			for (int i = 0; i < depth; i++) {
				newSearchNodes = new ArrayList<Pair<DTNode<String,String>,DTNode<ApproxStringLabel,ApproxStringLabel>>>();
				for (Pair<DTNode<String,String>,DTNode<ApproxStringLabel,ApproxStringLabel>> nodePair : searchNodes) {				
					for (DTLink<String,String> link : nodePair.first().linksOut()) {
						DTNode<ApproxStringLabel,ApproxStringLabel> n2 = newGraph.add(new ApproxStringLabel(link.to().label(), depth - (i+1)));
						newSearchNodes.add(new Pair<DTNode<String,String>,DTNode<ApproxStringLabel,ApproxStringLabel>>(link.to(),n2));					
						nodePair.second().connect(n2, new ApproxStringLabel(link.tag(),depth - (i+1)));
					}
				}
				searchNodes = newSearchNodes;
			}
			subTrees.add(newGraph);
		}
		return new GraphList<DTGraph<ApproxStringLabel,ApproxStringLabel>>(subTrees);
	}

	/**
	 * Return subtrees instead of graphs, i.e. the same nodes get repeated into a tree, so no cycles. 
	 * The first node in the graph is the root, i.e. nodes().get(0) should be the root node. 
	 * 
	 */
	public static GraphList<DTGraph<String,String>> getSubTrees(DTGraph<String,String> graph, List<DTNode<String,String>> instances, int depth) {
		List<DTGraph<String,String>> subTrees = new ArrayList<DTGraph<String,String>>();
		List<Pair<DTNode<String,String>,DTNode<String,String>>> searchNodes, newSearchNodes;

		for (DTNode<String,String> startNode : instances) {
			DTGraph<String,String> newGraph = new LightDTGraph<String,String>();
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
		return new GraphList<DTGraph<String,String>>(subTrees);
	}

	public static SingleDTGraph blankLabels(SingleDTGraph graph) {
		Map<DTNode<String,String>, Integer> ns = new HashMap<DTNode<String,String>,Integer>();
		DTGraph<String,String> newGraph = new LightDTGraph<String,String>();
		List<DTNode<String,String>> newIN = new ArrayList<DTNode<String,String>>();

		for (int i = 0; i < graph.getInstances().size(); i++) {
			ns.put(graph.getInstances().get(i), i);
			newIN.add(null);
		}

		for (DTNode<String,String> n : graph.getGraph().nodes()) {
			if (ns.containsKey(n)) {
				newIN.set(ns.get(n), newGraph.add(""));
			} else {
				newGraph.add("");
			}
		}
		for (DTLink<String,String> l : graph.getGraph().links()) {
			newGraph.nodes().get(l.from().index()).connect(newGraph.nodes().get(l.to().index()), "");
		}
		return new SingleDTGraph(newGraph, newIN);
	}


	public static DTGraph<String,String> simplifyInstanceNodeLabels(DTGraph<String,String> oldGraph, List<DTNode<String,String>> instanceNodes) {
		String rootLabel = KernelUtils.ROOTID;
		Map<DTNode<String,String>, Integer> ns = new HashMap<DTNode<String,String>,Integer>();
		DTGraph<String,String> graph = new LightDTGraph<String,String>();

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
	 * Convert a set of RDF statements into a SingleDTGraph dataset object
	 *  
	 * @param stmts
	 * @param literalOption
	 * @param instances
	 * @return SingleDTGraph
	 */
	public static SingleDTGraph statements2Graph(Set<Statement> stmts, int literalOption, List<Resource> instances, boolean simplifyInstanceNodes) {
		List<DTNode<String,String>> instanceNodes = new ArrayList<DTNode<String,String>>();
		DTGraph<String,String> graph = new LightDTGraph<String,String>();	
		Map<String, DTNode<String,String>> nodeMap = new HashMap<String, DTNode<String,String>>();

		for (Resource instance : instances) {
			if (simplifyInstanceNodes) {
				nodeMap.put(instance.toString(), graph.add(KernelUtils.ROOTID));
			} else {
				nodeMap.put(instance.toString(), graph.add(instance.toString()));
			}
			instanceNodes.add(nodeMap.get(instance.toString()));
		}	

		for (Statement s : stmts) {
			if (s.getObject() instanceof Literal && literalOption != NO_LITERALS) {
				if (literalOption == REGULAR_LITERALS) {
					addStatement(graph, s, false, false, nodeMap);
				}
				if (literalOption == REGULAR_SPLIT_LITERALS) {
					addStatement(graph, s, false, true, nodeMap);
				}
				if (literalOption == REPEAT_LITERALS) {
					addStatement(graph, s, true, false, nodeMap);
				}
				if (literalOption == REPEAT_SPLIT_LITERALS) {
					addStatement(graph, s, true, true, nodeMap);
				}
			} else if (!(s.getObject() instanceof Literal)){
				addStatement(graph, s, false, false, nodeMap);
			}
		}	
		return new SingleDTGraph(graph, instanceNodes);
	}

	private static void addStatement(DTGraph<String,String> graph, Statement stmt, boolean newObject, boolean splitLiteral, Map<String, DTNode<String,String>> nodeMap) {

		DTNode<String,String> n1 = nodeMap.get(stmt.getSubject().toString());
		if (n1 == null) {
			n1 = graph.add(stmt.getSubject().toString());
			nodeMap.put(stmt.getSubject().toString(), n1);
		}

		DTNode<String, String> n2 = null;
		List<DTNode<String,String>> nodeList = new ArrayList<DTNode<String,String>>();

		if (stmt.getObject() instanceof Resource) {
			n2 = nodeMap.get(stmt.getObject().toString());
			if (n2 == null) {
				n2 = graph.add(stmt.getObject().toString());
				nodeMap.put(stmt.getObject().toString(), n2);
			}
			nodeList.add(n2);

		} else { // Literal
			if (splitLiteral) {
				ValueFactory factory = ValueFactoryImpl.getInstance();
				Literal orgLit = (Literal)stmt.getObject();
				WordIterator wi = new WordIterator(orgLit.getLabel());

				while (wi.hasNext()) {
					String word = wi.next();
					Literal lit;

					// Retain the original datatype/language tag
					if (orgLit.getDatatype() != null) {
						lit = factory.createLiteral(word, orgLit.getDatatype());
					} else if (orgLit.getLanguage() != null) {
						lit = factory.createLiteral(word, orgLit.getLanguage());
					} else {
						lit = factory.createLiteral(word);
					}

					n2 = nodeMap.get(lit.toString());

					if (n2 == null || newObject) {
						n2 = graph.add(lit.toString());
						nodeMap.put(lit.toString(), n2);
					}
					nodeList.add(n2);
				}
			} else {
				n2 = nodeMap.get(stmt.getObject().toString()); // toString() should be different from stringValue()
				if (n2 == null) {
					n2 = graph.add(stmt.getObject().toString());
					if (!newObject) {
						nodeMap.put(stmt.getObject().toString(), n2);
					}
				}
				nodeList.add(n2);
			}
		}
		for (DTNode<String,String> n : nodeList) {
			// Statements are unique, since they are in a Set, thus we have never seem this particular edge before, we know that.
			n1.connect(n, stmt.getPredicate().toString());
		}
	}


	/*
	 * TODO replace this iterator with a more fancy text processing library, to at least do
	 * - Stop word removal
	 * - Stemming
	 * - Better treatment of case, currently we change everything to lower case
	 */
	private static class WordIterator implements Iterator<String> {
		private String text;
		private BreakIterator wordIt;
		private int start, end;

		public WordIterator(String text) {
			this.text = text;
			this.wordIt = BreakIterator.getWordInstance();
			this.wordIt.setText(text);
			this.start = wordIt.first();
			this.end = wordIt.next();		    
		}

		public boolean hasNext() {
			while (end != BreakIterator.DONE) {
				String word = text.substring(start,end);
				if (Character.isLetterOrDigit(word.charAt(0))) { // if it is a word, break
					break;
				} else { // if not, scoot one over
					start = end;
					end = wordIt.next();
				}
			}
			return end != BreakIterator.DONE;
		}

		public String next() {
			String word = text.substring(start,end);
			start = end;
			end = wordIt.next();
			return word.toLowerCase();

		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

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
		DTGraph<String,String> graph = new LightDTGraph<String,String>();	
		Map<String, DTNode<String,String>> nodeMap = new HashMap<String, DTNode<String,String>>();
		
		for (Statement s : stmts) {
			if (s.getObject() instanceof Literal && literalOption != NO_LITERALS) {
				if (literalOption == REGULAR_LITERALS) {
					addStatement(graph, s, false, false, nodeMap);
				}
				if (literalOption == REGULAR_SPLIT_LITERALS) {
					addStatement(graph, s, false, true, nodeMap);
				}
				if (literalOption == REPEAT_LITERALS) {
					addStatement(graph, s, true, false, nodeMap);
				}
				if (literalOption == REPEAT_SPLIT_LITERALS) {
					addStatement(graph, s, true, true, nodeMap);
				}
			} else if (!(s.getObject() instanceof Literal)){
				addStatement(graph, s, false, false, nodeMap);
			}
		}	
		return graph;
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
