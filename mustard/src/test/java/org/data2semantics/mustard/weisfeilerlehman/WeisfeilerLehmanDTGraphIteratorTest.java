package org.data2semantics.mustard.weisfeilerlehman;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;

public class WeisfeilerLehmanDTGraphIteratorTest {

	@Test
	public void test() {
		DTGraph<StringLabel,StringLabel> graph = new LightDTGraph<StringLabel,StringLabel>();

		DTNode<StringLabel,StringLabel> n1 = graph.add(new StringLabel("n1"));
		DTNode<StringLabel,StringLabel> n2 = graph.add(new StringLabel("n1"));
		DTNode<StringLabel,StringLabel> n3 = graph.add(new StringLabel("n1"));
		DTNode<StringLabel,StringLabel> n4 = graph.add(new StringLabel("n1"));
		DTNode<StringLabel,StringLabel> n5 = graph.add(new StringLabel("n1"));
		DTNode<StringLabel,StringLabel> n6 = graph.add(new StringLabel("n2"));
		DTNode<StringLabel,StringLabel> n7 = graph.add(new StringLabel("n3"));

		n1.connect(n3, new StringLabel("ea"));
		n2.connect(n4, new StringLabel("ea"));
		n3.connect(n2, new StringLabel("ea"));
		n3.connect(n4, new StringLabel("ea"));
		n4.connect(n5, new StringLabel("ea"));
		n5.connect(n6, new StringLabel("eb"));
		n5.connect(n7, new StringLabel("ec"));



		List<DTGraph<StringLabel,StringLabel>> gl = new ArrayList<DTGraph<StringLabel,StringLabel>>();
		gl.add(graph);

		WeisfeilerLehmanIterator<DTGraph<StringLabel,StringLabel>> wl = new WeisfeilerLehmanDTGraphIterator(true, true);

		System.out.println("Regular --> before");
		System.out.println(graph);


		wl.wlInitialize(gl);

		System.out.println("init");
		System.out.println(graph);

		for (int i = 0; i < 6; i++) {
			wl.wlIterate(gl);
			System.out.println("iteration " + i);
			System.out.println(graph);

		}
		
		DTGraph<ApproxStringLabel,ApproxStringLabel> graph2 = new LightDTGraph<ApproxStringLabel,ApproxStringLabel>();

		DTNode<ApproxStringLabel,ApproxStringLabel> n12 = graph2.add(new ApproxStringLabel("n1"));
		DTNode<ApproxStringLabel,ApproxStringLabel> n22 = graph2.add(new ApproxStringLabel("n1"));
		DTNode<ApproxStringLabel,ApproxStringLabel> n32 = graph2.add(new ApproxStringLabel("n1"));
		DTNode<ApproxStringLabel,ApproxStringLabel> n42 = graph2.add(new ApproxStringLabel("n1"));
		DTNode<ApproxStringLabel,ApproxStringLabel> n52 = graph2.add(new ApproxStringLabel("n1"));
		DTNode<ApproxStringLabel,ApproxStringLabel> n62 = graph2.add(new ApproxStringLabel("n2"));
		DTNode<ApproxStringLabel,ApproxStringLabel> n72 = graph2.add(new ApproxStringLabel("n3"));

		n12.connect(n32, new ApproxStringLabel("ea"));
		n22.connect(n42, new ApproxStringLabel("ea"));
		n32.connect(n22, new ApproxStringLabel("ea"));
		n32.connect(n42, new ApproxStringLabel("ea"));
		n42.connect(n52, new ApproxStringLabel("ea"));
		n52.connect(n62, new ApproxStringLabel("eb"));
		n52.connect(n72, new ApproxStringLabel("ec"));



		List<DTGraph<ApproxStringLabel,ApproxStringLabel>> gl2 = new ArrayList<DTGraph<ApproxStringLabel,ApproxStringLabel>>();
		gl2.add(graph2);

		WeisfeilerLehmanApproxIterator<DTGraph<ApproxStringLabel,ApproxStringLabel>, String> wla = new WeisfeilerLehmanApproxDTGraphIterator(true, 1, 10, 1);

		System.out.println("Approx --> before");
		System.out.println(graph2);


		wla.wlInitialize(gl2);
		
		Map<String, Integer> m = labelFreqs(gl2);


		
		System.out.println("init");
		System.out.println(graph2);

		for (int i = 0; i < 6; i++) {
			wla.wlIterate(gl2,m);
			m = labelFreqs(gl2);
			// n3 -> 2 -> "", we manipulate that freq.
			m.put("2", 0);
			m.put("", 0);
			
			System.out.println("iteration " + i);
			System.out.println(graph2);

		}
		
		System.out.println(wla.getLabelDict());
	}
	
	private Map<String, Integer> labelFreqs(List<DTGraph<ApproxStringLabel,ApproxStringLabel>> gs) {
		Map<String, Integer> m = new HashMap<String, Integer>();
		
		for (int i = 0; i < gs.size(); i++) {
			Set<String> seen = new HashSet<String>(); // to track seen label for this instance

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (DTNode<ApproxStringLabel,ApproxStringLabel> vertex : gs.get(i).nodes()) {
				String lab = vertex.label().toString();
				if (!m.containsKey(lab)) {
					m.put(lab, 0);
				} 
				if (!seen.contains(lab)) {
					m.put(lab, m.get(lab) + 1);
					seen.add(lab);
				}
			}

			for (DTLink<ApproxStringLabel,ApproxStringLabel> edge : gs.get(i).links()) {
				String lab = edge.tag().toString();
				// Count
				if (!m.containsKey(lab)) {
					m.put(lab, 0);
				} 
				if (!seen.contains(lab)) {
					m.put(lab, m.get(lab) + 1);
					seen.add(lab);
				}
			}
		}
		return m;
	}

}
