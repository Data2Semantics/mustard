package org.data2semantics.mustard.experiments.rescal;

import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.rdfvault.StringTree;
import org.data2semantics.mustard.utils.Pair;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

public class RESCALLinkPredictor {
	public static final String PYTHON_EXE = "C:\\Users\\Gerben\\Dropbox\\D2S\\python_stuff\\WinPython-64bit-2.7.6.4\\python-2.7.6.amd64\\python.exe";
	public static final String RESCAL_DIR = "C:\\Users\\Gerben\\Dropbox\\D2S\\python_stuff\\Ext-RESCAL-master";
	public static final String WORKING_DIR = "test";

	private double lambda;
	private int numFactors;

	private Map<Resource, double[]> entityEmb;
	private Map<Pair<URI,Literal>, double[]>   attributeEmb;
	private Map<URI, double[][]>    latentFac;
	private Map<String, double[][]>    addLatentFac;


	public RESCALLinkPredictor(int numFactors, double lambda) {
		super();
		this.lambda = lambda;
		this.numFactors = numFactors;
	}

	public void train(List<Statement> rdf) {
		RESCALWrapper rw = new RESCALWrapper(PYTHON_EXE, RESCAL_DIR, WORKING_DIR);
		RESCALInput in = new RESCALInput();
		in.convert(rdf);

		StringTree st = new StringTree();

		for (int key : in.getInvEntityMap().keySet()) {
			st.store(in.getInvEntityMap().get(key).toString());
		}
		double[][] slice = new double[in.getInvEntityMap().size()][in.getInvEntityMap().size()];

		StringTree.PrefixStatistics stat = st.getPrefixStatistics(true);

		for (int k1 : in.getInvEntityMap().keySet()) {
			for (int k2 : in.getInvEntityMap().keySet()) {
				double sim = stat.prefixSimilarity(in.getInvEntityMap().get(k1).toString(), in.getInvEntityMap().get(k2).toString());
				if (sim > 0.9) {
					slice[k1][k2] = sim;
				} else {
					slice[k1][k2] = 0.0;
				}
			}
		}
		in.addEntitySimilaritySlice(slice, "URIPrefixSlice");


		RESCALOutput out = rw.factorize(in, numFactors, lambda);
		entityEmb = out.getEntityEmb();
		attributeEmb = out.getAttributeEmb();
		latentFac = out.getLatentFac();
		addLatentFac = out.getAdditionalLatentFac();
	}

	public double predict(Statement stmt) {
		// Relation
		if (!(stmt.getObject() instanceof Literal)) {
			return dotProduct(computeRow(entityEmb.get(stmt.getSubject()), latentFac.get(stmt.getPredicate())), entityEmb.get((Resource) stmt.getObject()));
		} else { // Literal
			Pair<URI, Literal> p = new Pair<URI,Literal>(stmt.getPredicate(), (Literal) stmt.getObject());
			return dotProduct(entityEmb.get(stmt.getSubject()), attributeEmb.get(p));	
		}
	}

	private double[] computeRow(double[] ent, double[][] factors) {
		double[] ret = new double[ent.length];

		/* we assume correct sizing and squareness of the factors matrix */
		for (int i = 0; i < ent.length; i++) {
			ret[i] = 0;

			// dot product, but with the column, so cannot use the private method below
			for (int j = 0; j < ent.length; j++) {
				ret[i] += ent[j] * factors[j][i];
			}
		}
		return ret;
	}

	private double dotProduct(double[] v1, double[] v2) {
		double ret = 0;

		if (v1 != null && v2 != null) {
			for (int i = 0; i < v1.length; i++) {
				ret += v1[i] * v2[i];
			}
		}
		return ret;
	}


}
