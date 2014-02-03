package org.data2semantics.mustard.rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.nodes.MapDTGraph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RDFUtils {
	public static final int NO_LITERALS = 1;
	public static final int REPEAT_LITERALS = 2;
	public static final int REGULAR_LITERALS = 3;

	
	public static List<DTNode<String,String>> findInstances(DTGraph<String,String> graph, List<Resource> instances) {
		List<DTNode<String,String>> iNodes = new ArrayList<DTNode<String,String>>();
		for (Resource inst: instances) {
			iNodes.add(graph.node(inst.toString()));
		}
		return iNodes;
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
