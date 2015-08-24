package org.data2semantics.mustard.simplegraph;

import java.util.ArrayList;
import java.util.List;

public class SimpleGraph<L,T> {

	private List<Node> _nodes = new ArrayList<Node>();
	
	public List<Node> nodes() { return _nodes; }
	public List<Link> links() {
		List<Link> links = new ArrayList<Link>();
		for (Node node : nodes()) {
			links.addAll(node.outLinks());
		}
		return links;
	}
	
	public DTNode<L,T> add(L label) {
		return new Node(label);
	}
	
	
	public class Node implements DTNode<L,T> {
	
		private L _label;
		private List<Link> _in_links  = new ArrayList<Link>();
		private List<Link> _out_links = new ArrayList<Link>();
		
		public Node(L label) {
			_nodes.add(this);
			this._label = label;
		}
		
		public List<Link> inLinks()  { return _in_links; }
		public List<Link> outLinks() { return _out_links; }
		
		public L label() {
			return _label;
		}
		
		public DTLink<L,T> connect(DTNode<L,T> to, T tag) {
			return new Link(this, to, tag);
		}
	}
	
	public class Link implements DTLink<L,T> {
	
		private T    _tag;
		private DTNode<L,T> _from;
		private DTNode<L,T> _to;
		
		public Link(DTNode<L,T> from, DTNode<L,T> to, T tag) { 
			_from = from;
			_to   = to;
			_tag  = tag;
		//	_from.outLinks().add(this);
		//	_to.inLinks().add(this);
		}
		
		public DTNode<L,T> from() { return _from; }
		public DTNode<L,T> to()   { return _to;   }
		
		public T tag() {
			return _tag;
		}
	}
}
