package org.data2semantics.mustard.simplegraph;

import java.util.List;


public interface DTNode<L,T> {
	public List<? extends DTLink<L,T>> inLinks();  
	public List<? extends DTLink<L,T>> outLinks(); 
	
	public L label();
	
	public DTLink<L,T> connect(DTNode<L,T> to, T tag);

}
