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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.tests.TestUtils;

public class BenchmarkRunUtilsTest {
	private static final Iterable<BenchmarkRun> NO_RUNS = new ArrayList<BenchmarkRun>();
	
	private static final BenchmarkRun SIMPLE_RUN = new ImmutableBenchmarkRun(2, 0, 1, 5);
	
	private static final BenchmarkRun[] RUNS_ARRAY = new BenchmarkRun[] {
		new ImmutableBenchmarkRun(0, 1, 2),
		new ImmutableBenchmarkRun(3, 6, 9),
		new ImmutableBenchmarkRun(10, 11, 33)
	};
	
	private static Collection<BenchmarkRun> RUNS_COLLECTION;
	
	@Before
	public void prepareRuns() {
		RUNS_COLLECTION = Arrays.asList(RUNS_ARRAY);
	}
	
	@Test
	public void mergeNoRuns() {
		TestUtils.assertBenchmarkRun(BenchmarkRunUtils.merge(NO_RUNS));
	}
	
	@Test
	public void mergeSeveralRuns() {
		BenchmarkRun merged = BenchmarkRunUtils.merge(RUNS_COLLECTION);
		
		TestUtils.assertBenchmarkRun(merged, 0, 1, 2, 3, 6, 9, 10, 11, 33);
	}
	
	@Test
	public void meanReducer() {
		Collection<Double> means = BenchmarkRunUtils.reduce(RUNS_COLLECTION, BenchmarkRunUtils.MEAN);
		assertEquals(Arrays.asList((Double) 1., 6., 18.), means);
	}
	
	@Test
	public void varianceReducer() {
		Collection<Double> vars = BenchmarkRunUtils.reduce(RUNS_COLLECTION, BenchmarkRunUtils.VARIANCE);
		assertEquals(Arrays.asList((Double) 1., 9., 169.), vars);
	}
	
	@Ignore
	private static class SampleMultiplier implements BenchmarkRunUtils.Transformer {
		private final long mult;
		
		public SampleMultiplier(int multiplier) {
			mult = multiplier;
		}

		@Override
		public long apply(long sample) {
			return sample * mult;
		}
	}
	
	@Test
	public void testTransformer() {
		SampleMultiplier multiplier = new SampleMultiplier(2);
		BenchmarkRun result = BenchmarkRunUtils.transform(SIMPLE_RUN, multiplier);
		
		TestUtils.assertBenchmarkRun(SIMPLE_RUN, 2, 0, 1, 5);
		TestUtils.assertBenchmarkRun(result, 4, 0, 2, 10);
	}
}
