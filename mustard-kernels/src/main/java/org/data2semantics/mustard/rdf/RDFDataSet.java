package org.data2semantics.mustard.rdf;

import java.util.Collection;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;


/**
 * Abstract class that defines an RDF dataset with some necessary methods
 * 
 * @author Gerben
 *
 */
public abstract class RDFDataSet implements Serializable {
	private static final long serialVersionUID = -1478501093710185561L;
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
	
	protected abstract void initialize();
	
	/**
	 * Factory method to create a Statement
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return
	 */
	public abstract Statement createStatement(URI subject, URI predicate, URI object);
	
	/**
	 * Factory method to create a URI
	 * 
	 * @param uri
	 * @return
	 */
	public abstract URI createURI(String uri);
	
	/**
	 * Factory method to create a Literal
	 * 
	 * @param lit
	 * @return
	 */
	public abstract Literal createLiteral(String lit);
	
	/**
	 * Add statements to the RDF dataset
	 * 
	 * @param stmts
	 */
	public abstract void addStatements(Collection<Statement> stmts);
	

	/**
	 * Get the entire RDF graph in the dataset, i.e. all the statements.
	 * 
	 * @return
	 */
	public List<Statement> getFullGraph() 
	{	
		return getFullGraph(false);
	}
		
	/**
	 * Get the full graph, potentially with inferencing by the triplestore if inferene=true.
	 * 
	 * @param inference
	 * @return
	 */
	public List<Statement> getFullGraph(boolean inference) 
	{	
		return getStatements(null, null, null, inference);
	}
	
	/**
	 * Get all the statements in the dataset that fit the provided subject, predicate, object.
	 * Use null values as wildcards, i.e. for all statements with a given subject use getStatements(subject, null, null)
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return
	 */
	public List<Statement> getStatements(Resource subject, URI predicate, Value object) {
		return getStatements(subject, predicate, object, false);
	}

	/**
	 * Like getStatements/3 but with an added boolean to allow triplestore inferencing
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param allowInference
	 * @return
	 */
	public abstract List<Statement> getStatements(Resource subject, URI predicate, Value object, boolean allowInference);

	/**
	 * Use strings for the subject, predicate and objects, null values are wildcards.
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return
	 */
	public List<Statement> getStatementsFromStrings(String subject, String predicate, String object) {
		return getStatementsFromStrings(subject, predicate, object, false);
	}

	public abstract List<Statement> getStatementsFromStrings(String subject, String predicate, String object, boolean allowInference); 
	
	public abstract void removeStatements(Resource subject, URI predicate, Value object); 
	
	public abstract void removeStatementsFromStrings(String subject, String predicate, String object);	
			
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeObject(getFullGraph());
		
		oos.defaultWriteObject();
	}
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		List<Statement> fullgraph = (List<Statement>)ois.readObject();
		ois.defaultReadObject();
		initialize();
		addStatements(fullgraph);
	}
}
