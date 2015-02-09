package org.data2semantics.mustard.rdf;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class RDFUtilsTest {
	private static String AIFB_FILE = "../mustard-experiments/datasets/aifb-fixed_complete.n3";

	
	@Test
	public void test() {
		
		RDFDataSet tripleStore = new RDFFileDataSet(AIFB_FILE, RDFFormat.N3);
		
		
		DTGraph<String,String> graph = RDFUtils.statements2Graph(new HashSet<Statement>(tripleStore.getFullGraph()), RDFUtils.REGULAR_SPLIT_LITERALS);
		
		for (DTNode<String,String> n : graph.nodes()) {
			System.out.println(n);
		}
	}

}
