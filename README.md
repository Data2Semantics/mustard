mustard - Machine learning Using Svms To Analyse Rdf Data
=======

Mustard is a machine learning library for learning from RDF data using kernel methods. Currently the library itself supports Support Vector Machines via the Java versions of the LibSVM and LibLINEAR libraries.

This repository consists of 4 projects:

- `mustard`, this is the heart of the library. It contains the graph kernels for RDF and the additional classes needed to handle RDF. This part of the library is the most well maintained and documented.
- `mustard-learners` wraps LibSVM and LibLINEAR for use with the kernels defined in `mustard`.
- `mustard-experiments` contains experiments and utility classes for experimenting with `mustard`.
- `mustard-ducktape-experiments` contains classes to perform experiments with `mustard` using `ducktape`, which is also located in de Data2Semantics github. This part of the library is hardly maintained at the moment.

Mustard depends on the `nodes` graph library which is part of the Data2Semantics github.

The mustard library is work in progress, but is getting closer to a release.
