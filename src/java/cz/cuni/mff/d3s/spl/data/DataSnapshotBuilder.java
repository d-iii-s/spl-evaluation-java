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
import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.DataSnapshot;


/** Helper class for creating data snapshots.
 *
 */
public class DataSnapshotBuilder {
	private List<BenchmarkRun> runs = new LinkedList<>();
		
	public DataSnapshotBuilder() {
	}
	
	public synchronized DataSnapshot create() {
		return new Snapshot(runs);
	}
	
	public DataSnapshotBuilder addRun(BenchmarkRun run) {
		runs.add(new ImmutableBenchmarkRun(run));
		return this;
	}
	
	private static class Snapshot implements DataSnapshot {
		private List<BenchmarkRun> runs;
		
		public Snapshot(List<BenchmarkRun> data) {
			synchronized (data) {
				runs = new ArrayList<>(data.size());
				for (BenchmarkRun run : data) {
					runs.add(run);
				}
			}
		}

		@Override
		public int getRunCount() {
			return runs.size();
		}

		@Override
		public BenchmarkRun getRun(int index) {
			return runs.get(index);
		}

		@Override
		public Iterable<BenchmarkRun> getRuns() {
			return runs;
		}

		@Override
		public DataSnapshot getPreviousEpoch() {
			throw new UnsupportedOperationException("Not available.");
		}

	}
}
