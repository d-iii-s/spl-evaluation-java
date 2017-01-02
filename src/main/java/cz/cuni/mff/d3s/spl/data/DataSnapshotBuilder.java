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
	private final List<BenchmarkRun> runs = new LinkedList<>();
	private DataSnapshot prevEpoch = null;
		
	public DataSnapshotBuilder() {
	}
	
	public synchronized DataSnapshot create() {
		return new Snapshot(runs, prevEpoch);
	}
	
	public synchronized DataSnapshotBuilder setPreviousEpoch(final DataSnapshot snapshot) {
		prevEpoch = snapshot;
		return this;
	}
	
	public synchronized DataSnapshotBuilder addRun(final BenchmarkRun run) {
		runs.add(new ImmutableBenchmarkRun(run));
		return this;
	}
	
	public synchronized List<BenchmarkRun> getRuns(){
		return runs;
	}
	
	private static class Snapshot implements DataSnapshot {
		private List<BenchmarkRun> runs;
		private final DataSnapshot prevEpoch;
		
		public Snapshot(final List<BenchmarkRun> data, final DataSnapshot prev) {
			synchronized (data) {
				runs = new ArrayList<>(data.size());
				for (final BenchmarkRun run : data) {
					runs.add(run);
				}
			}
			prevEpoch = prev;
		}

		@Override
		public int getRunCount() {
			return runs.size();
		}

		@Override
		public BenchmarkRun getRun(final int index) {
			return runs.get(index);
		}

		@Override
		public Iterable<BenchmarkRun> getRuns() {
			return runs;
		}

		@Override
		public DataSnapshot getPreviousEpoch() {
			return prevEpoch;
		}

	}
}
