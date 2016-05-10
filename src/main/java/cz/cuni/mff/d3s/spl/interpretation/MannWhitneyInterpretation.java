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

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.ComparisonResult;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunUtils;

/** SPL interpretation based on Mann-Whitney test.
 * 
 * This code is greatly inspired by
 * org.apache.commons.math3.stat.inference.MannWhitneyUTest implementation
 * that could not be used directly in the compare() method.
 *
 */
public class MannWhitneyInterpretation implements Interpretation {
	private final MannWhitneyUTest utest = new MannWhitneyUTest();
	
	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot left, DataSnapshot right) {
		double[] leftSamples = mergeSamples(left);
		double[] rightSamples = mergeSamples(right);
		
		double uStatMax = utest.mannWhitneyU(leftSamples, rightSamples);
		
		long lengthsMultiplied = (long) leftSamples.length * rightSamples.length;

		double uStatMin = lengthsMultiplied - uStatMax;
		
		/* https://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U_test#Normal_approximation */
		double meanU = lengthsMultiplied / 2.0;
		double varU = lengthsMultiplied * (leftSamples.length + rightSamples.length + 1) / 12.0;
		
		double z = (uStatMin - meanU) / Math.sqrt(varU);
		
		NormalDistribution distribution = new NormalDistribution(0.0, 1.0);
		
		return new DistributionBasedComparisonResult(z, distribution);
	}

	/** {@inheritDoc} */
	@Override
	public ComparisonResult compare(DataSnapshot data, double value) {
		throw new UnsupportedOperationException("This is not yet implemented.");
	}

	private double[] mergeSamples(DataSnapshot data) {
		BenchmarkRun merged = BenchmarkRunUtils.merge(data.getRuns());
		
		return BenchmarkRunUtils.toDoubleArray(merged);
	}
}
