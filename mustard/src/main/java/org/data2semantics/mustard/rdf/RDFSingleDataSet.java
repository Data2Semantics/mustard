package org.data2semantics.mustard.rdf;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

public class RDFSingleDataSet extends RDFDataSet
{
	protected Repository rdfRep;
	private String label;

	public RDFSingleDataSet() {
		try {
			rdfRep = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
			rdfRep.initialize();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public RDFSingleDataSet(String label) {
		this();
		this.label = label;
	}
	
	public RDFSingleDataSet(Repository rdfRep, String label) {
		this.rdfRep = rdfRep;
		this.label = label;
	}

	/* (non-Javadoc)
	 * @see org.data2semantics.tools.rdf.RDFDataSet#getLabel()
	 */
	@Override
	public String getLabel() {
		return this.label;
	}
	
	
	/* (non-Javadoc)
	 * @see org.data2semantics.tools.rdf.RDFDataSet#createStatement(org.openrdf.model.URI, org.openrdf.model.URI, org.openrdf.model.URI)
	 */
	@Override
	public Statement createStatement(URI subject, URI predicate, URI object) {
		return rdfRep.getValueFactory().createStatement(subject, predicate, object, null);
	}
	
	/* (non-Javadoc)
	 * @see org.data2semantics.tools.rdf.RDFDataSet#createURI(java.lang.String)
	 */
	@Override
	public URI createURI(String uri) {
		return rdfRep.getValueFactory().createURI(uri);
	}
	
	/* (non-Javadoc)
	 * @see org.data2semantics.tools.rdf.RDFDataSet#createLiteral(java.lang.String)
	 */
	@Override
	public Literal createLiteral(String lit) {
		return rdfRep.getValueFactory().createLiteral(lit);
	}
	
	/* (non-Javadoc)
	 * @see org.data2semantics.tools.rdf.RDFDataSet#addStatements(java.util.List)
	 */
	@Override
	public void addStatements(List<Statement> stmts) {
		try {
			this.rdfRep.getConnection().add(stmts, (Resource) null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public List<Statement> getFullGraph() 
	{	
		return getStatements(null, null, null, true);
	}
	
	public List<Statement> sparqlQuery(String sparqlQuery) {
		List<Statement> graph = new ArrayList<Statement>();

		try {
			RepositoryConnection repCon = rdfRep.getConnection();
			try {
				GraphQueryResult graphResult = repCon.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery).evaluate();

				try {
					while (graphResult.hasNext()) {
						graph.add(graphResult.next());
					}					
				} finally {
					graphResult.close();
				}							
			} finally {
				repCon.close();
			}			

		} catch (Exception e) {
			e.printStackTrace();
		}

		return graph;
	}
	
	
	/* (non-Javadoc)
	 * @see org.data2semantics.tools.rdf.RDFDataSet#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value)
	 */
	@Override
	public List<Statement> getStatements(Resource subject, URI predicate, Value object) {
		return getStatements(subject, predicate, object, false);
	}

	/* (non-Javadoc)
	 * @see org.data2semantics.tools.rdf.RDFDataSet#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, boolean)
	 */	
	@Override
	public List<Statement> getStatements(Resource subject, URI predicate, Value object, boolean allowInference) {
		List<Statement> resGraph = new ArrayList<Statement>();

		try {
			RepositoryConnection repCon = rdfRep.getConnection();

			try {
				
				
				RepositoryResult<Statement> statements = repCon.getStatements(subject, predicate, object, allowInference);

				try {
					while (statements.hasNext()) {
						resGraph.add(statements.next());
					}
				}
				finally {
					statements.close();
				}
			} finally {
				repCon.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return resGraph;		
	}




	/* (non-Javadoc)
	 * @see org.data2semantics.tools.rdf.RDFDataSet#getStatementsFromStrings(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<Statement> getStatementsFromStrings(String subject, String predicate, String object) {
		return getStatementsFromStrings(subject, predicate, object, false);
	}

	/* (non-Javadoc)
	 * @see org.data2semantics.tools.rdf.RDFDataSet#getStatementsFromStrings(java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public List<Statement> getStatementsFromStrings(String subject, String predicate, String object, boolean allowInference) 
	{	
		URI querySub = null;
		URI queryPred = null;
		URI queryObj = null;

		if (subject != null) {
			querySub = rdfRep.getValueFactory().createURI(subject);
		}

		if (predicate != null) {
			queryPred = rdfRep.getValueFactory().createURI(predicate);
		}		

		if (object != null) {
			queryObj = rdfRep.getValueFactory().createURI(object);
		}

		return getStatements(querySub, queryPred, queryObj, allowInference);
	}

	
	/* (non-Javadoc)
	 * @see org.data2semantics.tools.rdf.RDFDataSet#removeStatementsFromStrings(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void removeStatementsFromStrings(String subject, String predicate, String object) {
		
		URI querySub = null;
		URI queryPred = null;
		URI queryObj = null;

		if (subject != null) {
			querySub = rdfRep.getValueFactory().createURI(subject);
		}

		if (predicate != null) {
			queryPred = rdfRep.getValueFactory().createURI(predicate);
		}		

		if (object != null) {
			queryObj = rdfRep.getValueFactory().createURI(object);
		}
		removeStatements(querySub, queryPred, queryObj);		
	}
	
	
	/* (non-Javadoc)
	 * @see org.data2semantics.tools.rdf.RDFDataSet#removeStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value)
	 */
	@Override
	public void removeStatements(Resource subject, URI predicate, Value object) {
		try {
			RepositoryConnection repCon = rdfRep.getConnection();

			try {
				repCon.remove(subject, predicate, object);
			} finally {
				repCon.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
			


			
}
