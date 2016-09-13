package cz.cuni.mff.d3s.spl;


import cz.cuni.mff.d3s.spl.data.Revision;
import cz.cuni.mff.d3s.spl.data.readers.JmhJsonRevisionReader;
import cz.cuni.mff.d3s.spl.data.readers.StructuredDataReader;
import cz.cuni.mff.d3s.spl.formula.SplFormula;
import cz.cuni.mff.d3s.spl.interpretation.WelchTestInterpretation;
import org.apache.commons.cli.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class Main {
	private static final double SIGNIFICANCE_LEVEL = 0.95;

	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = createOptions();

		try {
			CommandLine line = parser.parse(options, args);

			String jarFormulas = line.getOptionValue("jar-formulas");
			String fileFormulas = line.getOptionValue("file-formulas");
			String[] cmdFormulas = line.getOptionValues("commandline-formulas");
			String dataDir = line.getOptionValue("data-dir");
			String revisionMapping = line.getOptionValue("revision-mapping");
			boolean printUnknownOnly = line.hasOption("print-unknown");

			if (jarFormulas == null && fileFormulas == null && cmdFormulas == null) {
				System.err.println("You must specify one of -j, -f, -c options.");
				printHelp(options);
				return;
			}

			// Formulas are processed in order jar, file and command line.
			// Latter options have higher priority and will override previous values.
			// Pair of FQ benchmark name and corresponding SPL formula.
			Map<String, String> formulas = new HashMap<>();
			readJarFormulas(formulas, jarFormulas);
			readFileFormulas(formulas, fileFormulas);
			readCommandlineFormulas(formulas, cmdFormulas);

			// Get custom mapping of revisions form file.
			Map<String, String> customRevisionMap = getCustomRevisionMapping(revisionMapping);

			DataReader reader = new StructuredDataReader<>(new JmhJsonRevisionReader.RevisionFactory());
			Map<String, List<Revision>> data = reader.readData(new String[] {dataDir});

			for (Map.Entry<String, List<Revision>> benchmarkItem : data.entrySet()) {
				String benchmarkName = benchmarkItem.getKey();
				String formulaString = null;

				if (formulas.containsKey("*")) {
					formulaString = formulas.get("*");
				}
				if (formulas.containsKey(benchmarkName)) {
					formulaString = formulas.get(benchmarkName);
				}

				if (formulaString == null) {
					System.out.println("Skipping benchmark without formula: " + benchmarkItem.getKey());
					continue;
				}

				Formula formula = SplFormula.create(formulaString);
				formula.setInterpretation(new WelchTestInterpretation());

				// get benchmark's revisions in better format for us
				Map<String, DataSource> revisionMap = getRevisionMap(benchmarkItem.getValue());
				// Bind variables to formula (according to revisions collection)
				for (String variable : formula.getVariables()) {
					// try if there is real revision of that name
					if (revisionMap.containsKey(variable)) {
						formula.bind(variable, revisionMap.get(variable));
					}
					// else try to find another revision is custom mapping
					else if (customRevisionMap.containsKey(variable)) {
						formula.bind(variable, revisionMap.get(customRevisionMap.get(variable)));
					}
					// else there is no such revision
					else {
						System.out.printf("Benchmark: %s, unknown version: %s\n", benchmarkName, variable);
					}
				}

				// if we want only get missing versions, skip formula evaluating
				if (!printUnknownOnly) {
					Result result = formula.evaluate(SIGNIFICANCE_LEVEL);
					System.out.printf("Benchmark: %s, formula: %s, result: %s\n", benchmarkName, formulaString, result);
				}
			}
		} catch (ParseException e) {
			// oops, something went wrong
			System.err.println("Parsing failed. Reason: " + e.getMessage());
			printHelp(options);
		} catch (DataReader.ReaderException e) {
			System.err.println("Cannot read measured data. Reason: " + e.getMessage());
		}
	}

	private static Map<String, DataSource> getRevisionMap(List<Revision> revisions) {
		Map<String, DataSource> result = new HashMap<>();
		for (Revision rev : revisions) {
			result.put(rev.name, rev.data);
		}
		return result;
	}

	private static void readJarFormulas(Map<String, String> formulas, String jarFormulas) {
		if (jarFormulas != null) {
			try {
				File jarFile = new File(jarFormulas);
				URL url = new URL("jar:file:" + jarFile.getAbsolutePath() + "!/META-INF/SPLFormulas");
				InputStream is = url.openStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String line;
				List<String> lines = new ArrayList<>();
				while ((line = reader.readLine()) != null) {
					lines.add(line);
				}
				parseStringsIntoMap(lines, formulas);
			} catch (IOException | NullPointerException e) {
				System.err.println("Cannot read SPL formulas from JAR, assuming no formulas.");
			}
		}
	}

	private static void readFileFormulas(Map<String, String> formulas, String fileFormulas) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(fileFormulas), StandardCharsets.UTF_8);
			parseStringsIntoMap(lines, formulas);
		} catch (IOException e) {
			System.err.println("Cannot read SPL formulas from file, assuming empty one.");
		} catch (NullPointerException e) {
			// there is no such formula file
		}
	}

	private static void readCommandlineFormulas(Map<String, String> formulas, String[] cmdFormulas) {
		if (cmdFormulas != null) {
			parseStringsIntoMap(cmdFormulas, formulas);
		}
	}

	private static void parseStringsIntoMap(String[] data, Map<String, String> output) {
		for (String line : data) {
			// skip empty lines and comments
			if (line.equals("") || line.startsWith("#")) {
				continue;
			}
			String[] parsed = line.split(":");
			if (parsed.length == 2) {
				// save only well formed lines
				output.put(parsed[0], parsed[1]);
			} else {
				System.err.println("Malformed line '" + line + "'. Exactly one ':' expected.");
			}
		}
	}

	private static void parseStringsIntoMap(Collection<String> data, Map<String, String> output) {
		parseStringsIntoMap(data.toArray(new String[] {}), output);
	}

	private static Map<String, String> getCustomRevisionMapping(String revisionsMappingFile) {
		List<String> lines;
		try {
			lines = Files.readAllLines(Paths.get(revisionsMappingFile), StandardCharsets.UTF_8);
			Map<String, String> result = new HashMap<>();
			parseStringsIntoMap(lines, result);
			return result;
		} catch (IOException e) {
			System.err.println("Cannot read mapping file, assuming empty one.");
		} catch (NullPointerException e) {
			// there is no such mapping file, return empty mapping
		}
		return new HashMap<>();
	}

	private static void printHelp(Options options) {
		String header = "Evaluate measured data against SPL formulas.";
		String footer = "";
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("spl-evaluation-java", header, options, footer, true);
	}

	private static Options createOptions() {
		Options options = new Options();

		options.addOption(Option.builder("j")
				.longOpt("jar-formulas")
				.hasArg()
				.argName("benchmarks.jar")
				.desc("Read SPL formulas from JAR file.")
				.build()
		);

		options.addOption(Option.builder("f")
				.longOpt("file-formulas")
				.hasArg()
				.argName("formula_file")
				.desc("Read SPL formulas from text file.")
				.build()
		);

		options.addOption(Option.builder("c")
				.longOpt("commandline-formulas")
				.hasArgs()
				.argName("formulas")
				.desc("Read SPL formulas from commandline arguments.")
				.build()
		);

		options.addOption(Option.builder("d")
				.longOpt("data-dir")
				.hasArg()
				.argName("data_directory")
				.required()
				.desc("Path to directory with measured data.")
				.build()
		);

		options.addOption(Option.builder("r")
				.longOpt("revision-mapping")
				.hasArg()
				.argName("mapping_file")
				.desc("Mapping of formula revisions to file names.")
				.build()
		);

		options.addOption(Option.builder("p")
				.longOpt("print-unknown")
				.desc("Print unknown revisions and exit.")
				.build()
		);

		return options;
	}
}
