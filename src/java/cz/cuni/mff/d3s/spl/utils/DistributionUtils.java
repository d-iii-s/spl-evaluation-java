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

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/** Helper method for creating mathematical distributions.
 */
public class DistributionUtils  {
		
	/** Create empirical distribution from given samples.
	 * 
	 * @param samples Samples (does not need to be distinct) to use.
	 * @return Empirical distribution built from the samples.
	 */
	public static EmpiricalDistribution makeEmpirical(double[] samples) {
		/* Be deterministic for now. */
		RandomGenerator gen = new JDKRandomGenerator();
		gen.setSeed(0);
		
		EmpiricalDistribution result = new EmpiricalDistribution(samples.length / 10 + 1, gen);
		result.load(samples);
		
		return result;
	}
}
