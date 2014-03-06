package org.data2semantics.mustard.rdf;

import java.io.File;
import java.net.URL;

import org.openrdf.rio.RDFFormat;


public class RDFURLDataSet extends RDFSingleDataSet 
{
	public RDFURLDataSet(String url, RDFFormat fileFormat) {
		addFile(url, fileFormat);
	}
	
	public void addFile(String url, RDFFormat fileFormat) {
		try {
			File tempFile = File.createTempFile("ducktape_data", "temp");
			
			org.apache.commons.io.FileUtils.copyURLToFile(new URL(url), tempFile);	
			
			this.rdfRep.getConnection().add(tempFile, null, fileFormat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}