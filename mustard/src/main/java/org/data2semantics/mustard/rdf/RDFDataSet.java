package org.data2semantics.mustard.rdf;

import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public abstract class RDFDataSet
{
	private String label;

	public RDFDataSet() {
		this.label = "RDF dataset";
	}
	
	public RDFDataSet(String label) {
		this();
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
	
	public abstract Statement createStatement(URI subject, URI predicate, URI object);
	
	public abstract URI createURI(String uri);
	
	public abstract Literal createLiteral(String lit);
	
	public abstract void addStatements(List<Statement> stmts);
	

	public List<Statement> getFullGraph() 
	{	
		return getFullGraph(false);
	}
		
	public List<Statement> getFullGraph(boolean inference) 
	{	
		return getStatements(null, null, null, inference);
	}
	
	
	public List<Statement> getStatements(Resource subject, URI predicate, Value object) {
		return getStatements(subject, predicate, object, false);
	}

	public abstract List<Statement> getStatements(Resource subject, URI predicate, Value object, boolean allowInference);

	public List<Statement> getStatementsFromStrings(String subject, String predicate, String object) {
		return getStatementsFromStrings(subject, predicate, object, false);
	}

	public abstract List<Statement> getStatementsFromStrings(String subject, String predicate, String object, boolean allowInference); 
	
	public abstract void removeStatements(Resource subject, URI predicate, Value object); 
	
	public abstract void removeStatementsFromStrings(String subject, String predicate, String object);	
			
}
