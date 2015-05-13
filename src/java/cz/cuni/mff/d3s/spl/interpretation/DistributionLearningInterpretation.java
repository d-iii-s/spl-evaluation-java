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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.RealDistribution;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.ComparisonResult;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunSummary;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunUtils;
import cz.cuni.mff.d3s.spl.utils.DistributionUtils;
import cz.cuni.mff.d3s.spl.utils.StatisticsUtils;


/** SPL interpretation based on learning the distribution before deciding.
 * 
 * <p>
 * See
 * <a href="http://d3s.mff.cuni.cz/publications/download/D3S-TR-2014-04.pdf">SPL:
 * Unit Testing Performance</a> by
 * Bulej, Bures, Horky, Kotrc, Marek, Trojanek and Tuma for details.
 *
 */
public class DistributionLearningInterpretation implements Interpretation {
	private Random bootstrapRandom = new Random(0);
	
	private final int bootstrapSizeInnerMeans;
	private final int bootstrapSizeOuterMeans;
	private final int diffDistributionSampleCount;
	private PrintStream debug = null;
	
	public DistributionLearningInterpretation() {
		this(1000, 10000, 100000);
	}
	
	private DistributionLearningInterpretation(int innerMeansSize, int outerMeansSize, int diffDistrSize) {
		bootstrapSizeInnerMeans = innerMeansSize;
		bootstrapSizeOuterMeans = outerMeansSize;
		diffDistributionSampleCount = diffDistrSize;
	}
	
	public static DistributionLearningInterpretation getDebug(PrintStream output) {
		DistributionLearningInterpretation result = new DistributionLearningInterpretation();
		result.debug = output;
		return result;
	}
	
	public static DistributionLearningInterpretation getDebugFast(PrintStream output) {
		DistributionLearningInterpretation result = getFast();
		result.debug = output;
		return result;
	}
	
	public static DistributionLearningInterpretation getFast() {
		DistributionLearningInterpretation result = new DistributionLearningInterpretation(100, 100, 1000);
		return result;
	}
	

	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot left, DataSnapshot right) {
		double leftMean = computeMean(left);
		double rightMean = computeMean(right);
		
		if (debug != null) {
			debug.println("DistributionLearningInterpreation.compare");
			debug.printf("means: %15.3f %15.3f\n", leftMean, rightMean);
		}
		
		RealDistribution leftDistr = boostrapEmpirical(left, -leftMean);
		if (debug != null) {
			debug.printf("left boostrapped:");
			showDistribution(leftDistr);
		}
		
		RealDistribution rightDistr = boostrapEmpirical(right, -rightMean);
		if (debug != null) {
			debug.printf("right boostrapped:");
			showDistribution(rightDistr);
		}
		
		RealDistribution diffDistr = substractDistributions(leftDistr, rightDistr);
		if (debug != null) {
			debug.printf("diff distribution:");
			showDistribution(diffDistr);
		}
		
		double statistic = leftMean - rightMean;
		
		return new DistributionBasedComparisonResult(statistic, diffDistr);
	}
	
	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot data, double value) {
		throw new UnsupportedOperationException("This is not yet implemented.");
	}
	
	
	private RealDistribution boostrapEmpirical(DataSnapshot data, double shift) {
		List<BenchmarkRun> runs = new ArrayList<>(data.getRunCount());
		for (BenchmarkRun run : data.getRuns()) {
			runs.add(run);
		}
		
		int runCount = runs.size();
		
		double[] runMeans = new double[runCount * bootstrapSizeInnerMeans];
		int startIndex = 0;
		for (int i = 0; i < runCount; i++, startIndex += bootstrapSizeInnerMeans) {
			double[] samples = BenchmarkRunUtils.toDoubleArray(runs.get(i));
			bootstrapWithMean(samples, samples.length, bootstrapSizeInnerMeans, runMeans, startIndex);
		}
		
		double[] finalSamples = new double[bootstrapSizeOuterMeans];
		bootstrapWithMean(runMeans, runs.size(), bootstrapSizeOuterMeans, finalSamples, 0);
		
		for (int i = 0; i < finalSamples.length; i++) {
			finalSamples[i] += shift;
		}
		
		return DistributionUtils.makeEmpirical(finalSamples);
	}
	
	private RealDistribution substractDistributions(RealDistribution left, RealDistribution right) {
		double[] leftSamples = left.sample(diffDistributionSampleCount);
		double[] rightSamples = left.sample(diffDistributionSampleCount);
		
		for (int i = 0; i < leftSamples.length; i++) {
			leftSamples[i] -= rightSamples[i];
		}
		
		return DistributionUtils.makeEmpirical(leftSamples);
	}
	
	
	private double computeMean(DataSnapshot data) {
		BenchmarkRun merged = BenchmarkRunUtils.merge(data.getRuns());
		
		BenchmarkRunSummary summary = new BenchmarkRunSummary(merged);
		
		return summary.getMean();
	}
	
	private void bootstrapWithMean(double[] data, int bootstrapLength, int count, double[] result, int resultStartIndex) {
		double[] tmp = new double[bootstrapLength];
		
		for (int i = 0; i < count; i++) {
			StatisticsUtils.bootstrap(data, tmp, bootstrapRandom);
			result[i + resultStartIndex] = StatisticsUtils.mean(tmp);
		}
	}
	
	private void showDistribution(RealDistribution distr) {
		assert debug != null;
		for (int i = 0; i < 10; i++) {
			debug.printf("  %.3f", distr.sample());
		}
		debug.println();
	}
}
