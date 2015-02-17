package org.data2semantics.mustard.rdfvault;

import java.util.Arrays;
import java.util.HashMap;

import org.data2semantics.mustard.learners.SparseVector;

/**
 * An implementation of Patricia tries over Strings that implements {@link Vault Vault&lt;String&gt;}.
 * It is both fast and memory efficient, especially for strings that have long
 * prefixes in common.
 *      
 * @author  Steven de Rooij
 */
public class StringTree {
	
	// The root of the tree is always present and always an internal node.
	private InternalNode _root = new InternalNode(null, null, null, null);
	private int  _size = 0; // The number of items in the tree.
	private static final int ITEM_MARKER = 65536; // n.find_child(ITEM_MARKER) returns a dummy child if present.
	
	/**
	 * Returns the number of unique items stored in the vault.
	 * @return The number of items.
	 */
	public int size() { return _size; }
	
	public void trash(Object entry) {
		Leaf e = (Leaf)entry;
		e.verify();
		InternalNode n = e._parent; // we need the parent so we can repair its neighborhood later
		e.remove();       	        // unlink the leaf and mark it as invalid
		e.invalidate();
		_size--;
		
		// see if I am now a node with 0 or 1 children, if so remove me too
		if (n._parent==null) return;                   // never remove the root
		
		Node ch = n._children;
		if (ch==null) { n.remove(); return; } // no more children? just remove node
		if (ch._siblings!=null) return; // more than one child? Then we're done

		// There is exactly one child. Remove n and join the child with its parent's parent.
		int l_n = n._label.length;
		char [] new_label;
		if (ch._label==null) {
			new_label = n._label;
		} else {
			new_label = Arrays.copyOf(n._label, l_n + ch._label.length);
			System.arraycopy(ch._label, 0, new_label, l_n, ch._label.length);
		}
		n.remove();
		ch._label = new_label;
		ch._parent = n._parent;
		ch._siblings = n._parent._children;
		n._parent._children = ch;
	}

	public Object store(String item) {
		// Find the parent of the leaf for this item. If the parent does not exist yet, create it.
		int len = item.length();
		InternalNode n = _root;
		int depth = 0;
		search: while (depth<len) {
			Node ch = n.find_child(item.charAt(depth));
			if (ch==null) break search; // we know a leaf can be added
			int ext;
			for (ext=1; ext<ch._label.length; ext++) {
				if (depth+ext==len || item.charAt(depth+ext)!=ch._label[ext]) {
					n = ch.split_edge(ext);
					depth += ext;
					break search;
				}
			}
			// we have traversed the edge label and now we've reached ch
			depth += ext;
			if (ch instanceof Leaf) {
				if (depth==len) return ch;
				// the leaf represents a prefix of this item. Insert an internal node here
				n = ch.split_edge(ext);
				break search;
			}
			n = (InternalNode)ch;
		}

		// Add a new leaf to the tree, starting at internal node n.
		
		char [] new_label;
		if (len==depth) {
			Node ch = n.find_child(ITEM_MARKER);
			if (ch!=null) return ch;
			new_label=null;
		} else {
			new_label = item.substring(depth).toCharArray();
		}
		
		Leaf res = new Leaf(n, n._children, new_label); 
		n._children = res;
		_size++;
		return res;
	}
	
	public Object search(String item) {
		Node n;
		int depth;
		for (n=_root, depth=0; depth<item.length(); depth+=n._label.length) {
			if (n instanceof Leaf) return null;
			Node ch = ((InternalNode)n).find_child(item.charAt(depth));
			if (ch==null) return null;
			for (int i=1; i<ch._label.length; i++) {
				if (depth+i==item.length() || item.charAt(depth+i)!=ch._label[i]) return null;
			}
			n = ch;
		}		
		return n instanceof Leaf ? n : ((InternalNode)n).find_child(ITEM_MARKER);
	}
	
	public String redeem(Object ticket) {
		if (!(ticket instanceof Leaf)) return null;
		Leaf leaf = (Leaf)ticket;
		leaf.verify();
		StringBuilder sb = new StringBuilder();
		leaf.redeem_rec(sb);
		return sb.toString();
	}
	
	// once prefixstatistics are requested, no tree operations should be performed except through the statistics object
	public PrefixStatistics getPrefixStatistics() { return new PrefixStatistics(); }
	
	public class PrefixStatistics {
		
		private HashMap<InternalNode, Integer> _node2bitix = new HashMap<InternalNode,Integer>();
		private HashMap<InternalNode, Integer> _leaves     = new HashMap<InternalNode,Integer>();
		private int _bits_used = 0;
		
		// assign a bit to identify each of its children, and recurse.
		// returns the number of leafs with this node as ancestor.
		private int initialise_rec(InternalNode node) {
			_node2bitix.put(node, _bits_used);
			for (Node n = node._children; n!=null; n=n._siblings) {
				_bits_used++;
			}
			int leaves = 0;
			for (Node n = node._children; n!=null; n=n._siblings) {
				if (n instanceof InternalNode) leaves += initialise_rec((InternalNode)n); else leaves++;
			}
			_leaves.put(node, leaves);
			return leaves;
		}
		
		public PrefixStatistics() { initialise_rec(_root); }
		
		/* _node2bitix assigns bit indices to all the internal tree nodes.
		 * Parent first, then subtrees in order.
		 * Thus, to find the size of the subtree rooted by a node, find the
		 * _node2bitix of its sibling, or if it is the last sibling, the first on the path to the
		 * root that has a sibling.
		 */
		public int sizeOfSubtree(Node n) {
			int ix0 = _node2bitix.get(n);
			Node m = n;
			while (m._siblings==null && m!=_root) m = m._parent;
			int ix1 = m==_root ? _bits_used : _node2bitix.get(m._siblings);
			return ix1-ix0;
		}
		
		public SparseVector createSparseVector(String s) {
			SparseVector sv = new SparseVector();
			sv.setLastIndex(_bits_used-1);
			
			Node n = _root;
			int depth = 0;
			while (n instanceof InternalNode) {
				// invariant: at internal node, depth <= s.length()
				int ix0 = _node2bitix.get(n);
				int c = depth==s.length() ? ITEM_MARKER : s.charAt(depth);
				Node ch = ((InternalNode)n)._children;
				int i;
				for (i=0; ch.label_char()!=c; i++) {
					ch = ch._siblings;
				}
				sv.setValue(ix0+i, 1);
				depth += ch._label==null ? 0 : ch._label.length;
				n = ch;
			}
			
			return sv;
		}
		
		public double prefixSimilarity(String s1, String s2) {
			Node n = _root;
			int depth = 0;
			while (n instanceof InternalNode) {
				int c1 = depth==s1.length() ? ITEM_MARKER : s1.charAt(depth);
				int c2 = depth==s2.length() ? ITEM_MARKER : s2.charAt(depth);
				if (c1 != c2) break; // paths diverge at this node
				n = ((InternalNode)n)._children;
				while (n.label_char()!=c1) {
					n = n._siblings;
				}
				depth += n._label==null ? 0 : n._label.length;
			}
			// n is the internal node where the paths diverge, or a leaf indicating an exact match
			if (n instanceof Leaf) return 1.0;
			return 1.0 - (double)_leaves.get(n) / (double)_size;
		}
		
	}
	
	///////////////////////////////////////// Tree nodes ///////////////////////////////////////////////
	
	private static abstract class Node {
		protected InternalNode _parent;
		protected Node         _siblings;
		protected char []      _label;

		// Returns the first character of the edge label into this node, or ITEM_MARKER if the label is empty
		private int label_char() {
			return _label==null ? ITEM_MARKER : _label[0];
		}
		
		// Elminates this node from the tree. Do not remove the root node.
		protected void remove() {
			InternalNode n = _parent;
			if (n._children==this) {
				n._children = _siblings; 
			} else {
				Node m = n._children;
				while (m._siblings!=this) { m = m._siblings; }
				m._siblings = m._siblings._siblings;
			}
		}
	
		// inserts an internal node into this edge with a label of length ext 
		private InternalNode split_edge(int ext) {
			char [] label_chunk1, label_chunk2;
			if (ext==_label.length) {
				label_chunk1 = _label; label_chunk2 = null;
			} else {
				label_chunk1 = Arrays.copyOfRange(_label, 0, ext);
				label_chunk2 = Arrays.copyOfRange(_label, ext, _label.length);
			}
			InternalNode n = new InternalNode(_parent, this, _siblings, label_chunk1);
			_parent._children = n;
			_parent = n;
			_siblings = null;
			_label = label_chunk2;
			return n;
		}
		
		// helper for redeem
		protected void redeem_rec(StringBuilder sb) {
			if (_parent==null) return; // root node has empty string and no parent
			_parent.redeem_rec(sb);
			if (_label!=null) sb.append(_label);
		}	
	}
	
	private static class InternalNode extends Node {
		private Node _children;
	
		private InternalNode(InternalNode parent, Node children, Node siblings, char [] label) {
			_parent = parent;
			_children = children;
			_siblings = siblings;
			_label = label;
		}
		
		/* Return the child whose edge label starts with c, or null if it is not there.
		 * The requested child, if it exists, is moved to the front of the linked list.
		 * This may speed up future lookups, and makes it easier to relink the child. 
		 */
		private Node find_child(int c) {
			Node ch = _children;
			if (ch==null || ch.label_char()==c) return ch;
			for (Node cur=ch, nxt=ch._siblings; nxt!=null; cur=nxt, nxt=cur._siblings) {
				if (nxt.label_char()==c) {
					cur._siblings = nxt._siblings;
					nxt._siblings = ch;
					_children = nxt;
					return nxt;
				}
			}
			return null;
		}		
	}
	
	private static class Leaf extends Node {

		private Leaf(InternalNode parent, Node siblings, char [] label) {
			_parent = parent;
			_siblings = siblings;
			_label = label;
		}
		
		private void invalidate() { _parent = null; _siblings = null; _label = null; }
		private void verify() { if (_parent==null) throw new InvalidTicketException(); }
	}
}
