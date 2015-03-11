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

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.ComparisonResult;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.Interpretation;

/** Primitive interpretation that merely compares arithmetic means.
 *
 */
public class KindergartenMath implements Interpretation {
	public static final KindergartenMath INSTANCE = new KindergartenMath();
	private static final int MIN_SAMPLE_COUNT = 2;
	private static final double ZERO = 0.000001;
	
	private static class ImpossibleToCompareResult implements ComparisonResult {
		
		public static ImpossibleToCompareResult INSTANCE = new ImpossibleToCompareResult();
		
		private ImpossibleToCompareResult() {
		}

		@Override
		public Relation get(double significanceLevel) {
			return Relation.UNKNOWN;
		}

		@Override
		public double getStatistic() {
			throw new UnsupportedOperationException();
		}

		@Override
		public double getCriticalValue(double significanceLevel) {
			throw new UnsupportedOperationException();
		}

		@Override
		public double[] getConfidenceInterval(double confidenceLevel) {
			throw new UnsupportedOperationException();
		}
	}
	
	private static class MeanDifferenceComparisonResult implements ComparisonResult {
		private double diff;
		
		public MeanDifferenceComparisonResult(double left, double right) {
			diff = left - right;
		}

		@Override
		public Relation get(double significanceLevel) {
			if ((diff > -ZERO) && (diff < ZERO)) {
				return Relation.EQUAL;
			} else if (diff < 0.) {
				return Relation.LESS_THAN;
			} else {
				return Relation.GREATER_THAN;
			}
		}

		@Override
		public double getStatistic() {
			throw new UnsupportedOperationException();
		}

		@Override
		public double getCriticalValue(double significanceLevel) {
			throw new UnsupportedOperationException();
		}

		@Override
		public double[] getConfidenceInterval(double confidenceLevel) {
			throw new UnsupportedOperationException();
		}
	}
	
	private double getMean(DataSnapshot data) {
		double sum = 0;
		long count = 0;
		for (BenchmarkRun run : data.getRuns()) {
			for (long sample : run.getSamples()) {
				sum += sample;
				count++;
			}
		}
		if (count == 0) {
			return 0.;
		} else {
			return sum / count;
		}
	}
	
	private boolean hasEnoughSamples(DataSnapshot data) {
		if (data.getRunCount() == 0) {
			return false;
		}
		
		long totalSampleCount = 0;
		for (BenchmarkRun run : data.getRuns()) {
			totalSampleCount += run.getSampleCount();
		}
		
		return totalSampleCount > MIN_SAMPLE_COUNT;
	}


	@Override
	public ComparisonResult compare(DataSnapshot left, DataSnapshot right) {
		if (!hasEnoughSamples(left) || !hasEnoughSamples(right)) {
			return ImpossibleToCompareResult.INSTANCE;
		}
		
		return new MeanDifferenceComparisonResult(getMean(left), getMean(right));
	}


	@Override
	public ComparisonResult compare(DataSnapshot data, double value) {
		if (!hasEnoughSamples(data)) {
			return ImpossibleToCompareResult.INSTANCE;
		}
		
		return new MeanDifferenceComparisonResult(getMean(data), value);
	}

}
