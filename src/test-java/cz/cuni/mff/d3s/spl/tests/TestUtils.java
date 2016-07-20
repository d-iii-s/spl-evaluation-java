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
package cz.cuni.mff.d3s.spl.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.DataSnapshot;

@Ignore
public class TestUtils {
	private static final Double[] DOUBLE_ARRAY_TYPE = new Double[0];
	
	public static void assertBenchmarkRun(BenchmarkRun run, double... samples) {
		List<Double> actual = new ArrayList<>(samples.length);
		for (Double s : run.getSamples()) {
			actual.add(s);
		}
		
		Double[] expected = new Double[samples.length];
		for (int i = 0; i < samples.length; i++) {
			expected[i] = samples[i];
		}
		
		assertArrayEquals(expected, actual.toArray(DOUBLE_ARRAY_TYPE));
	}
	
	public static void assertDataSnapshot(DataSnapshot snapshot, double[][] allSamples) {
		int runIndex = 0;

		for (double[] samples : allSamples) {
			if (samples == null) {
				/* Look into history. */
				assertEquals(runIndex, snapshot.getRunCount());
				snapshot = snapshot.getPreviousEpoch();
				runIndex = 0;
				continue;
			}
			
			assertBenchmarkRun(snapshot.getRun(runIndex), samples);
			
			runIndex++;
		}
		
		assertEquals(runIndex, snapshot.getRunCount());
		
		/* Either epochs are not supported or we have reach the last one. */
		try {
			DataSnapshot previousEpoch = snapshot.getPreviousEpoch();
			assertNull(previousEpoch);
		} catch (UnsupportedOperationException e) {
			assertTrue(true);
		}
		
		
	}
}
