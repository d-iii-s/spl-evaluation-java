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

import java.util.ArrayList;
import java.util.Collection;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.utils.StatisticsUtils;

/** Helper methods for working with the BenchmarkRun interface.
 */
public class BenchmarkRunUtils  {
	/** Aggregates all samples from a benchmark run.
	 */ 
	public static interface Reducer {
		/** Reduce benchmark run to a single value.
		 * 
		 * @param run Benchmark run to be reduced.
		 * @return Aggregate of the benchmark depending on the implementation.
		 */
		public double reduce(BenchmarkRun run);
	}
	
	/** Compute mean of a run. */
	protected static class MeanReducer implements Reducer {
		/** {@inheritDoc} */
		@Override
		public double reduce(BenchmarkRun run) {
			return StatisticsUtils.mean(toDoubleArray(run));
		}		
	}
	
	/** Compute variance of a run. */
	protected static class VarianceReducer implements Reducer {
		/** {@inheritDoc} */
		@Override
		public double reduce(BenchmarkRun run) {
			return StatisticsUtils.variance(toDoubleArray(run));
		}		
	}
	
	/** Compute variance of a run without bias correction. */
	protected static class VarianceNReducer implements Reducer {
		/** {@inheritDoc} */
		@Override
		public double reduce(BenchmarkRun run) {
			return StatisticsUtils.varianceN(toDoubleArray(run));
		}		
	}
	
	/** Convert benchmark run to an array of doubles.
	 * 
	 * @param run Benchmark run to be converted.
	 * @return Array of doubles - samples in the run.
	 */
	public static double[] toDoubleArray(BenchmarkRun run) {
		synchronized (run) {
			double[] result = new double[run.getSampleCount()];
			for (int i = 0; i < result.length; i++) {
				result[i] = run.getSample(i);
			}
			return result;
		}
	}
	
	/** Merge individual benchmark runs into a single one.
	 * 
	 * <p>
	 * The sample ordering preserves order inside a run and
	 * runs are ordered in the same way they were iterated.
	 * 
	 * @param runs Individual runs to merge.
	 * @return Merged run.
	 */
	public static BenchmarkRun merge(Iterable<BenchmarkRun> runs) {
		BenchmarkRunBuilder builder = new BenchmarkRunBuilder();
		
		synchronized (runs) {
			for (BenchmarkRun r : runs) {
				synchronized (r) {
					for (double l : r.getSamples()) {
						builder.addSamples(l);
					}
				}
			}
		}
		
		return builder.create();
	}
	
	/** Reducer for mean computation from a benchmark run. */
	public static final Reducer MEAN = new MeanReducer();
	
	/** Reducer for variance computation from a benchmark run. */
	public static final Reducer VARIANCE = new VarianceReducer();
	
	/** Reducer for variance computation without bias correction from a benchmark run. */
	public static final Reducer VARIANCE_N = new VarianceNReducer();
	
	/** Reduce multiple runs, each to a single double value.
	 * 
	 * @param runs Runs to be reduced.
	 * @param reducer Reducer to be used, see available reduced in this class.
	 * @return Each run represented as a single value in the collection.
	 */
	public static Collection<Double> reduce(Iterable<BenchmarkRun> runs, Reducer reducer) {
		Collection<Double> result = new ArrayList<>();
		synchronized (runs) {
			for (BenchmarkRun run : runs) {
				synchronized (run) {
					result.add(reducer.reduce(run));
				}
			}
		}
		
		return result;
	}
}
