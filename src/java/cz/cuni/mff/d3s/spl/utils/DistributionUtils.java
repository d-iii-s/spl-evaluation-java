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
package cz.cuni.mff.d3s.spl.utils;

import java.util.Arrays;

import org.apache.commons.math3.random.EmpiricalDistribution;

/** Helper method for creating mathematical distributions.
 */
public class DistributionUtils  {
	private static int countDistinctValues(double[] samples, double epsilon) {
		if (samples.length <= 1) {
			return 1;
		}
		
		double[] data = Arrays.copyOf(samples, samples.length);
		Arrays.sort(data);
		
		int count = 1;
		double previous = data[0];
		for (double value : data) {
			if (Math.abs(previous - value) > epsilon) {
				previous = value;
				count++;
			}
		}
		return count;
	}
	
	/** Create empirical distribution from given samples.
	 * 
	 * @param samples Samples (does not need to be distinct) to use.
	 * @return Empirical distribution built from the samples.
	 */
	public static EmpiricalDistribution makeEmpirical(double[] samples) {
		int values = countDistinctValues(samples, 0.001);
		
		EmpiricalDistribution distr = new EmpiricalDistribution(values/10 + 1);
		distr.load(samples);	
		
		return distr;
	}
}
