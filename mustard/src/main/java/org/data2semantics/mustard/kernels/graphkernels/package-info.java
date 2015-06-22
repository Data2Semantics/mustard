/**
 * The mustard graph kernels package contains a number of graph kernels divided over three packages, for three different (graph) datatypes.
 * 
 * For nearly all the kernels in the .rdfdata package it holds that they wrap kernels defined in the .singledtgraph package with the same name,
 * only now with the DTGraph prefix. This wrapping takes care of extracting the DTGraph from the RDFData.
 * Some kernels in the singledtgraph package in turn wrap kernels in the .graphlist package, again for convenience. Note that these kernels all
 * have 'GraphList' in the class name.
 * 
 * There are two main types of kernels, the kernels based on the Weisfeiler-Lehman (WL) algorithm, which count subtrees in graph(s), and the kernels
 * based on counting walks in a graph, which are indicated with Walk Count. Please look at {@link WLSubTreeKernel}, {@link DTGraphWLSubTreeKernel} and {@link WalkCountKernel}.
 * These are the most standard variants. Variants of these kernels have 'Tree' and 'Root' in their name indicating that the underlying graph structure
 * used for the instances is a tree or that they only count substructures (walks/subtrees) that start in the Root (i.e. instance node).
 * 
 * Some kernels have 'Approx' in their name. These kernels define approximate variants, that allow for some inexact matching between the counted substructures.
 * See {@link DTGraphWLSubTreeIDEQApproxKernel}.
 * 
 * Some WalkCount kernels are affixed 'MkII' this indicates that it is a second variant for the same kernel, with a different algorithm to compute it.
 * Similarly for the 'IDEQ' infix (in this case the kernels are not necessarily equal for the WL algorithm).
 * 
 * TODO refactor all the wrapping done individually per kernel into one generic wrapper class in the vein of the HubRemovalWrapper kernels.
 * 
 * TODO create a parameter class to use during the creation of a kernel, to avoid the long and incomprehensible list of parameters of the kernels.s
 * 
 * TODO refactor all kernels to use the {@link SimpleGraph} graph representation to remove the dependency on the nodes library. Currently this graph is only
 * used in the experimental GeoProb kernels.
 * 
 * TODO Both the WL and WalkCount kernels have a considerable amount of code duplication, this duplicate code should be refactored into separate classes.
 * 
 * 
 * @author Gerben
 * 
 */
package org.data2semantics.mustard.kernels.graphkernels;