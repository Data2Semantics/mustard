package org.data2semantics.mustard.experiments.data;

import static org.junit.Assert.*;

import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

public class MutagDataSetTest {

	@Test
	public void test() {
		RDFDataSet ts = new RDFFileDataSet("datasets/carcinogenesis.owl", RDFFormat.forFileName("datasets/carcinogenesis.owl"));
		
		MutagDataSet ds = new MutagDataSet(ts);
		ds.create();
	}

}
