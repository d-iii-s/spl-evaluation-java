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

import java.util.Collection;
import java.util.LinkedList;
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
	private Random bootstrapRandom = new Random();
	
	public DistributionLearningInterpretation() {
	}

	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot left, DataSnapshot right) {
		Collection<BenchmarkRun> historicalRuns = getHistoricalRuns(left, right);
		
		RealDistribution historicalDistribution = makeBootstrappedEmpiricalDistribution(historicalRuns);
		
		RealDistribution diffDistribution = makeDiffDistribution(historicalDistribution, historicalDistribution);
				
		double statistic = computeMean(left) - computeMean(right);
		
		return new DistributionBasedComparisonResult(statistic, diffDistribution);
	}

	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot data, double value) {
		throw new UnsupportedOperationException("This is not yet implemented.");
	}
	
	private double computeMean(DataSnapshot data) {
		BenchmarkRun merged = BenchmarkRunUtils.merge(data.getRuns());
		
		BenchmarkRunSummary summary = new BenchmarkRunSummary(merged);
		
		return summary.getMean();
	}
	

	private Collection<BenchmarkRun> getHistoricalRuns(DataSnapshot left,
			DataSnapshot right) {
		List<BenchmarkRun> result = new LinkedList<>();
		
		addHistoricalData(result, left);
		addHistoricalData(result, right);
		
		return result;
	}
	
	private void addHistoricalData(List<BenchmarkRun> historicalRuns, DataSnapshot currentData) {
		try {
			DataSnapshot historicalData = currentData.getPreviousEpoch();
			if (historicalData == null) {
				return;
			}
			
			for (BenchmarkRun run : historicalData.getRuns()) {
				historicalRuns.add(run);
			}
		} catch (UnsupportedOperationException e) {
			// Ignore.
		}		
	}
	
	private RealDistribution makeBootstrappedEmpiricalDistribution(Collection<BenchmarkRun> baseRuns) {
		int setsNo = baseRuns.size();
		
		int insideRunIter = 100;
		int outsideRunIter = 20;
		
		insideRunIter = 1000;
		outsideRunIter = 1000*100;
		
		double[] Pxn = new double[setsNo * insideRunIter];
		int startIndex = 0;
		for (BenchmarkRun run : baseRuns) {
			double[] samples = BenchmarkRunUtils.toDoubleArray(run);
			bootstrapWithMean(samples, samples.length, insideRunIter, Pxn, startIndex);
			startIndex += insideRunIter;
		}
		
		double[] finalSamples = new double[outsideRunIter];
		bootstrapWithMean(Pxn, baseRuns.size(), outsideRunIter, finalSamples, 0);
		
		return DistributionUtils.makeEmpirical(finalSamples);
	}
	
	private void bootstrapWithMean(double[] data, int bootstrapLength, int count, double[] result, int resultStartIndex) {
		double[] tmp = new double[bootstrapLength];
		
		for (int i = 0; i < count; i++) {
			StatisticsUtils.bootstrap(data, tmp, bootstrapRandom);
			result[i + resultStartIndex] = StatisticsUtils.mean(tmp);
		}
	}
	
	private RealDistribution makeDiffDistribution(RealDistribution d1, RealDistribution d2) {
		int iters = 1000;
		iters = 1000 * 1000;
		
		double[] samples = new double[iters];
		
		for (int i = 0; i < iters; i++) {
			samples[i] = d1.sample();
		}
		
		for (int i = 0; i < iters; i++) {
			samples[i] -= d2.sample();
		}
		
		return DistributionUtils.makeEmpirical(samples);
	}
}
