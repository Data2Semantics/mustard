package org.data2semantics.mustard.rdf;

import java.util.ArrayList;
import java.util.List;


import org.data2semantics.mustard.utils.Pair;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class DataSetUtils {

	/**
	 * Create a blacklist for a dataset from a list of instances and their labels.
	 * Every statement of the form <instance, _, value> and <value, _, instance> contained in the dataset is added to the blacklist.
	 * 
	 * NOTE - This method can be dangerous when the labels are primitives like boolean or int, since there might be a lot more
	 * relations that you don't want to remove.
	 *
	 * @param dataset
	 * @param instances
	 * @param labels
	 * @return
	 */
	public static List<Statement> createBlacklist(RDFDataSet dataset, List<Resource> instances, List<Value> labels) {
		List<Statement> newBL = new ArrayList<Statement>();

		for (int i = 0; i < instances.size(); i++) {
			newBL.addAll(dataset.getStatements(instances.get(i), null, labels.get(i), true));
			if (labels.get(i) instanceof Resource) {
				newBL.addAll(dataset.getStatements((Resource) labels.get(i), null, instances.get(i), true));
			}
		}
		return newBL;
	}
	
	
	/**
	 * Create a blacklist for a dataset given a list of pairs of resources. For each pair (p1,p2), all the statements <p1, _, p2>
	 * and <p2, _, p1> that are in the dataset are added to the blacklist.
	 * 
	 * NOTE - This method can be dangerous when the labels are primitives like boolean or int, since there might be a lot more
	 * relations that you don't want to remove.  
	 *
	 * @param dataset
	 * @param instances
	 * @return
	 */
	public static List<Statement> createBlacklist(RDFDataSet dataset, List<Pair<Resource, Resource>> instances) {
		List<Statement> newBL = new ArrayList<Statement>();

		for (int i = 0; i < instances.size(); i++) {
			newBL.addAll(dataset.getStatements(instances.get(i).getFirst(), null, instances.get(i).getSecond(), true));
			newBL.addAll(dataset.getStatements(instances.get(i).getSecond(), null, instances.get(i).getFirst(), true));		
		}
		return newBL;
	}
	
}
