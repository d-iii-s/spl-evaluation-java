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
	private static final Long[] LONG_ARRAY_TYPE = new Long[0];
	
	public static void assertBenchmarkRun(BenchmarkRun run, long... samples) {
		List<Long> actual = new ArrayList<>(samples.length);
		for (Long s : run.getSamples()) {
			actual.add(s);
		}
		
		Long[] expected = new Long[samples.length];
		for (int i = 0; i < samples.length; i++) {
			expected[i] = samples[i];
		}
		
		assertArrayEquals(expected, actual.toArray(LONG_ARRAY_TYPE)); 
	}
	
	public static void assertDataSnapshot(DataSnapshot snapshot, long[][] samples) {
		assertEquals(snapshot.getRunCount(), samples.length);
		
		for (int i = 0; i < samples.length; i++) {
			assertBenchmarkRun(snapshot.getRun(i), samples[i]);
		}
	}
}
