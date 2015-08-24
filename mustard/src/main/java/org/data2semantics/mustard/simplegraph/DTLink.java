package org.data2semantics.mustard.simplegraph;


public interface DTLink<L,T> {
	public DTNode<L,T> from(); 
	public DTNode<L,T> to();   
	
	public T tag();
}
