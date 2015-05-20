package org.data2semantics.mustard.experiments.playground;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.mustard.experiments.data.AIFBDataSet;
import org.data2semantics.mustard.experiments.data.ClassificationDataSet;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFRootWalkCountKernel;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.openrdf.rio.RDFFormat;

public class QuickFVCheck {
	private static String AIFB_FILE = "datasets/aifb-fixed_complete.n3";


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RDFDataSet tripleStore = new RDFFileDataSet(AIFB_FILE, RDFFormat.N3);
		ClassificationDataSet ds = new AIFBDataSet(tripleStore, true);
		ds.create();

		RDFRootWalkCountKernel k = new RDFRootWalkCountKernel(2, false, false);

		SparseVector[] fvs = k.computeFeatureVectors(ds.getRDFData());

		int lastIndex = fvs[0].getLastIndex();
		
		List<Integer> inds = new ArrayList<Integer>();
		for (int i = 0; i < lastIndex; i++) {
			inds.add(i);
		}
		List<String> labels = k.getFeatureDescriptions(inds);
		
		for (int i = 0; i < lastIndex; i++) {
			System.out.print(labels.get(i) + ", ");
		}
		System.out.print("\n");
		
		for (int j = 0; j < fvs.length; j++) {
			System.out.print(ds.getRDFData().getInstances().get(j) + ": ");
			for (int i = 0; i < lastIndex; i++) {
				System.out.print(fvs[j].getValue(i) + ", ");
			}
			System.out.print("\n");
		}

	}

}
