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

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.ComparisonResult;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunBuilder;
import cz.cuni.mff.d3s.spl.data.DataSnapshotBuilder;
import cz.cuni.mff.d3s.spl.data.FileDataSource;
import cz.cuni.mff.d3s.spl.interpretation.DistributionLearningInterpretationParallel;
import cz.cuni.mff.d3s.spl.interpretation.WelchTestInterpretation;

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
		public boolean[] getRejects(DataSnapshot left, ComparisonOperator op,
				DataSnapshot right, double[] alphas);

		public String getName(double alpha);
	}

	private static abstract class GenericStatisticalTest implements
			StatisticalTest {
		protected Interpretation interpretation;

		@Override
		public boolean[] getRejects(DataSnapshot left, ComparisonOperator op,
				DataSnapshot right, double[] alphas) {

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
		public LearningTest(ExecutorService executor) {
			interpretation = DistributionLearningInterpretationParallel.getReasonable(executor);
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
				answer = results.get(setting);
				if (answer != null) {
					answer = Arrays.copyOf(answer, answer.length);
				}
			}
			if (answer == null) {
				return "???";
			}
			double percent = (double) answer[1] / (answer[0] + answer[1]);
			String result = String.format("%d,%d", answer[0], answer[1]);
			result = String.format("%6.4f", percent, answer[0], answer[1]);
			
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

		public Evaluator(SimpleComparison cmp, int leftLearnSize, int leftSize, int rightLearnSize, int rightSize, StatisticalTest t, double[] alfas) {
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
			DataSnapshot left = getSubset(comparison.left, leftSubsetSize, getSubset(comparison.left, leftLearnSubsetSize, null));
			DataSnapshot right = getSubset(comparison.right, rightSubsetSize, getSubset(comparison.right, rightLearnSubsetSize, null));

			boolean[] result = test.getRejects(left, comparison.op, right,
					alphas);
			for (int i = 0; i < result.length; i++) {
				String name = test.getName(alphas[i]);
				String resultId = String.format("%s.%d.%d.%d.%d", name, leftLearnSubsetSize, leftSubsetSize, rightLearnSubsetSize, rightSubsetSize);
				comparison.addResult(resultId, result[i]);
			}
		}

		private DataSnapshot getSubset(DataSnapshot data, int size, DataSnapshot prev) {
			DataSnapshotBuilder builder = new DataSnapshotBuilder();
			int[] indexes = getRandomIndexes(data.getRunCount(), size);
			for (int run = 0; run < indexes.length; run++) {
				builder.addRun(data.getRun(indexes[run]));
			}
			builder.setPreviousEpoch(prev);
			return builder.create();
		}

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
				System.out.printf("\r%d jobs done (%d planned so far).",
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

	public static void main(String[] directoryNames) throws IOException {
		int cpuCount = Runtime.getRuntime().availableProcessors();
		ExecutorService innerExecutor = Executors.newCachedThreadPool();
		ExecutorService executor = Executors.newFixedThreadPool(cpuCount * 2);
		System.out.printf("CPU count = %d\n", cpuCount);
		
		StatisticalTest[] tests = new StatisticalTest[] {
				new TTest(),
				new LearningTest(innerExecutor),
				new TolerantTest(new TTest(), 1.01),
				new TolerantTest(new LearningTest(innerExecutor), 1.01),
				new TolerantTest(new TTest(), 1.02),
				new TolerantTest(new LearningTest(innerExecutor), 1.02),
				new TolerantTest(new TTest(), 1.05),
				new TolerantTest(new LearningTest(innerExecutor), 1.05),
		};

		double[] alphas = new double[] { 0.05 };
		int[][] subsets = new int[][] {
			{ 10, 1, 10, 1 },
			{ 30, 1, 30, 1 },
			{ 30, 3, 30, 3 },
			{ 50, 1, 50, 1 },
			{ 50, 3, 50, 3 },
			{ 50, 10, 50, 10 },
		};

		int repeats = 5;
		
		Collection<SimpleComparison> comparisons = new LinkedList<>();

		// BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		BufferedReader input = new BufferedReader(new StringReader(
				String.format("x = x ### %s = %s",
						getSensitivityDataFilenames(),
						getSensitivityDataFilenames())));
		while (true) {
			String line = input.readLine();
			if (line == null) {
				break;
			}
			
			String[] parts = line.split("###");
			if (parts.length != 2) {
				System.err.printf("Ignoring line %s.\n", line);
				continue;
			}
			String[] filenames = parts[1].split("[ \t]");
			
			DataSource leftSource = null;
			DataSource rightSource = null;
			ComparisonOperator cmpOp = null;
			
			Collection<File> files = new ArrayList<>();
			boolean err = false;
			for (String filename : filenames) {
				ComparisonOperator op = ComparisonOperator.fromString(filename);
				if (op == ComparisonOperator.ERR) {
					files.add(new File(filename));
				} else {
					if (leftSource == null) {
						leftSource = FileDataSource.load(files);
						cmpOp = op;
					} else {
						System.err.printf("%s: too many operators on line %s.\n", filename, line);
						err = true;
						break;
					}
				}
			}
			if (err) {
				continue;
			}
			if (cmpOp == null) {
				System.err.printf("Operator missing on line %s.\n", line);
				continue;
			}
			rightSource = FileDataSource.load(files);
			
			DataSnapshot left = leftSource.makeSnapshot();
			DataSnapshot right = rightSource.makeSnapshot();
			
			if ((left.getRunCount() == 0) || (right.getRunCount() == 0)) {
				System.err.printf("No sources on %s.\n", line);
				continue;
			}
			
			SimpleComparison cmp = new SimpleComparison(parts[0], left, cmpOp, right);
			comparisons.add(cmp);
		}
		input.close();
		
		
		
		Collection<Future<Integer>> jobs = new ArrayList<>();

		System.out.printf("Expects %d jobs (%d * %d * %d * %d).\n",
				comparisons.size() * tests.length * repeats * subsets.length,
				comparisons.size(), tests.length, subsets.length, repeats);

		
		long startTime = System.nanoTime();
		
		for (SimpleComparison comparison : comparisons) {
			for (StatisticalTest test : tests) {
				for (int[] subset : subsets) {
					Evaluator eval = new Evaluator(comparison, subset[0], subset[1], subset[2], subset[3], test, alphas);
					for (int i = 0; i < repeats; i++) {
						jobs.add(executor.submit(new JobCounterDecorator(eval), 0));
						JobCounterDecorator.print();
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
			JobCounterDecorator.print();
			if (allDone) {
				System.out.println();
				break;
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
		}

		long endTime = System.nanoTime();
		
		long timeDiff = endTime - startTime;
		long timeDiffMillis = timeDiff / 1000 / 1000;
		long timeDiffSec = timeDiffMillis / 1000;
		long timeDiffMillisPerJob;
		if (jobs.size() == 0) {
			timeDiffMillisPerJob = 0;
		} else {
			timeDiffMillisPerJob = timeDiffMillis / jobs.size();
		}
		System.out.printf("It took %ds for %d jobs (about %dms per job).\n", timeDiffSec, jobs.size(), timeDiffMillisPerJob);
		
		System.out.printf("%-30s", "");
		for (StatisticalTest test : tests) {
			for (double alpha : alphas) {
				String name = test.getName(alpha);
				System.out.printf("%15s", name);
			}
		}
		System.out.println();
		
		for (SimpleComparison comparison : comparisons) {
			for (int[] subset : subsets) {
				System.out.printf("%-30s", String.format("%s %2d:%2d [%2d:%2d]", comparison.name, subset[0], subset[2], subset[1], subset[3]));
				for (StatisticalTest test : tests) {
					for (double alpha : alphas) {
						String name = String.format("%s.%d.%d.%d.%d", test.getName(alpha), subset[0], subset[1], subset[2], subset[3]);
						System.out.printf("%15s", comparison.getResult(name));
					}
				}
				System.out.println();
			}
		}

		innerExecutor.shutdown();
		executor.shutdown();
	}
	
	private static String getSensitivityDataFilenames() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < 58; i++) {
			result.append(String.format(" data/sensitivity/%02d.dat", i));
		}
		return result.toString();
	}
}
