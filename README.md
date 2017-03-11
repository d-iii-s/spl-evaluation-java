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

```{.sh}
$ ant
```

or

```{.sh}
$ ant compile
```

to compile the library. For creating `.jar` package run

```{.sh}
$ ant package
```

Resulting file is `out/jars/spl-evaluation-java.jar`.

All the generated files (classes, test reports) are stored in the `out/`
directory.


## Usage

There are several command-line options. Quick help is printed on error (for example when no argument is given).

```
usage: spl-evaluation-java [-c <formulas>] -d <data_directory> [-f
       <formula_file>] [-j <benchmarks.jar>] [-p] [-r <mapping_file>]
Evaluate measured data against SPL formulas.
 -c,--commandline-formulas <formulas>   Read SPL formulas from commandline
                                        arguments.
 -d,--data-dir <data_directory>         Path to directory with measured
                                        data.
 -f,--file-formulas <formula_file>      Read SPL formulas from text file.
 -j,--jar-formulas <benchmarks.jar>     Read SPL formulas from JAR file.
 -p,--print-unknown                     Print unknown revisions and exit.
 -r,--revision-mapping <mapping_file>   Mapping of formula revisions to
                                        file names.
```

SPL formulas can be specified in 3 ways - inside `benchmarks.jar`, in text file or directly on command-line.
Formulas are parsed in this order, latter has higher priority and will override previous formulas.

- `benchmarks.jar` - formulas are saved in META-INF/SPLFormulas file formatted as `benchmark_name:formula`.
  This file is generated via corresponding maven plugin (jmh_spl-maven-plugin) from `@SPLFormula` annotations.
- text file - simple text file formatted as `benchmark_name:formula`
- command-line arguments - list of values `benchmark_name:formula` separated by space. Note that enclosing each
  value into `"` symbols may be required because of shell argument parsing.
  
**print-unknown** option prints versions, that are mentioned in SPL formulas but are not present in measured data
 or transitively in custom mapping of revisions. No evaluation is performed. Empty rows or rows starting with
 `#` are ignored.
 
**revision-mapping** is simple text file with `:` separated values of custom revision and actual measured data revision.
 Each line contains one mapping, empty rows or rows starting with `#` are ignored.
 
### Example

```{.sh}
$ java -jar spl-evaluation-java.jar -d ./demo-data/jmh -r /tmp/mapping.txt -j ./target/benchmarks.jar -c "cz.stdin.ps.MyBenchmark.testMethod:last < ver2" "cz.stdin.ps.MyBenchmark.methodTwo:ver1 < base"
```


## Tests

The attached unit tests can be compiled and executed by running

```{.sh}
$ ant test
```

Test report will be saved to `out/test-results/html/index.html`. There are 4
failing test now because of not implemented features.


## Documentation...

... is not very detailed at the moment.

Running `ant refdoc` will generate JavaDoc documentation with basic information
on how to use this evaluation engine (see `out/javadoc/index.html`).

SPL was originally created for performance unit testing, more information
is available at [SPL for Java page](http://d3s.mff.cuni.cz/software/spl-java)
at [Department of Distributed and Dependable Systems](http://d3s.mff.cuni.cz/)
([Faculty of Mathematics and Physics, Charles University in Prague](http://mff.cuni.cz/)).

