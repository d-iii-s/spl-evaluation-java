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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.ComparisonResult;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.data.DataSnapshotBuilder;
import cz.cuni.mff.d3s.spl.data.ImmutableBenchmarkRun;

public class WelchTestInterpretationTest  {
	private static DataSnapshot SNAPSHOT_1;
	private static DataSnapshot SNAPSHOT_2;
	
	private static final long[] RUN_1 = new long[] {1, 2, 1, 1 };
	private static final long[] RUN_2 = new long[] {5, 5, 6, 5 };
	
	@Before
	public void prepareSnapshots() {
		SNAPSHOT_1 = makeSnapshotFromSingleRun(RUN_1);
		SNAPSHOT_2 = makeSnapshotFromSingleRun(RUN_2);
	}
	
	private DataSnapshot makeSnapshotFromSingleRun(long... samples) {
		BenchmarkRun run = new ImmutableBenchmarkRun(samples);
		
		DataSnapshotBuilder builder = new DataSnapshotBuilder();
		builder.addRun(run);
		
		return builder.create();
	}
		
	
	@Test
	public void smokeTest() {
		Interpretation intr = new WelchTestInterpretation();
		ComparisonResult result = intr.compare(SNAPSHOT_1, SNAPSHOT_2);
		
		assertEquals(-11.313708499, result.getStatistic(), 0.000001);
		assertEquals(ComparisonResult.Relation.LESS_THAN, result.get(0.2));
	}
}
