# Stochastic Performance Logic evaluation engine

Stochastice Performance Logic is a formalism for capturing performance
assumptions.
It is, for example, possible to capture assumption that newer version of
a function `bar` is faster than the previous version or that library
`foobar` is faster than library `barfoo` when rendering antialiased text.

This repository contains an implementation for evaluation of the SPL
formulas.
That is, you would use this library when you have your performance measured
(or modeled) and you need to evaluate whether your assumptions are correct.

## Requirements

To compile and run the framework, the following software has to be available
on your machine.

* Java SDK >= 1.7.0
* Apache Ant

## Compilation

Simply execute

	ant

to compile the library.
The attached tests can be compiled and executed by running

	ant test

All the generated files (classes, test reports) are stored in the `out/`
directory.



## Documentation...

... is not very detailed at the moment.

Running `ant refdoc` will generate JavaDoc documentation with basic information
on how to use this evaluation engine (see `out/javadoc/index.html`).

SPL was originally created for performance unit testing, more information
is available at [SPL for Java page](http://d3s.mff.cuni.cz/software/spl-java)
at [Department of Distributed and Dependable Systems](http://d3s.mff.cuni.cz/)
([Faculty of Mathematics and Physics, Charles University in Prague](http://mff.cuni.cz/)).

