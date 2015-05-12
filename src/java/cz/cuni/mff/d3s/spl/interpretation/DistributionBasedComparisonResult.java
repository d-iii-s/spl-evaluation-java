/*
 * Copyright 2014 Charles University in Prague
 * Copyright 2014 Vojtech Horky
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

import cz.cuni.mff.d3s.spl.ComparisonResult;

/** Data set comparison result that uses probability distributions.
 */
public class DistributionBasedComparisonResult implements ComparisonResult {
	private double statistic;
	private RealDistribution distribution;
	
	/** Create the result from precomputed statistics and corresponding distribution,.
	 * 
	 * @param stat Statistics value.
	 * @param distr Probability distribution.
	 */
	public DistributionBasedComparisonResult(double stat, RealDistribution distr) {
		statistic = stat;
		distribution = distr;
	}

	/** {@inheritDoc} */
	@Override
	public double getStatistic() {
		return statistic;
	}

	/** {@inheritDoc} */
	@Override
	public Relation get(double significanceLevel) {
		boolean lt = statistic > getCriticalValue(significanceLevel);
		boolean gt = statistic < getCriticalValue(1. - significanceLevel);
		
		if (!lt && !gt) {
			return Relation.EQUAL;
		} else if (lt) {
			return Relation.GREATER_THAN;
		} else {
			return Relation.LESS_THAN;
		}
	}

	/** {@inheritDoc} */
	@Override
	public double getCriticalValue(double significanceLevel) {
		return distribution.inverseCumulativeProbability(significanceLevel);
	}
	
	/** {@inheritDoc} */
	@Override
	public double[] getConfidenceInterval(double confidenceLevel) {
		throw new UnsupportedOperationException("Confidence interval computation not implemented.");
	}
	
}
