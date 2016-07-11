package cz.cuni.mff.d3s.spl.demo;

import cz.cuni.mff.d3s.spl.data.readers.RevisionReader;
import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.Formula;
import cz.cuni.mff.d3s.spl.Result;
import cz.cuni.mff.d3s.spl.data.readers.RawJsonRevisionReader;
import cz.cuni.mff.d3s.spl.formula.SplFormula;
import cz.cuni.mff.d3s.spl.interpretation.WelchTestInterpretation;

import java.io.File;
import java.util.*;


public class JmhRawDataTester {
	private static final double SIGNIFICANCE_LEVEL = 0.95;

	private static class Revision {
		public String name;
		public DataSource data;

		public Revision(String n, DataSource d) {
			name = n;
			data = d;
		}
	}

	private static class FileComparator implements Comparator<File> {
		@Override
		public int compare(File x, File y) {
			long xModified = x.lastModified();
			long yModified = y.lastModified();

			if (xModified == yModified) {
				return 0;
			} else if (xModified > yModified) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	public static void main(String[] directory) {
		if (directory.length != 1) {
			printUsage();
			System.exit(1);
		}

		// name of benchmarked method and list of data for all revisions
		Map<String, List<Revision>> data = new HashMap<>();

		File dir = new File(directory[0]);
		File[] files = dir.listFiles();
		Arrays.sort(files, new FileComparator());

		for (File file : files) {
			System.out.printf("Reading data from %s revision...", file.getName());

			RevisionReader reader = new RawJsonRevisionReader();
			Map<String, DataSource> revisionData = null;
			try {
				revisionData = reader.readRevision(file);
			} catch (RevisionReader.ReaderException e) {
				e.printStackTrace();
				System.exit(2);
			}

			for (Map.Entry<String, DataSource> benchmark : revisionData.entrySet()) {
				if (!data.containsKey(benchmark.getKey())) {
					data.put(benchmark.getKey(), new LinkedList<>());
				}
				data.get(benchmark.getKey()).add(new Revision(file.getName(), benchmark.getValue()));
			}

			System.out.println(" ok");
		}

		Formula improved = SplFormula.create("new < old");
		improved.setInterpretation(new WelchTestInterpretation());

		System.out.printf("Looking for regressions and suspicious data...\n");

		// for each test method
		for (Map.Entry<String, List<Revision>> benchmark : data.entrySet()) {
			System.out.printf(" benchmark: %s...\n", benchmark.getKey());

			// for each pair of following measurements check regression
			List<Revision> revisions = benchmark.getValue();
			for (int i = 0; i < revisions.size() - 1; i++) {
				improved.bind("old", revisions.get(i).data);
				improved.bind("new", revisions.get(i + 1).data);

				Result result = improved.evaluate(SIGNIFICANCE_LEVEL);
				if (result != Result.TRUE) {
					System.out.printf("  - possible regression between %s and %s!\n",
							revisions.get(i).name, revisions.get(i + 1).name);
				}
			}
		}
	}

	private static void printUsage() {
		final String selfName = JmhRawDataTester.class.getName();
		System.out.printf("Usage: java [jvm-opts] %s directory name\n", selfName);
		System.out.println(" Directory should contain JSON files from JMH framework");
		System.out.println(" with name as revision (version) identifier. Files will");
		System.out.println(" be ordered by creation time.");
	}
}
