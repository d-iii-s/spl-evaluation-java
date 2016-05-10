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
package cz.cuni.mff.d3s.spl.interpretation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.apache.commons.math3.distribution.RealDistribution;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.ComparisonResult;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunSummary;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunUtils;
import cz.cuni.mff.d3s.spl.utils.DistributionUtils;
import cz.cuni.mff.d3s.spl.utils.StatisticsUtils;


/** Parallel implementation of the DistributionLearningInterpretation.
 * 
 * <b>Warning</b>: current implementation requires at least 4 worker threads
 * to be available due to parallelism nesting.
 * 
 * @see DistributionLearningInterpretation
 */
public class DistributionLearningInterpretationParallel implements Interpretation {
	private final int bootstrapSizeInnerMeans;
	private final int bootstrapSizeOuterMeans;
	private final int diffDistributionSampleCount;
	private final ExecutorService executor;
	
	public DistributionLearningInterpretationParallel(ExecutorService executor) {
		this(executor, 1000, 10000, 100000);
	}
	
	public DistributionLearningInterpretationParallel() {
		this(new ForkJoinPool(1));
	}
	
	private DistributionLearningInterpretationParallel(ExecutorService execService, int innerMeansSize, int outerMeansSize, int diffDistrSize) {
		bootstrapSizeInnerMeans = innerMeansSize;
		bootstrapSizeOuterMeans = outerMeansSize;
		diffDistributionSampleCount = diffDistrSize;
		executor = execService;
	}
	
	public static DistributionLearningInterpretationParallel getFast(ExecutorService executor) {
		DistributionLearningInterpretationParallel result = new DistributionLearningInterpretationParallel(executor, 100, 100, 1000);
		return result;
	}
	
	public static DistributionLearningInterpretationParallel get(ExecutorService executor) {
		DistributionLearningInterpretationParallel result = new DistributionLearningInterpretationParallel(executor);
		return result;
	}
	
	public static DistributionLearningInterpretationParallel getReasonable(ExecutorService executor) {
		DistributionLearningInterpretationParallel result = new DistributionLearningInterpretationParallel(executor, 1000, 1000, 10000);
		return result;
	}
	

	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot left, DataSnapshot right) {
		try {
			return compareThrowing(left, right);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private ComparisonResult compareThrowing(DataSnapshot left, DataSnapshot right) throws InterruptedException, ExecutionException {
	    	// Pack data into arrays because some parts of the computation can then be easily repeated.
	    	DataSnapshot[] dataSnapshots = new DataSnapshot [2];
	    	dataSnapshots[0] = left;
	    	dataSnapshots[1] = right;

		DataSnapshot[] learningSets = getLearningSets(dataSnapshots [0], dataSnapshots [1]);
		
	    	// Now just do some parts of the processing twice.

		@SuppressWarnings("unchecked")
		Future<Double>[] currentMeans = (Future<Double>[]) new Future<?>[2];

		@SuppressWarnings("unchecked")
		Future<Double>[] historicalMeans = (Future<Double>[]) new Future<?>[2];

		@SuppressWarnings("unchecked")
		Future<double[]>[] normalizedMeanSamples = (Future<double[]>[]) new Future<?>[2];

		for (int i = 0 ; i < 2 ; i ++) {
		    	// Compute the mean of the current runs.
		    	currentMeans[i] = executor.submit(new MeanComputation(dataSnapshots[i]));
		
		    	// Compute the mean used to normalize the historical runs.
		    	historicalMeans[i] = executor.submit(new MeanComputation(learningSets[i]));

		    	// Bootstrap means of means of samples of the normalized historical runs.
			Future<double[][]> allSamplesOriginal = executor.submit(new RunsToDoubleArrays(learningSets[i]));
			Future<double[][]> allSamplesShifted = executor.submit(new SubtractFrom2DArray(allSamplesOriginal, historicalMeans[i]));
			Future<double[]> boostrapped = executor.submit(
				new DoubleBootstrap(
					allSamplesShifted, 
					bootstrapSizeInnerMeans, 
					bootstrapSizeOuterMeans, 
					dataSnapshots[i].getRunCount (), 
					executor));
			
			// Abuse bootstrap to do Monte Carlo of differences of means of means of samples later.
			normalizedMeanSamples[i] = executor.submit(new Bootstrap(boostrapped, diffDistributionSampleCount));
		}
		
		// Compute the difference of means of means of samples.
		Future<double[]> normalizedMeanDifferencesFuture = executor.submit(new ArrayDiff(normalizedMeanSamples[0], normalizedMeanSamples[1]));

		// Use the distribution of the differences in historical means to classify the difference in current means. 
		double currentMeanDifference = currentMeans[0].get() - currentMeans[1].get();
		double[] normalizedMeanDifferences = normalizedMeanDifferencesFuture.get();
		RealDistribution normalizedMeanDifferenceDistribution = DistributionUtils.makeEmpirical(normalizedMeanDifferences);
		
		return new DistributionBasedComparisonResult(currentMeanDifference, normalizedMeanDifferenceDistribution);
	}
	
	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot data, double value) {
		throw new UnsupportedOperationException("This is not yet implemented.");
	}
	
	/** Returns the data snapshots to use for learning the mean difference distribution.
	 * In case one of the input data snapshots does not have a history,
	 * the history of the other input data snapshot is used.
	 * 
	 * TODO Why is the case of data with no history supported at all ? Does it make sense ?
	 * 
	 * @param left Left data snapshot whose history to take.
	 * @param right Right data snapshot whose history to take.
	 * @return The pair of data snapshots with historical data to use.
	 */
	private DataSnapshot[] getLearningSets(DataSnapshot left, DataSnapshot right) {
		DataSnapshot[] result = new DataSnapshot[2];
		
		result[0] = getNonEmptyPreviousEpochDataOrNull(left);
		result[1] = getNonEmptyPreviousEpochDataOrNull(right);
		
		if ((result[0] == null) && (result[1] == null)) {
			result[0] = left;
			result[1] = right;
		} else if (result[0] == null) {
			result[0] = result[1];
		} else if (result[1] == null) {
			result[1] = result[0];
		}
		
		return result;
	}
	
	private DataSnapshot getNonEmptyPreviousEpochDataOrNull(DataSnapshot data) {
		try  {
			DataSnapshot result = data.getPreviousEpoch();
			if ((result == null) || (result.getRunCount() == 0)) {
				return null;
			} else {
				return result;
			}
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}
	
	/** Append bootstrap samples of mean into result array.
	 *  
	 * @param rnd Random generator to use for bootstrap.
	 * @param data Data to bootstrap from.
	 * @param bootstrapLength How long sequences to bootstrap.
	 * @param count How many mean samples to compute.
	 * @param result Array of results to append to.
	 * @param resultStartIndex Starting position in results.
	 */
	private static void bootstrapMeans(Random rnd, double[] data, int bootstrapLength, int count, double[] result, int resultStartIndex) {
		double[] tmp = new double[bootstrapLength];
		
		for (int i = 0; i < count; i++) {
			StatisticsUtils.bootstrap(data, tmp, rnd);
			result[i + resultStartIndex] = StatisticsUtils.mean(tmp);
		}
	}
	
	
	private static class MeanComputation implements Callable<Double> {
		private final DataSnapshot[] data;
		
		/** Compute the mean of all samples given.
        	 * 
        	 * This is not a grand mean, all samples are simply thrown together for the mean computation. 
        	 * The number of samples in a run and the number of runs in a data snapshot is not considered.
        	 *  
		 * @param d Data snapshots to compute the mean from.
		 */
		public MeanComputation(DataSnapshot... d) {
			data = d;
		}

		@Override
		public Double call() throws Exception {
			Collection<BenchmarkRun> merged = new ArrayList<>(data.length);
			for (DataSnapshot d : data) {
				merged.add(BenchmarkRunUtils.merge(d.getRuns()));
			}
			BenchmarkRun twiceMerged = BenchmarkRunUtils.merge(merged);
				
			BenchmarkRunSummary summary = new BenchmarkRunSummary(twiceMerged);
			
			return summary.getMean();
		}
	}
	
	private static class RunsToDoubleArrays implements Callable<double[][]> {
		private final DataSnapshot data;
		
		/** Convert data snapshot into array of arrays of doubles, one double per sample, one array per run.
		 * 
	 	 * @param d Data snapshot to convert.
		 */
		public RunsToDoubleArrays(DataSnapshot d) {
			data = d;			
		}

		@Override
		public double[][] call() throws Exception {
			double[][] result = new double[data.getRunCount()][];
			
			int index = 0;
			for (BenchmarkRun run : data.getRuns()) {
				result[index] = BenchmarkRunUtils.toDoubleArray(run);
				index++;
			}
			
			return result;
		}
		
	}
	
	private static class DoubleBootstrap implements Callable<double[]> {
		private final ExecutorService executor;
		private final Future<double[][]> dataFuture;
		private final int bootstrapSizeInnerMeans;
		private final int bootstrapSizeOuterMeans;
		private final int grandMeanRunCount;
		private final Random bootstrapRandom = new Random();
		
		/** Compute bootstrap grand means from data.
		 * 
		 * @param dataFuture Data to use for computation.
		 * @param innerSize How many bootstrap means to compute from each run.
		 * @param outerSize How many bootstrap grand means to compute from the bootstrap means.
		 * @param runCount How many runs to use in each grand mean.
		 * @param exec The executor service to use.
		 */
		public DoubleBootstrap(Future<double[][]> dataFuture, int innerSize, int outerSize, int runCount, ExecutorService exec) {
			this.dataFuture = dataFuture;
			bootstrapSizeInnerMeans = innerSize;
			bootstrapSizeOuterMeans = outerSize;
			grandMeanRunCount = runCount;
			executor = exec;
		}
		
		@Override
		public double[] call() throws Exception {
			double[][] data = dataFuture.get();
			int runCount = data.length;
			
			double[] runMeans = new double[runCount * bootstrapSizeInnerMeans];
			ArrayList<Callable<Void>> tasks = new ArrayList<>(runCount);
			for (int i = 0; i < runCount; i++) tasks.add(new MeanBootstrap(data[i], runMeans, i * bootstrapSizeInnerMeans, bootstrapSizeInnerMeans));
			
			executor.invokeAll(tasks);
				
			double[] finalSamples = new double[bootstrapSizeOuterMeans];
			bootstrapMeans(bootstrapRandom, runMeans, grandMeanRunCount, bootstrapSizeOuterMeans, finalSamples, 0);
			
			return finalSamples;
		}
	}
	
	private static class MeanBootstrap implements Callable<Void> {
		private final double[] fullArray;
		private final int myStartIndex;
		private final int myLength;
		private final double[] myRun;
		
		/** Append bootstrap samples of mean into result array.
		 * 
		 * @param run Data to bootstrap from, bootstrap uses same sequence length as data length.
		 * @param array Where to store the results.
		 * @param startIndex How many results to append.
		 * @param length Where to start appending the results.
		 */
		public MeanBootstrap(double[] run, double[] array, int startIndex, int length) {
			myRun = run;
			fullArray = array;
			myStartIndex = startIndex;
			myLength = length;
		}
		
		@Override
		public Void call() throws Exception {
			bootstrapMeans(new Random(0), myRun, myRun.length, myLength, fullArray, myStartIndex);
			return null;
		}
	}
	
	private static class SubtractFrom2DArray implements Callable<double[][]> {
		private final Future<double[][]> arrayFuture;
		private final Future<Double> constantFuture;
		
		/** Subtract a constant from all samples in an array of arrays of doubles.
		 * 
		 * @param arr Future array of arrays of doubles to subtract from.
		 * @param c Future constant to subtract.
		 */
		public SubtractFrom2DArray(Future<double[][]> arr, Future<Double> c) {
			arrayFuture = arr;
			constantFuture = c;
		}

		@Override
		public double[][] call() throws Exception {
			double[][] arrays = arrayFuture.get();
			double c = constantFuture.get();
			
			for (int i = 0; i < arrays.length; i++) {
				for (int j = 0; j < arrays[i].length; j++) {
					arrays[i][j] -= c;
				}
			}
			
			return arrays;
		}
	}
	
	private static class Bootstrap implements Callable<double[]> {
		private final Future<double[]> samplesFuture;
		private final int count;
		
		/** Computes bootstrap of given sequence length.
		 * 
		 * @param samples Data to bootstrap.
		 * @param samplesCount Sequence length to return.
		 */
		public Bootstrap(Future<double[]> samples, int samplesCount) {
			samplesFuture = samples;
			count = samplesCount;
		}
		
		@Override
		public double[] call() throws Exception {
			double[] result = new double[count];
			double[] samples = samplesFuture.get();
			StatisticsUtils.bootstrap(samples, result, new Random());
			return result;
		}
	}
	
	private static class ArrayDiff implements Callable<double[]> {
		private final Future<double[]> leftFuture;
		private final Future<double[]> rightFuture;
		
		/** Compute item by item difference of two arrays.
		 */
		public ArrayDiff(Future<double[]> left, Future<double[]> right) {
			leftFuture = left;
			rightFuture = right;
		}
		
		@Override
		public double[] call() throws Exception {
			double[] left = leftFuture.get();
			double[] right = rightFuture.get();
			
			if (left.length != right.length) {
				throw new IllegalArgumentException("Arrays differs in length!");
			}
			
			double[] result = new double[left.length];
			
			for (int i = 0; i < result.length; i++) {
				result[i] = left[i] - right[i];
			}
			
			return result;
		}
	}
}
