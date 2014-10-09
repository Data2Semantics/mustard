package org.data2semantics.mustard.experiments.modules.data;

import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFURLDataSet;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.openrdf.rio.RDFFormat;

@Module(name="RDFDataSet")
public class RDFDataSetModule {
	private RDFDataSet dataset;
	private String filename;
	private RDFFormat format;
	private boolean isUrl;
	
	// TODO add more constructor's for different types of RDF datasets, i.e. from url, from directory, etc.
	
	/**
	 * RDFDataSet based on a single file
	 * 
	 * @param filename
	 * @param mimetype
	 */
	public RDFDataSetModule(
			@In(name="filename") String filename, 
			@In(name="mimetype") String mimetype) {
		this(filename, mimetype, false);
	}
	
	/**
	 * RDFDataSet based on a single file
	 * 
	 * @param filename
	 * @param mimetype
	 */
	public RDFDataSetModule(
			@In(name="filename") String filename, 
			@In(name="mimetype") String mimetype,
			@In(name="isUrl") Boolean isUrl) {
		super();
		this.filename = filename;
		this.format = RDFFormat.forMIMEType(mimetype);
		this.isUrl = isUrl;
	}

	
	
	

	@Main
	public RDFDataSet createDataSet() {	
		if (!isUrl) {
			dataset = new RDFFileDataSet(filename, format);	
		} else {
			dataset = new RDFURLDataSet(filename, format);
		}
		
		return dataset;
	}
	
	@Out(name="dataset")
	public RDFDataSet getDataSet() {
		return dataset;
	}	
}
