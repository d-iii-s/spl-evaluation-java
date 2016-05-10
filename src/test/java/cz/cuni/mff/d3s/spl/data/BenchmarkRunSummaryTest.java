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

import org.junit.Test;

import cz.cuni.mff.d3s.spl.BenchmarkRun;

public class BenchmarkRunSummaryTest {
	
	private static final double EPSILON = 0.00001;
	
	private static void assertSummary(BenchmarkRun run,
			long expectedSampleCount, double expectedMean, double expectedVariance) {
		BenchmarkRunSummary summary = new BenchmarkRunSummary(run);
		
		assertEquals(expectedSampleCount, summary.getSize());
		assertEquals(expectedMean, summary.getMean(), EPSILON);
		assertEquals(expectedVariance, summary.getVariance(), EPSILON);
	}
	
	@Test
	public void smokeTest() {
		assertSummary(new ImmutableBenchmarkRun(10, 10, 16),
				3, 12, 12);
	}
}
