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


/** Parallel interpretation of the DistributionLearningInterpretation.
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
	

	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot left, DataSnapshot right) {
		try {
			return compareThrowing(left, right);
		} catch (InterruptedException e) {
			return null;
		} catch (ExecutionException e) {
			return null;
		}
	}
	
	private ComparisonResult compareThrowing(DataSnapshot left, DataSnapshot right) throws InterruptedException, ExecutionException {
		Future<Double> leftMean = executor.submit(new MeanComputation(left));
		Future<Double> rightMean = executor.submit(new MeanComputation(right));
		
		Future<double[]> leftDistributionSamples = executor.submit(new DoubleBootstrap(left, bootstrapSizeInnerMeans, bootstrapSizeOuterMeans, executor));
		Future<double[]> rightDistributionSamples = executor.submit(new DoubleBootstrap(right, bootstrapSizeInnerMeans, bootstrapSizeOuterMeans, executor));
		
		Future<double[]> leftDistributionSamplesNormalized = executor.submit(new SubtractFromArray(leftDistributionSamples, leftMean));
		Future<double[]> rightDistributionSamplesNormalized = executor.submit(new SubtractFromArray(rightDistributionSamples, rightMean));
		
		Future<double[]> leftSamples = executor.submit(new SamplesOfEmpiricalDistribution(leftDistributionSamplesNormalized, diffDistributionSampleCount));
		Future<double[]> rightSamples = executor.submit(new SamplesOfEmpiricalDistribution(rightDistributionSamplesNormalized, diffDistributionSampleCount));
		
		Future<double[]> diffSamplesFuture = executor.submit(new ArrayDiff(leftSamples, rightSamples));
		
		double statistic = leftMean.get() - rightMean.get();
		
		double[] diffSamples = diffSamplesFuture.get();
		
		RealDistribution diffDistr = DistributionUtils.makeEmpirical(diffSamples);
		
		return new DistributionBasedComparisonResult(statistic, diffDistr);
	}
	
	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot data, double value) {
		throw new UnsupportedOperationException("This is not yet implemented.");
	}
	
	private static class MeanComputation implements Callable<Double> {
		private final DataSnapshot data;
		
		public MeanComputation(DataSnapshot d) {
			data = d;
		}

		@Override
		public Double call() throws Exception {
			BenchmarkRun merged = BenchmarkRunUtils.merge(data.getRuns());
			
			BenchmarkRunSummary summary = new BenchmarkRunSummary(merged);
			
			return summary.getMean();
		}
	}
	
	private static void bootstrapWithMean(Random rnd, double[] data, int bootstrapLength, int count, double[] result, int resultStartIndex) {
		double[] tmp = new double[bootstrapLength];
		
		for (int i = 0; i < count; i++) {
			StatisticsUtils.bootstrap(data, tmp, rnd);
			result[i + resultStartIndex] = StatisticsUtils.mean(tmp);
		}
	}
	
	private static class DoubleBootstrap implements Callable<double[]> {
		private final ExecutorService executor;
		private final DataSnapshot data;
		private final int bootstrapSizeInnerMeans;
		private final int bootstrapSizeOuterMeans;
		private final Random bootstrapRandom = new Random(0);
		
		public DoubleBootstrap(DataSnapshot d, int innerSize, int outerSize, ExecutorService exec) {
			data = d;
			bootstrapSizeInnerMeans = innerSize;
			bootstrapSizeOuterMeans = outerSize;
			executor = exec;
		}
		
		@Override
		public double[] call() throws Exception {
			int runCount = data.getRunCount();
			
			double[] runMeans = new double[runCount * bootstrapSizeInnerMeans];
			ArrayList<Callable<Void>> tasks = new ArrayList<>(runCount);
			for (int i = 0; i < runCount; i++) {
				tasks.add(new BootstrapFromRun(data.getRun(i), runMeans, i * bootstrapSizeInnerMeans, bootstrapSizeInnerMeans));
			}
			
			executor.invokeAll(tasks);
				
			double[] finalSamples = new double[bootstrapSizeOuterMeans];
			bootstrapWithMean(bootstrapRandom, runMeans, runCount, bootstrapSizeOuterMeans, finalSamples, 0);
			
			return finalSamples;
		}
	}
	
	private static class BootstrapFromRun implements Callable<Void> {
		private final double[] fullArray;
		private final int myStartIndex;
		private final int myLength;
		private final BenchmarkRun myRun;
		
		public BootstrapFromRun(BenchmarkRun run, double[] array, int startIndex, int length) {
			myRun = run;
			fullArray = array;
			myStartIndex = startIndex;
			myLength = length;
		}
		
		@Override
		public Void call() throws Exception {
			double[] samples = BenchmarkRunUtils.toDoubleArray(myRun);
			bootstrapWithMean(new Random(0), samples, samples.length, myLength, fullArray, myStartIndex);
			return null;
		}
	}
	
	private static class SubtractFromArray implements Callable<double[]> {
		private final Future<double[]> arrayFuture;
		private final Future<Double> constantFuture;
		
		public SubtractFromArray(Future<double[]> arr, Future<Double> c) {
			arrayFuture = arr;
			constantFuture = c;
		}

		@Override
		public double[] call() throws Exception {
			double[] array = arrayFuture.get();
			double c = constantFuture.get();
			
			for (int i = 0; i < array.length; i++) {
				array[i] -= c;
			}
			
			return array;
		}
	}
	
	private static class SamplesOfEmpiricalDistribution implements Callable<double[]> {
		private final Future<double[]> samplesFuture;
		private final int count;
		
		public SamplesOfEmpiricalDistribution(Future<double[]> samples, int samplesCount) {
			samplesFuture = samples;
			count = samplesCount;
		}
		
		@Override
		public double[] call() throws Exception {
			RealDistribution distr = DistributionUtils.makeEmpirical(samplesFuture.get());
			return distr.sample(count);
		}
	}
	
	private static class ArrayDiff implements Callable<double[]> {
		private final Future<double[]> leftFuture;
		private final Future<double[]> rightFuture;
		
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
