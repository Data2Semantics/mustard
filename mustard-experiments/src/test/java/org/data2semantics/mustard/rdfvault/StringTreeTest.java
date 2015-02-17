package org.data2semantics.mustard.rdfvault;

import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.rdfvault.StringTree.PrefixStatistics;
import org.junit.Test;


public class StringTreeTest {

	@Test public void test_tree() {
		String [] tst = { "blub", "blubblieb", "blu", "bla", "aap", "iets", "nog iets", "stringetje" };
		Object [] tickets = new Object[tst.length];
		StringTree T = new StringTree();

		for (int i=0; i<tst.length; i++) {
			String s = tst[i];
			tickets[i] = T.store(s);
		}

		for (int i=0; i<tst.length; i++) {
			String s = T.redeem(tickets[i]);
			assert s.equals(tst[i]): "Item '"+tst[i]+"' redeemed as '"+s+"'!";
		}

		for (String s : tst) {
			assert T.search(s)!=null: "Whoa! "+s+" is not recovered";
			assert T.search(s+"qqqq")==null: "Whoa! Extension of "+s+" is erroneously recovered";
			assert T.search(s.substring(0,s.length()-2))==null: "Whoa! Prefix of "+s+" is erroneously recovered";
		}

		Object test = T.search(tst[0]);

		for (String s : tst) {
			Object ticket = T.search(s);
			assert ticket!=null: "Huh? Key not in tree: "+s;
			T.trash(ticket);
			assert T.search(s)==null: "Trashing '"+s+"' seems to have failed!?";
		}

		boolean problem = true;
		try { T.trash(test); } catch (InvalidTicketException e) { problem = false; }
		assert !problem : "The vault did not complain when I trashed a ticket twice!";
	}
	
	@Test public void prefix_statistics_test() {
		String [] tst = { "blub", "blubblieb", "blu", "bla", "aap", "iets", "nog iets", "stringetje" };
		StringTree T = new StringTree();
		
		for (int i=0; i<tst.length; i++) {
			String s = tst[i];
			T.store(s);
		}
		
		PrefixStatistics ps = T.getPrefixStatistics();
		
		for (int i=0; i<tst.length; i++) {
			String s1 = tst[i];
			SparseVector sv = ps.createSparseVector(s1);
			int bits = sv.getIndices().size();
			System.out.println(s1 + " has bitvector with "+bits+" indices.");
			for (int j=0; j<=i; j++) {
				String s2 = tst[j];
				double sim = ps.prefixSimilarity(s1, s2);
				System.out.println("sim("+s1+","+s2+")="+sim);
			}
		}
	
	}

}

// Test SparseVector creation
// use SparseVector.setLastIndex: last used bit position
