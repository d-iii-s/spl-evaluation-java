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
package cz.cuni.mff.d3s.spl.data;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import cz.cuni.mff.d3s.spl.BenchmarkRun;

/** Statistical summary of a benchmark run.
 * 
 * <p>
 * This class aggregates the whole benchmark run into few values such as
 * mean, variance or number of data samples.
 * 
 * <p>
 * This class is in essence immutable but it uses caching to improve
 * performance (hopefully).
 * Also, it makes copy of the original benchmark run and the changes in
 * the original run are not taken into account when user retrieves the values.
 */
public class BenchmarkRunSummary {
	private final double[] data;
	private Double cacheMean = null;
	private Double cacheVariance = null;
	
	/** Create a new summary from a benchmark run.
	 * 
	 * <p>
	 * The data from the given run are copied and further changes to the
	 * run are ignored when the statistical values are retrived. 
	 * 
	 * @param run Benchmark run from which to compute the summary.
	 */
	public BenchmarkRunSummary(BenchmarkRun run) {
		synchronized (run) {
			data = new double[run.getSampleCount()];
			for (int i = 0; i < data.length; i++) {
				data[i] = run.getSample(i);
			}
		}
	}
	
	/** Compute artihmetic mean of the samples.
	 * 
	 * @return Arithmetic mean of the data in the original benchmark run.
	 */
	public synchronized double getMean() {
		if (cacheMean == null) {
			Mean mean = new Mean();
			cacheMean = mean.evaluate(data);
		}
		return cacheMean;
	}
	
	/** Compute variance of the samples.
	 * 
	 * @return Variance of the data in the original benchmark run.
	 */
	public synchronized double getVariance() {
		if (cacheVariance == null) {
			Variance mean = new Variance();
			cacheVariance = mean.evaluate(data);
		}
		return cacheVariance;
	}
	
	/** Tell number of data samples.
	 * 
	 * @return Number of samples in the original benchmark run.
	 */
	public long getSize() {
		return data.length;
	}
}
