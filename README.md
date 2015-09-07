mustard - Machine learning Using Svms To Analyse Rdf Data
=========================================================

Mustard is a machine learning library for learning from RDF data using kernel methods. Currently the library itself supports Support Vector Machines via the Java versions of the LibSVM and LibLINEAR libraries.

This repository consists of 4 projects:

- `mustard-kernels`, this is the heart of the library. It contains the graph kernels for RDF and the additional classes needed to handle RDF. This part of the library is the most well maintained and documented.
- `mustard-learners` wraps LibSVM and LibLINEAR for use with the kernels defined in `mustard`. Together with `mustard-kernels` you can build SVM classifiers for RDF data.
- `mustard-experiments` contains experiments and utility classes for experimenting with `mustard`.
- `mustard-ducktape-experiments` contains classes to perform experiments with `mustard` using `ducktape`, which is also located in de Data2Semantics github. This part of the library is hardly maintained at the moment.


Contents
--------

1. Usage
2. Kernel documentation
3. LOD extension
4. Dependencies
5. JWS paper


Usage
-----

The `mustard-kernels` and `mustard-learners` project can be used together to build classifiers for RDF datasets. Examples of how to do this can be found in `mustard-experiments`. Note that the library is currently mainly used for experimental comparisons between algorithms. 

The general set up to build a classifier is as follows.

First, we need a dataset, for which we use the `RDFDataSet` class. 
```java
RDFDataSet tripleStore = new RDFFileDataSet("some_filename.n3", RDFFormat.N3);
```
Note that other files than N3 are also supported.

The instances that we want to build a classifier for are nodes/resources in this RDF graph. This list of instances can be supplied externally, or they could be extracted from the RDF, for example like this.
```java
List<Statement> stmts = tripleStore.getStatementsFromStrings(null, RDF.TYPE, some_class_uri);

List<Resource> instances = new ArrayList<Resource>();

for (Statement stmt : stmts) {
	instances.add(stmt.getSubject());
}
```
In this example we extract all the resources that are of the type `some_class_uri`.



Kernel documentation
--------------------
Please see `/src/main/java/org/data2semantics/mustard/kernels/graphkernels/package-info.java` for the main documentation on the different graph kernels defined.


LOD extension
-------------
Part of the kernels available in this library are also available in the Linked Open Data extension, developed at the University of Mannheim, for the popular RapidMiner data mining software. See <http://dws.informatik.uni-mannheim.de/en/research/rapidminer-lod-extension/> and <http://dws.informatik.uni-mannheim.de/en/research/rapidminer-lod-extension/rapid-miner-lod-extension-example-using-kernels-for-feature-generation/>.

Dependencies
------------
Mustard depends on the `nodes` graph library which is part of the Data2Semantics github. Furtermore, it depends on the SESAME triplestore (<http://rdf4j.org/>) and `mustard-learners` depends on the Java version of LibLINEAR (<https://github.com/bwaldvogel/liblinear-java/>).

All the 4 projects are congfigured using Maven, which takes care of the dependencies. The first 2 projects (`mustard` and `mustard-learners`) are submodules of one maven parent project (see the `pom.xml` in the root dir of this project). If you want to use them without Maven then you should make sure that the correct JAR's are on the classpath.

If you want to use `mustard` and `mustard-learners` (or the `nodes` project) as Maven artifacts, then this can easily be achieved using the excellent JitPack service (<https://jitpack.io/>). Please see their documentation on multi module maven projects.


JWS paper
---------
This library was used for the Journal of Web Semantics 2015 paper: [“Substructure Counting Graph Kernels for Machine Learning from RDF data”, GKD de Vries, S de Rooij](http://www.sciencedirect.com/science/article/pii/S1570826815000657).


We used the following datasets in our experiments:
  - The AIFB research portal (<http://figshare.com/articles/AIFB_DataSet/745364>)
  - The British Geological Survey (<http://figshare.com/articles/British_Geological_Survey/745365>)
  - Amsterdam Museum (<http://datahub.io/dataset/amsterdam-museum-as-edm-lod>)
  - DL Learner Mutagenic (<http://dl-learner.org/community/carcinogenesis/>)


Most of the experiments were run on a computing cluster. Using the `org.data2semantics.mustard.experiments.cluster.LocalExecutor.java` class the experiments can be rerun on a single machine. For the small experiments different settings are given as String constants in the top of the class. To rerun the large experiments, parameter files and susbsets of the datasets have to be created with `ParamsCreator.java` and `SubSetCreator.java`. Both of these are configured with some parameter settings at the top of the main() methods.

The computation time experiments can be found in `org.data2semantics.mustard.experiments.JWS2015`. In the top of the file a String constant sets the location of the dataset.
