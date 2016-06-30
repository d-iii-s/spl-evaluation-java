/*
 * Copyright 2015 Charles University in Prague
 * Copyright 2015 Vojtech Horky
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.cuni.mff.d3s.spl.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cz.cuni.mff.d3s.spl.*;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunBuilder;
import cz.cuni.mff.d3s.spl.data.DataSnapshotBuilder;
import cz.cuni.mff.d3s.spl.data.readers.LineOrientedReader;
import cz.cuni.mff.d3s.spl.interpretation.DistributionLearningInterpretationParallel;
import cz.cuni.mff.d3s.spl.interpretation.WelchTestInterpretation;
import cz.cuni.mff.d3s.spl.utils.ArrayUtils;

import javax.activation.FileDataSource;

public class SensitivityComparison {
	private static enum ComparisonOperator {
		LT, EQ, GT, ERR;
		
		public static ComparisonOperator fromString(String op) {
			if (op.equals("=")) {
				return EQ;
			} else if (op.equals("<")) {
				return LT;
			} else if (op.equals(">")) {
				return GT;
			} else {
				return ERR;
			}
		}
	}

	private static interface StatisticalTest {
	    
		/** Return test result for multiple confidence levels at the same time.
		 * 
		 * Given two data snapshots, a comparison operator and a list of confidence values,
		 * the method tells which of the confidence levels would require rejecting the comparison.
		 * 
		 * @param left Left data snapshot.
		 * @param op Comparison operator.
		 * @param right Right data snapshot.
		 * @param alphas Confidence levels to consider.
		 * @return Boolean array, a position is true if the test would reject at the corresponding confidence level.
		 */
		public boolean[] getRejects(DataSnapshot left, ComparisonOperator op,
				DataSnapshot right, double[] alphas);

		public String getName(double alpha);
	}

	private static abstract class GenericStatisticalTest implements	StatisticalTest {
		protected Interpretation interpretation;

		@Override
		public boolean[] getRejects(DataSnapshot left, ComparisonOperator op, DataSnapshot right, double[] alphas) {

			ComparisonResult result = interpretation.compare(left, right);
			double t = result.getStatistic();

			boolean[] rejects = new boolean[alphas.length];
			
			for (int i = 0; i < alphas.length; i++) {
				switch (op) {
				case EQ:
					rejects[i] = (t > result.getCriticalValue(1 - alphas[i] / 2))
						|| (t < result.getCriticalValue(alphas[i] / 2));
					break;
				case GT:
					rejects[i] = t < result.getCriticalValue(alphas[i]);
					break;
				case LT:
					rejects[i] = t > result.getCriticalValue(1 - alphas[i]);
					break;
				default:
					assert false;
				}

			}

			return rejects;
		}
	}

	private static class TTest extends GenericStatisticalTest {
		public TTest() {
			interpretation = new WelchTestInterpretation();
		}

		@Override
		public String getName(double alpha) {
			return String.format("T.%04.2f", alpha);
		}
	}
	
	private static class LearningTest extends GenericStatisticalTest {
		public LearningTest(ExecutorService executor, boolean beFast) {
			if (beFast) {
				interpretation = DistributionLearningInterpretationParallel.getFast(executor);
			} else {
				interpretation = DistributionLearningInterpretationParallel.getReasonable(executor);
			}
		}

		@Override
		public String getName(double alpha) {
			return String.format("L.%04.2f", alpha);
		}
	}
	
	private static class TolerantTest implements StatisticalTest {
		private final StatisticalTest actualTest;
		private final double tolerancy;
		
		public TolerantTest(StatisticalTest inner, double tol) {
			actualTest = inner;
			tolerancy = tol;
		}
		
		@Override
		public boolean[] getRejects(DataSnapshot left, ComparisonOperator op,
				DataSnapshot right, double[] alphas) {
			
			switch (op) {
			case EQ: {
				boolean[] lt = actualTest.getRejects(left, ComparisonOperator.LT, fixData(right, tolerancy), alphas);
				boolean[] gt = actualTest.getRejects(fixData(left, tolerancy), ComparisonOperator.GT, right, alphas);
				for (int i = 0; i < lt.length; i++) {
					lt[i] = lt[i] || gt[i];
				}
				return lt;
			}
			case GT:
				return actualTest.getRejects(fixData(left, tolerancy), op, right, alphas);
			case LT:
				return actualTest.getRejects(left, op, fixData(right, tolerancy), alphas);
			default:
				assert false;
				return null;
			}
		}
		
		private DataSnapshot fixData(DataSnapshot data, double coef) {
			DataSnapshotBuilder snapshotBuilder = new DataSnapshotBuilder();
			
			for (BenchmarkRun run : data.getRuns()) {
				BenchmarkRunBuilder builder = new BenchmarkRunBuilder();
				
				for (long sample : run.getSamples()) {
					builder.addSamples(Math.round((double)sample * coef));
				}
				
				snapshotBuilder.addRun(builder.create());
			}
			
			return snapshotBuilder.create();
		}

		@Override
		public String getName(double alpha) {
			return String.format("tl%04.2f.%s", tolerancy, actualTest.getName(alpha));
		}
		
	}

	private static class SimpleComparison {
		public final String name;
		public final DataSnapshot left;
		public final DataSnapshot right;
		public final ComparisonOperator op;
		private final Map<String, int[]> results;

		public SimpleComparison(String n, DataSnapshot l, ComparisonOperator o,
				DataSnapshot r) {
			name = n;
			left = l;
			right = r;
			op = o;
			results = new HashMap<>();
		}

		/** Accumulate test result statistics.
		 * 
		 * Each test execution (test name, current subset sizes, history subset sizes) delivers a single boolean result.
		 * The results are aggregated for all executions of the same test.
		 *  
		 * @param setting Unique identification of the test including test settings.
		 * @param rejected Boolean test result to record.
		 */
		public void addResult(String setting, boolean rejected) {
			int index = rejected ? 1 : 0;
			synchronized (results) {
				int[] counts = results.get(setting);
				if (counts == null) {
					counts = new int[] { 0, 0 };
				}
				counts[index]++;
				results.put(setting, counts);
			}
		}

		public String getResult(String setting) {
			int[] answer = null;
			synchronized (results) {
				answer = results.get (setting);
				if (answer != null) answer = Arrays.copyOf(answer, answer.length);
			}
			
			if (answer == null) return "???";
			
			double percent = (double) answer[1] / (answer[0] + answer[1]);
			String result = String.format("%6.4f (%8d:%8d)", percent, answer[0], answer[1]);
			
			return result;
		}
	}

	private static class Evaluator implements Runnable {
		private final SimpleComparison comparison;
		private final StatisticalTest test;
		private final double[] alphas;
		private final int leftLearnSubsetSize;
		private final int leftSubsetSize;
		private final int rightLearnSubsetSize;
		private final int rightSubsetSize;

		/** Runnable for single test execution.
		 *
		 * Given a comparison object (which includes the data snapshots and the comparison operator),
		 * the test to use and the subset sizes to use, the runnable executes a single test
		 * and records the results in the comparison object.
		 * 
		 * @param cmp Comparison object that provides comparison data and comparison operator and aggregates results.
		 * @param leftLearnSize How many runs to use as historical data on left side.
		 * @param leftSize How many runs to use as current data on left side.
		 * @param rightLearnSize How many runs to use as historical data on right side.
		 * @param rightSize How many runs to use as current data on right side.
		 * @param t What test to use.
		 * @param alfas What confidence levels to use.
		 */
		public Evaluator(SimpleComparison cmp, int leftLearnSize, int leftSize, int rightLearnSize, int rightSize,
		                 StatisticalTest t, double[] alfas) {
			comparison = cmp;
			test = t;
			alphas = alfas;
			leftLearnSubsetSize = leftLearnSize;
			leftSubsetSize = leftSize;
			rightLearnSubsetSize = rightLearnSize;
			rightSubsetSize = rightSize;
		}

		@Override
		public void run() {
		    	// Generate random subsets of both current and historical data to use for comparison.
			DataSnapshot left = getSubset(comparison.left, leftSubsetSize,
					getSubset(comparison.left, leftLearnSubsetSize, null));
			DataSnapshot right = getSubset(comparison.right, rightSubsetSize,
					getSubset(comparison.right, rightLearnSubsetSize, null));

			// Get a list of results for a range of confidence levels.
			// Accumulate the results in the associated comparison object.
			boolean[] result = test.getRejects(left, comparison.op, right, alphas);
			for (int i = 0; i < result.length; i++) {
				String name = test.getName(alphas[i]);
				String resultId = String.format("%s.%d.%d.%d.%d", name, leftLearnSubsetSize, leftSubsetSize,
						rightLearnSubsetSize, rightSubsetSize);
				comparison.addResult(resultId, result[i]);
			}
		}

		/** Create a data snapshot from a random subset of runs of another data snapshot.
		 * 
		 * @param data Source data snapshot.
		 * @param size How many runs to use.
		 * @param prev Historical data snapshot to use.
		 * @return New data snapshot that is a subset of the source.
		 */
		private DataSnapshot getSubset(DataSnapshot data, int size, DataSnapshot prev) {
			DataSnapshotBuilder builder = new DataSnapshotBuilder();
			int[] indexes = getRandomIndexes(data.getRunCount(), size);
			for (int run = 0; run < indexes.length; run++) {
				builder.addRun(data.getRun(indexes[run]));
			}
			builder.setPreviousEpoch(prev);
			return builder.create();
		}

		/** Return an array of random indexes from given range without replacement.
		 * 
		 * Because sampling is without replacement we expect wantedSize <= origSize.
		 * 
		 * @param origSize Only indexes smaller than this are available.
		 * @param wantedSize How many indexes to select.
		 * @return Unique random indexes from given range.
		 */
		private int[] getRandomIndexes(int origSize, int wantedSize) {
			if (wantedSize > origSize) {
				wantedSize = origSize;
			}
			ArrayList<Integer> indexes = new ArrayList<>(origSize);
			for (int i = 0; i < origSize; i++) {
				indexes.add(i);
			}
			Collections.shuffle(indexes);
			int[] result = new int[wantedSize];
			for (int i = 0; i < wantedSize; i++) {
				result[i] = indexes.get(i);
			}
			return result;
		}
	}

	private static class JobCounterDecorator implements Runnable {
		private Runnable inner;

		private static Integer jobsGuard = 0;
		private static int jobsTotal = 0;
		private static int jobsDone = 0;

		private static void announceNewJob() {
			synchronized (jobsGuard) {
				jobsTotal++;
			}
		}

		private static void announceFinishJob() {
			synchronized (jobsGuard) {
				jobsDone++;
			}
		}

		public static void print() {
			synchronized (jobsGuard) {
				System.err.printf("\r# %d jobs done (%d planned so far).",
						jobsDone, jobsTotal);
			}
		}

		public JobCounterDecorator(Runnable actual) {
			inner = actual;
			announceNewJob();
		}

		@Override
		public void run() {
			inner.run();
			announceFinishJob();
		}
	}

	public static void main(String[] args) throws IOException {
		int cpuCount = Runtime.getRuntime().availableProcessors();
		Collection<Double> alphasCol = new ArrayList<>();
		Collection<int[]> subsets = new ArrayList<>();
		Collection<Double> tolerancies = new ArrayList<>();
		int parallelJobs = cpuCount * 2;
		int repeats = 10;
		boolean verbose = false;
		boolean demo = false;
		boolean learningTestBeFast = false;
		double skip = 0;
		
		// Process command line arguments.
		for (int i = 0 ; i < args.length ; i ++) {
			if (args[i].equals("--help")) {
				System.out.printf("Usage: java -cp ... %s [opts]\n", SensitivityComparison.class.getName());
				System.out.println("where [opts] is a combination of ([mult] .. option can be repeated):");
				System.out.println(" --help                Print this help.");
				System.out.println(" --verbose             Print what the program does (including progress bar).");
				System.out.println(" --subset HL:CL:HR:CR  Set historical and current run count on left and right side.");
				System.out.println(" --repeats N           Number of loops for each test.");
				System.out.println(" --jobs N              Number of parallel jobs (defaults to CPU*2)");
				System.out.println(" --jobs xN             Number of parallel jobs (multiply of CPU count)");
				System.out.println(" --alpha A             Significance level [mult].");
				System.out.println(" --skip N              Skip first N percent of samples from each file.");
				System.out.println(" --tolerance X         Extra tolerance for the tests [mult].");
				System.out.println(" --fast                Use faster (but less precise) implementation.");
				System.out.println(" --demo                Run on prepackaged data only.");
				System.out.println("When --demo is not specified, reads formula specifications from stdin.");
				System.out.println("Each line has format 'name ### files.left = files.right'.");
				System.out.println("(No expansion of wildcards is done.)");
				System.exit(0);
			} else if (args[i].equals("--subset")) {
				String[] sizesStr = args[i+1].split(":");
				if (sizesStr.length != 4) {
					System.err.println("--subset expects 4 integers, colon separated");
					System.exit(1);
				}
				int[] sizes = new int[4];
				for (int j = 0; j < sizes.length; j++) {
					sizes[j] = Integer.parseInt(sizesStr[j]);
				}
				subsets.add(sizes);
				i++;
			} else if (args[i].equals("--repeats")) {
				repeats = Integer.parseInt(args[i+1]);
				i++;
			} else if (args[i].equals("--skip")) {
				skip = Double.parseDouble(args[i+1]);
				if (skip < 0.0 || skip > 1.0) {
				    	System.err.println("--skip expects double from 0 to 1");
				    	System.exit(1);
				}
				i++;
			} else if (args[i].equals("--jobs")) {
				if (args[i+1].startsWith("x")) {
					parallelJobs = cpuCount * Integer.parseInt(args[i+1].substring(1));
				} else {
					parallelJobs = Integer.parseInt(args[i+1]);
				}
				i++;
			} else if (args[i].equals("--alpha")) {
				double alpha = Double.parseDouble(args[i+1]);
				alphasCol.add(alpha);
				i++;
			} else if (args[i].equals("--tolerance")) {
				double tol = Double.parseDouble(args[i+1]);
				tolerancies.add(tol);
				i++;
			} else if (args[i].equals("--fast")) {
				learningTestBeFast = true;
			} else if (args[i].equals("--verbose")) {
				verbose = true;
			} else if (args[i].equals("--demo")) {
				demo = true;
			} else {
				System.err.println("Unknown command-line option.");
				System.exit(1);
			}
		}
		
		
		// Set up the defaults.
		if (alphasCol.isEmpty()) alphasCol.add(0.05);
		if (subsets.isEmpty()) subsets.add(new int[] {10, 1, 10, 1});
		
		// Set up the executors.
		ExecutorService testInternalExecutor = Executors.newCachedThreadPool();
		ExecutorService mainExecutor = Executors.newFixedThreadPool(parallelJobs);
		
		// Prepare the tests that will be run.
		Collection<StatisticalTest> tests = new ArrayList<>();
		tests.add(new TTest());
		tests.add(new LearningTest(testInternalExecutor, learningTestBeFast));
		for (double tol : tolerancies) {
			tests.add(new TolerantTest(new TTest(), tol));
			tests.add(new TolerantTest(new LearningTest(testInternalExecutor, learningTestBeFast), tol));
		};		
		
		// Read the comparisons on which we ought to test.
		Reader input = null;
		if (demo) {
			input = new StringReader(
					String.format("x = x ### %s = %s",
							getSensitivityDataFilenames(),
							getSensitivityDataFilenames()));
		} else {
			input = new InputStreamReader(System.in);
		}
		

		Collection<SimpleComparison> comparisons = new LinkedList<>();
		BufferedReader bufferedInput = new BufferedReader(input);
		while (true) {
			String line = bufferedInput.readLine();
			if (line == null) break;
			
			String[] parts = line.split("[ \t]*###[ \t]*");
			if (parts.length != 2) {
				System.err.printf("Ignoring line %s.\n", line);
				continue;
			}
			String[] filenames = parts[1].split("[ \t]+");
			
			Collection<File> leftFiles = new ArrayList<>();
			Collection<File> rightFiles = new ArrayList<>();
			Map<String, DataSource> leftSource = null;
			Map<String, DataSource> rightSource = null;
			ComparisonOperator cmpOp = null;
			int cmpOpCount = 0;

			// Scan the list of files.
			// Files left of operator form the left source.
			// Files right of operator form the right source.
			System.err.printf ("# Total %d filenames.\n", filenames.length);
			for (String filename : filenames) {
			    ComparisonOperator op = ComparisonOperator.fromString(filename);
			    if (op == ComparisonOperator.ERR) {
				if (cmpOp == null) leftFiles.add(new File(filename));
				else rightFiles.add(new File(filename));
			    } else {
				cmpOp = op;
				cmpOpCount ++;
			    }
			}
			// There should be exactly one operator.
			if (cmpOpCount != 1) {
			    System.err.printf("There are %d operators on line %s.\n", cmpOpCount, line);
			    continue;
			}

			DataReader reader = new LineOrientedReader();
			try {
				leftSource = reader.readRevision(leftFiles.toArray(new File[leftFiles.size()]));
				rightSource = reader.readRevision(rightFiles.toArray(new File[rightFiles.size()]));
			} catch (DataReader.ReaderException e) {
				e.printStackTrace();
			}

			DataSnapshot left = leftSource.get("default").makeSnapshot(skip);
			DataSnapshot right = rightSource.get("default").makeSnapshot(skip);
			
			if ((left.getRunCount() == 0) || (right.getRunCount() == 0)) {
				System.err.printf("There are no sources on %s.\n", line);
				continue;
			}
			
			SimpleComparison cmp = new SimpleComparison(parts[0], left, cmpOp, right);
			comparisons.add(cmp);
		}
		bufferedInput.close();
		
		/* Convert to array for simpler use. */
		double[] alphas = ArrayUtils.makeArray(alphasCol);
		
		/*
		 * Let's go :-)
		 */
		Collection<Future<Integer>> jobs = new ArrayList<>();

		if (verbose) {
			System.out.printf("# Expects %d jobs (%d * %d * %d * %d).\n",
				comparisons.size() * tests.size() * repeats * subsets.size(),
				comparisons.size(), tests.size(), subsets.size(), repeats);
			System.out.printf("# Using %d parallel jobs.\n", parallelJobs);
		}
		
		long startTime = System.nanoTime();
		
		for (SimpleComparison comparison : comparisons) {
			for (StatisticalTest test : tests) {
				for (int[] subset : subsets) {
					Evaluator eval = new Evaluator(comparison, subset[0], subset[1], subset[2], subset[3], test, alphas);
					for (int i = 0; i < repeats; i++) {
						jobs.add(mainExecutor.submit(new JobCounterDecorator(eval), 0));
						if (verbose) {
							JobCounterDecorator.print();
						}
					}
				}
			}
		}

		while (true) {
			boolean allDone = true;
			for (Future<Integer> job : jobs) {
				if (!job.isDone()) {
					allDone = false;
				}
			}
			if (verbose) {
				JobCounterDecorator.print();
			}
			if (allDone) {
				if (verbose) {
					System.err.println();
				}
				break;
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
		}

		long endTime = System.nanoTime();
		
		if (verbose) {
			long timeDiff = endTime - startTime;
			long timeDiffMillis = timeDiff / 1000 / 1000;
			long timeDiffSec = timeDiffMillis / 1000;
			long timeDiffMillisPerJob;
			if (jobs.size() == 0) {
				timeDiffMillisPerJob = 0;
			} else {
				timeDiffMillisPerJob = timeDiffMillis / jobs.size();
			}
			System.out.printf("# Took %ds for %d jobs (about %dms per job).\n", timeDiffSec, jobs.size(),
					timeDiffMillisPerJob);
		}
		
		System.out.printf("%-55s", "");
		for (StatisticalTest test : tests) {
			for (double alpha : alphasCol) {
				String name = test.getName(alpha);
				System.out.printf("%30s", name);
			}
		}
		System.out.println();
		
		for (SimpleComparison comparison : comparisons) {
			for (int[] subset : subsets) {
				System.out.printf("%-55s", String.format("%s %2d:%2d [%2d:%2d]", comparison.name, subset[0],
						subset[2], subset[1], subset[3]));
				for (StatisticalTest test : tests) {
					for (double alpha : alphasCol) {
						String name = String.format("%s.%d.%d.%d.%d", test.getName(alpha), subset[0], subset[1],
								subset[2], subset[3]);
						System.out.printf("%30s", comparison.getResult(name));
					}
				}
				System.out.println();
			}
		}

		mainExecutor.shutdown();
		testInternalExecutor.shutdown();
	}
	
	private static String getSensitivityDataFilenames() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < 58; i++) {
			result.append(String.format(" data/sensitivity/%02d.dat", i));
		}
		return result.toString();
	}
}
