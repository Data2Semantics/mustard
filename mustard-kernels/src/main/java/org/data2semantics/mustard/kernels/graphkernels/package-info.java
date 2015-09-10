/**
 * <p>
 * The mustard graph kernels package contains a number of graph kernels, divided over three packages, for three different (graph) datatypes.
 * These three different types are defined in {@link org.data2semantics.mustard.kernels.data}.
 * </p>
 * 
 * <p>
 * For nearly all the kernels in the {@link org.data2semantics.mustard.kernels.graphkernels.rdfdata} package it holds that they wrap kernels defined in the 
 * {@link org.data2semantics.mustard.kernels.graphkernels.singledtgraph} package with the same name,
 * only now with the 'DTGraph' prefix. This wrapping takes care of extracting the DTGraph from the RDFData.
 * Some kernels in the {@link org.data2semantics.mustard.kernels.graphkernels.singledtgraph} package in turn wrap kernels in the 
 * {@link org.data2semantics.mustard.kernels.graphkernels.graphlist} package, again for convenience. Note that these kernels all
 * have 'GraphList' in the class name.
 * </p>
 * 
 * <p>
 * There are two main types of kernels, the kernels based on the Weisfeiler-Lehman (WL) algorithm, which count subtrees in graph(s), and the kernels
 * based on counting walks in a graph, which are indicated with 'WalkCount'. 
 * Please look at {@link org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeKernel}, {@link org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeKernel} and {@link org.data2semantics.mustard.kernels.graphkernels.graphlist.WalkCountKernel}.
 * These are the most standard variants. Variants of these kernels have 'Tree' and 'Root' in their name indicating that the underlying graph structure
 * used for the instances is a tree or that they only count substructures (walks/subtrees) that start in the Root (i.e. instance node).
 * </p>
 * 
 * <p> 
 * Some kernels have 'Approx' in their name. These kernels define approximate variants, that allow for some inexact matching between the counted substructures.
 * See {@link org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeIDEQApproxKernel}.
 * </p>
 * 
 * <p>
 * Some WalkCount kernels are affixed 'MkII' this indicates that it is a second variant for the same kernel, with a different algorithm to compute it.
 * Similarly for the 'IDEQ' infix (in this case the kernels are not necessarily equal for the WL algorithm).
 * </p>
 * 
 * 
 * @author Gerben
 * 
 */
package org.data2semantics.mustard.kernels.graphkernels;