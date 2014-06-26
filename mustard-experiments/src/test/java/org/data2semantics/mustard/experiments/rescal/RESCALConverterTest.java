package org.data2semantics.mustard.experiments.rescal;

import static org.junit.Assert.*;

import org.data2semantics.mustard.experiments.rescal.RESCALConverter;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

public class RESCALConverterTest {
	private static String dataFile = "datasets/aifb-fixed_complete.n3";	

	@Test
	public void test() {
		RDFDataSet dataset = new RDFFileDataSet(dataFile, RDFFormat.N3);
		RESCALConverter rc = new RESCALConverter();
		rc.convert(dataset.getFullGraph());
		rc.save("test/");
		
		
	}

}
