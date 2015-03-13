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

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.TDistribution;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.ComparisonResult;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunSummary;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunUtils;

/** SPL interpretation based on Welch's t-test.
 *
 */
public class WelchTestInterpretation implements Interpretation {
	private double statistic;
	private RealDistribution distribution = null;
	
	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot left, DataSnapshot right) {
		BenchmarkRunSummary leftSummary = computeMergedStatistic(left);
		BenchmarkRunSummary rightSummary = computeMergedStatistic(right);
		
		statistic = getStatistic(leftSummary, rightSummary);
		
		double freedomDeg = getDegreesOfFreedom(leftSummary, rightSummary);
		distribution = new TDistribution(freedomDeg);
		
		return new DistributionBasedComparisonResult(statistic, distribution);
	}

	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot data, double value) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	private BenchmarkRunSummary computeMergedStatistic(DataSnapshot data) {
		BenchmarkRun merged = BenchmarkRunUtils.merge(data.getRuns());
		
		return new BenchmarkRunSummary(merged);
	}
	
	private double getStatistic(BenchmarkRunSummary x, BenchmarkRunSummary y) {
		double numer = x.getMean() - y.getMean();
		double denom2 = x.getVariance() / x.getSize() + y.getVariance() / y.getSize();
		return (numer) / Math.sqrt(denom2);
	}
	
	private double getDegreesOfFreedom(BenchmarkRunSummary x, BenchmarkRunSummary y) {
		return getDegreesOfFreedom(x.getVariance(), x.getSize(), y.getVariance(), y.getSize());
	}

	private double getDegreesOfFreedom(double xVar, long xSize, double yVar, long ySize) {
		double numerator = square(xVar/xSize + yVar/ySize);
		double denominator = nuHelper(xVar, xSize) + nuHelper(yVar, ySize);
		return numerator / denominator;
	}

	private double nuHelper(double var, long n) {
		return (var * var) / (n * n * (n - 1));
	}
		
	private double square(double x) {
		return x * x;
	}
}
