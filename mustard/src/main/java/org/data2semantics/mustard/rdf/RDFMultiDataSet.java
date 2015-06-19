package org.data2semantics.mustard.rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class RDFMultiDataSet extends RDFDataSet {
	private static final long serialVersionUID = 5959426528335298053L;
	private List<RDFDataSet> datasets;
	
	public RDFMultiDataSet() {
		this("Multi Repository Dataset");
	}
	
	public RDFMultiDataSet(String label) {
		super(label);
		datasets = new ArrayList<RDFDataSet>();
	}
	
	public void addRDFDataSet(RDFDataSet dataset) {
		datasets.add(dataset);
	}	

	@Override
	public Statement createStatement(URI subject, URI predicate, URI object) {
		return datasets.get(0).createStatement(subject, predicate, object);
	}

	@Override
	public URI createURI(String uri) {
		return datasets.get(0).createURI(uri);
	}

	@Override
	public Literal createLiteral(String lit) {
		return datasets.get(0).createLiteral(lit);
	}

	@Override
	public void addStatements(Collection<Statement> stmts) {
		datasets.get(0).addStatements(stmts);
	}

	@Override
	public List<Statement> getStatements(Resource subject, URI predicate,
			Value object) {
		return getStatements(subject, predicate, object, false);
	}

	@Override
	public List<Statement> getStatements(Resource subject, URI predicate,
			Value object, boolean allowInference) {
		
		List<Statement> resGraph = new ArrayList<Statement>();

		for (RDFDataSet dataset : datasets) {
			List<Statement> res = dataset.getStatements(subject, predicate, object, allowInference);
			if (res != null) {
				resGraph.addAll(res);
			}
		}	
		return resGraph;		
	}

	@Override
	public List<Statement> getStatementsFromStrings(String subject,
			String predicate, String object) {
		return getStatementsFromStrings(subject, predicate, object, false);
	}

	@Override
	public List<Statement> getStatementsFromStrings(String subject,
			String predicate, String object, boolean allowInference) {
		URI querySub = null;
		URI queryPred = null;
		URI queryObj = null;

		if (subject != null) {
			querySub = createURI(subject);
		}

		if (predicate != null) {
			queryPred = createURI(predicate);
		}		

		if (object != null) {
			queryObj = createURI(object);
		}
		return getStatements(querySub, queryPred, queryObj, allowInference);
	}

	@Override
	public void removeStatementsFromStrings(String subject, String predicate,
			String object) {
		datasets.get(0).removeStatementsFromStrings(subject, predicate, object);
	}

	@Override
	public void removeStatements(Resource subject, URI predicate, Value object) {
		datasets.get(0).removeStatements(subject, predicate, object);
	}

	@Override
	protected void initialize() {
		// TODO Auto-generated method stub
		
	}
}
