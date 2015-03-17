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

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.ComparisonResult;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunUtils;
import cz.cuni.mff.d3s.spl.utils.ArrayUtils;
import cz.cuni.mff.d3s.spl.utils.StatisticsUtils;

/** SPL interpretation based on Welch's t-test with enlarged variances.
 * 
 * <p>See <a href="http://dx.doi.org/10.1109/MASCOTS.2005.18">Automated
 * Detection of Performance Regressions: The Mono Experience</a> by
 * Kalibera, Bulej and Tuma for details.
*
*/
public class WelchTestWithEnlargedVariancesInterpretation implements Interpretation {
	public WelchTestWithEnlargedVariancesInterpretation() {
	}

	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot left, DataSnapshot right) {
		NeededStatistics leftStat = NeededStatistics.create(left);
		NeededStatistics rightStat = NeededStatistics.create(right);
		
		double stat = getStatistic(leftStat, rightStat);
		
		RealDistribution distribution = new NormalDistribution();
		
		return new DistributionBasedComparisonResult(stat, distribution);
	}

	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot data, double value) {
		throw new UnsupportedOperationException("This is not yet implemented.");
	}
	
	private double getStatistic(NeededStatistics x, NeededStatistics y) {
		double numer = x.getMean() - y.getMean();
		double denom = Math.sqrt(x.getSigma2() + y.getSigma2());
		return numer / denom;
	}
	
	private static class NeededStatistics {
		private double mean;
		private double sigma2;
		
		public static NeededStatistics create(DataSnapshot data) {
			NeededStatistics result = new NeededStatistics();
			
			Collection<Double> meansCollection = BenchmarkRunUtils.reduce(data.getRuns(), BenchmarkRunUtils.MEAN);
			double[] means = ArrayUtils.makeArray(meansCollection);
			result.mean = StatisticsUtils.mean(means);
			double varianceOfMeans = StatisticsUtils.variance(means);
			
			Collection<Double> variancesCollection = BenchmarkRunUtils.reduce(data.getRuns(), BenchmarkRunUtils.VARIANCE_N);
			double meanOfVariances = StatisticsUtils.mean(ArrayUtils.makeArray(variancesCollection));
			
			long totalSampleCount = 0;
			for (BenchmarkRun run : data.getRuns()) {
				totalSampleCount += run.getSampleCount();
			}
			
			result.sigma2 = varianceOfMeans / data.getRunCount()
					+ meanOfVariances / totalSampleCount;
			
			return result;
		}
		
		public double getMean() {
			return mean;
		}
		
		public double getSigma2() {
			return sigma2;
		}
	}
}
