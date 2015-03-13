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

import java.util.Random;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

/** Helper methods for common statistics.
 */
public class StatisticsUtils  {
	/** Compute arithmetic mean of given data.
	 * 
	 * @param values Array of values to compute the mean from.
	 * @return Mean of the provided values.
	 */
	public static double mean(double... values) {
		Mean mean = new Mean();
		return mean.evaluate(values);
	}
	
	/** Compute variance of given data with bias correction.
	 * 
	 * @param values Array of values to compute the variance from.
	 * @return Varince of the provided values.
	 */
	public static double variance(double... values) {
		Variance var = new Variance();
		return var.evaluate(values);
	}
	
	/** Compute variance of given data without bias correction.
	 * 
	 * @param values Array of values to compute the variance from.
	 * @return Varince of the provided values.
	 */
	public static double varianceN(double... values) {
		Variance var = new Variance(false);
		return var.evaluate(values);
	}
	
	/** Bootstrap from already known values.
	 * 
	 * <p>
	 * The bootstrapping procedure is simple as we randomly select values from
	 * the source array to store in the destination one.
	 * Obviously, the individual values can be repeated in the bootstrapped
	 * array.
	 * 
	 * @param source Array with original values to bootstrap from.
	 * @param dest Array where to store the bootstrapped values.
	 * @param rnd Random number generator to use.
	 */
	public static void bootstrap(double[] source, double dest[], Random rnd) {
		for (int i = 0; i < dest.length; i++) {
			dest[i] = source[rnd.nextInt(source.length)];
		}
	}
}
