package org.data2semantics.mustard.rdf;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.sparql.SPARQLRepository;

public class RDFSparqlDataSet extends RDFSingleDataSet {
	private static final long serialVersionUID = -4044064372906037138L;
	private List<String> nameSpaces;
	private File logFile;
	private boolean fake; // use this to test of things work, by writing the potential subjects to a logFile


	public RDFSparqlDataSet(String url) {
		this(url, new ArrayList<String>());
	}

	public RDFSparqlDataSet(String url, List<String> nameSpaces) {
		super(url);		
		this.rdfRep = new SPARQLRepository(url);
		this.nameSpaces = nameSpaces;
		this.fake = false;

		try {
			this.rdfRep.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Statement> getStatements(Resource subject, URI predicate,
			Value object, boolean allowInference) {

		for (String nameSpace : nameSpaces) {
			if ((subject != null && subject.toString().startsWith(nameSpace)) || (object != null && object.toString().startsWith(nameSpace))) {
				if (!fake) {
					List<Statement> res =  super.getStatements(subject, predicate, object, allowInference);
					//System.out.println("Querying external dataset " + super.getLabel() + ", got: " + res.size() + " statements.");
					return res;
				} else {
					try {
						FileWriter out = new FileWriter(logFile, true);
						if (subject != null) {
							out.write(subject.toString());
							out.write("\n");
						}
						out.close();
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					return new ArrayList<Statement>();
				}
			}
		}
		return new ArrayList<Statement>();
	}

	public void setLogFile(String filename) {
		logFile = new File(filename);
		fake = true;

	}

}
