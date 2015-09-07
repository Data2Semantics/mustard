package org.data2semantics.mustard.kernels.data;

import java.io.Serializable;
import java.util.List;

import org.data2semantics.mustard.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

/**
 * <p>
 * RDFData is graph data in the form of an RDFDataSet with a list of instances (Resources) and a blacklist (Statements)
 * 
 * <p>
 * The instances are nodes in an RDF graph (which is contained in the RDFDataSet).
 * 
 * <p>
 * The blackList is a list of Statements (ie. triples) that should be ignored in the RDFDataSet. 
 * Most often this is needed when these Statements contain the labels for the learning task. 
 * Typically, these should not be part of the RDF graph during training, since they are also not part of the RDF graph during testing.
 * 
 * 
 * @author Gerben
 *
 */
public class RDFData implements GraphData, Serializable {
	private static final long serialVersionUID = -6267540751250144084L;
	private RDFDataSet dataset;
	private List<Resource> instances;
	private List<Statement> blackList;

	public RDFData(RDFDataSet dataset, List<Resource> instances,
			List<Statement> blackList) {
		super();
		this.dataset = dataset;
		this.instances = instances;
		this.blackList = blackList;
	}

	public RDFDataSet getDataset() {
		return dataset;
	}

	public List<Resource> getInstances() {
		return instances;
	}

	public List<Statement> getBlackList() {
		return blackList;
	}

	/**
	 * returns the size of the list of instances.
	 * 
	 */
	public int numInstances() {
		return instances.size();
	}	


}
