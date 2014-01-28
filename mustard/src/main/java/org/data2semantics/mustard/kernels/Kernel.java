package org.data2semantics.mustard.kernels;

/**
 * Kernel interface, used as a marker interface, since this does not define on what we compute a kernel.
 * 
 * @author Gerben
 */
public interface Kernel {
	public String getLabel();
	public void setNormalize(boolean normalize);	
}
