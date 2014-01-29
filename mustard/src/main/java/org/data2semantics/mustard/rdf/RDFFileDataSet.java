package org.data2semantics.mustard.rdf;

import java.io.File;
import java.io.FileFilter;

import org.openrdf.rio.RDFFormat;


public class RDFFileDataSet extends RDFSingleDataSet 
{
	
	public RDFFileDataSet(String filename, RDFFormat fileFormat) {
			this(new File(filename), fileFormat);
	}

	public RDFFileDataSet(File file, RDFFormat fileFormat) {
		super(file.toString());
		if (file.isDirectory()) {
			addDir(file, fileFormat);
		} else {
			addFile(file, fileFormat);
		}
	}
	
	
	public void addFile(String filename, RDFFormat fileFormat) {
		try {
			this.rdfRep.getConnection().add(new File(filename), null, fileFormat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addFile(File file, RDFFormat fileFormat) {
		try {
			this.rdfRep.getConnection().add(file, null, fileFormat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addDir(File dir, RDFFormat fileFormat) {
		for (File file : dir.listFiles(new RDFFileFilter(fileFormat))) {
			System.out.println("Adding: " + file.getName());
			addFile(file, fileFormat);
			
		}
	}	
	
	public void addDir(String dirString, RDFFormat fileFormat) {
		addDir(new File(dirString), fileFormat);
	}
	
	class RDFFileFilter implements FileFilter {
		private RDFFormat fileFormat;
		
		public RDFFileFilter(RDFFormat fileFormat) {
			this.fileFormat = fileFormat;
		}
		
		public boolean accept(File file) {
			for (String ext : fileFormat.getFileExtensions()) {
				if (file.getName().endsWith(ext)) {
					return true;
				}
			}
			return false;
		 }
	}
	
	
	
}
