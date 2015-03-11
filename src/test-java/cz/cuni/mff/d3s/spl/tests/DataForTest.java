// piotaas-proposal
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

import java.util.Arrays;

import org.junit.Ignore;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunBuilder;

@Ignore
public class DataForTest implements DataSource {

	@Ignore
	private class SnapshotForTests implements DataSnapshot {
		private BenchmarkRun run;
		
		public SnapshotForTests(long mean, int sampleCount) {
			BenchmarkRunBuilder builder = new BenchmarkRunBuilder();
			for (int i = 0; i < sampleCount; i++) {
				builder.addSamples(mean);
			}
			run = builder.create();
		}

		@Override
		public int getRunCount() {
			return 1;
		}

		@Override
		public BenchmarkRun getRun(int index) {
			return run;
		}

		@Override
		public Iterable<BenchmarkRun> getRuns() {
			return Arrays.asList(run);
		}
	}
	
	private SnapshotForTests snapshot;
	
	public DataForTest(long mean, int sampleCount) {
		snapshot = new SnapshotForTests(mean, sampleCount);
	}
	
	@Override
	public DataSnapshot makeSnapshot() {
		return snapshot;
	}

}
