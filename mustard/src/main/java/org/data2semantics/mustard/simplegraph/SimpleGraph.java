package org.data2semantics.mustard.simplegraph;

import java.util.ArrayList;
import java.util.List;

public class SimpleGraph<V,W> {

	private List<Node> _nodes = new ArrayList<Node>();
	
	public List<Node> nodes() { return _nodes; }
	public List<Link> links() {
		List<Link> links = new ArrayList<Link>();
		for (Node node : nodes()) {
			links.addAll(node.outLinks());
		}
		return links;
	}
	
	
	public  class Node {
	
		private V _label;
		private List<Link> _in_links  = new ArrayList<Link>();
		private List<Link> _out_links = new ArrayList<Link>();
		
		public Node(V label) {
			_nodes.add(this);
			this._label = label;
		}
		
		public List<Link> inLinks()  { return _in_links; }
		public List<Link> outLinks() { return _out_links; }
		
		public V label() {
			return _label;
		}
	}
	
	public class Link {
	
		private W    _tag;
		private Node _from;
		private Node _to;
		
		public Link(Node from, Node to, W tag) { 
			_from = from;
			_to   = to;
			_tag  = tag;
			_from._out_links.add(this);
			_to._in_links.add(this);
		}
		
		public Node from() { return _from; }
		public Node to()   { return _to;   }
		
		public W tag() {
			return _tag;
		}
	}
}
