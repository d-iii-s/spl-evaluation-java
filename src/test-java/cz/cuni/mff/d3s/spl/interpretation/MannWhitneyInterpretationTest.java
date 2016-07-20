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
import cz.cuni.mff.d3s.spl.data.DataSnapshotBuilder;
import cz.cuni.mff.d3s.spl.data.ImmutableBenchmarkRun;

public class MannWhitneyInterpretationTest  {
	private DataSnapshot SNAPSHOT_1;
	private DataSnapshot SNAPSHOT_1_PERM;
	private DataSnapshot SNAPSHOT_2;
	private MannWhitneyInterpretation interpretation;
	
	private static final double[] RUN_1 = new double[] {1, 2, 3, 0 };
	private static final double[] RUN_1_PERM = new double[] {2, 1, 0, 3 };
	private static final double[] RUN_2 = new double[] {7, 5, 6, 8 };
	
	@Before
	public void prepareSnapshots() {
		SNAPSHOT_1 = makeSnapshotFromSingleRun(RUN_1);
		SNAPSHOT_1_PERM = makeSnapshotFromSingleRun(RUN_1_PERM);
		SNAPSHOT_2 = makeSnapshotFromSingleRun(RUN_2);
	}
	
	@Before
	public void prepareInterpretation() {
		interpretation = new MannWhitneyInterpretation();
	}
	
	private DataSnapshot makeSnapshotFromSingleRun(double... samples) {
		BenchmarkRun run = new ImmutableBenchmarkRun(samples);
		
		DataSnapshotBuilder builder = new DataSnapshotBuilder();
		builder.addRun(run);
		
		return builder.create();
	}
		
	
	@Test
	public void smokeTestForTwoSnapshots() {
		ComparisonResult result = interpretation.compare(SNAPSHOT_1, SNAPSHOT_2);
		
		assertEquals(-2.309, result.getStatistic(), 0.001);
		assertEquals(ComparisonResult.Relation.LESS_THAN, result.get(0.9));
	}
	
	@Test
	public void equalSamples() {
		ComparisonResult result = interpretation.compare(SNAPSHOT_1, SNAPSHOT_1_PERM);
		
		assertEquals(0.0, result.getStatistic(), 0.001);
		assertEquals(ComparisonResult.Relation.EQUAL, result.get(0.9));
	}
	
	@Test
	public void smokeTestForOneSnapshot() {
		@SuppressWarnings("unused")
		ComparisonResult result = interpretation.compare(SNAPSHOT_1, 10.);
		
		// FIXME - compute
	}
}
