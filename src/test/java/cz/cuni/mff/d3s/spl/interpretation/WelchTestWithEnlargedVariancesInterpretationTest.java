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
package cz.cuni.mff.d3s.spl.interpretation;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.ComparisonResult;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.data.DataSnapshotBuilder;
import cz.cuni.mff.d3s.spl.data.ImmutableBenchmarkRun;

public class WelchTestWithEnlargedVariancesInterpretationTest {
	private static final double EPSILON = 0.00001;
	
	private static final long[] RUN_1_1 = new long[] {10, 10, 12, 10 };
	private static final long[] RUN_1_2 = new long[] {10, 12, 9, 10 };
	private static final long[] RUN_2_1 = new long[] {10, 10, 11, 10 };
	private static final long[] RUN_2_2 = new long[] {10, 10, 10, 9 };
	
	private Interpretation interpretation;
	
	private DataSnapshot SNAPSHOT_1;
	private DataSnapshot SNAPSHOT_2;
	
	@Before
	public void prepareSnapshots() {
		SNAPSHOT_1 = makeSnapshot(RUN_1_1, RUN_1_2);
		SNAPSHOT_2 = makeSnapshot(RUN_2_1, RUN_2_2);
	}
	
	@Before
	public void prepareInterpretation() {
		interpretation = new WelchTestWithEnlargedVariancesInterpretation();
	}
	
	private DataSnapshot makeSnapshot(long[]... runs) {		
		DataSnapshotBuilder builder = new DataSnapshotBuilder();
		for (long[] samples : runs) {
			BenchmarkRun run = new ImmutableBenchmarkRun(samples);
			builder.addRun(run);
		}
		
		return builder.create();
	}
		
	
	@Test
	public void smokeTestForTwoSnapshots() {
		ComparisonResult result = interpretation.compare(SNAPSHOT_1, SNAPSHOT_2);
		
		assertEquals(0.7947194, result.getStatistic(), 0.000001);
		assertEquals(ComparisonResult.Relation.GREATER_THAN, result.get(0.2));
		assertEquals(-1.281552, result.getCriticalValue(0.1), EPSILON);
	}
	
	@Test
	public void smokeTestForOneSnapshot() {
		@SuppressWarnings("unused")
		ComparisonResult result = interpretation.compare(SNAPSHOT_1, 10.);
		
		// FIXME - compute
	}
}
