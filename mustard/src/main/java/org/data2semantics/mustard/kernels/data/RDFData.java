package org.data2semantics.mustard.kernels.data;

import java.util.List;

import org.data2semantics.mustard.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RDFData implements GraphData {
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
}
