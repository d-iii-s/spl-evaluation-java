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

import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.d3s.spl.DataSnapshot;

public class DataSnapshotBuilderTest {
	
	private DataSnapshotBuilder builder;
	
	@Before
	public void setUp() {
		builder = new DataSnapshotBuilder();
	}
	
	@Test
	public void emptySnapshot() {
		DataSnapshot snapshot = builder.create();
		assertEquals(0, snapshot.getRunCount());
	}
	
	@Test
	public void singleRun() {
		builder.addRun(new ImmutableBenchmarkRun(0, 1, 2));
		
		DataSnapshot snapshot = builder.create();
		assertEquals(1, snapshot.getRunCount());
	}
}
