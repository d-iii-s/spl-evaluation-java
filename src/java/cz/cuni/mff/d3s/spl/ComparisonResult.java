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
package cz.cuni.mff.d3s.spl;

/** Encapsulates result of a comparison of two data sources.
 * 
 * <p>
 * The basic operation is to get what is the relation between the
 * two data sets.
 * That is the only operation that must be provided by the interface,
 * all other methods may throw UnsupportedOperationException.
 * 
 * <p>
 * For a typical statistical test the comparison result is directly
 * derived from the statistic and its position relative to the critical
 * region as computed at given significance level.
 *
 */
public interface ComparisonResult {
	/** Actual result of the comparison.
	 * 
	 * All the values are bound to the given significance level.
	 */
	public enum Relation {
		/** It is not possible to tell the relation at given significance level. */
		UNKNOWN,
		/** The first (left) data set is smaller than the second (right) one. */
		LESS_THAN,
		/** The data sets are considered equal. */
		EQUAL,
		/** The first (left) data set is greater than the second (right) one. */
		GREATER_THAN
	};
	
	/** Get the actual result.
	 * 
	 * @param significanceLevel Significance level to use.
	 * @return Relation between the two original data sets.
	 */
	public Relation get(double significanceLevel);
	
	/** Get statistic of the underlying test.
	 * 
	 * @return Test statistic.
	 * @throws UnsupportedOperationException When the operation is not supported.
	 */
	public double getStatistic();
	
	/** Tells a critical value for given significance level.
	 * 
	 * @param significanceLevel Significance level to use.
	 * @return Critical value at given significance level.
	 * @throws UnsupportedOperationException When the operation is not supported.
	 * @throws IllegalArgumentException When the significanceLevel is outside range 0 and 1.
	 */
	public double getCriticalValue(double significanceLevel);
	
	/** Tells confidence interval for given level of the interpretation.
	 * 
	 * <p>
	 * Typically the confidence interval is returned for a difference
	 * between means of the two data sets.
	 * 
	 * @param confidenceLevel Confidence level to use.
	 * @return Typically a two member array denoting the confidence interval.
	 * @throws UnsupportedOperationException When the operation is not supported.
	 * @throws IllegalArgumentException When the significanceLevel is outside range 0 and 1.
	 */
	public double[] getConfidenceInterval(double confidenceLevel);
}
