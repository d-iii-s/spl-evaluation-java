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
	private DataSnapshot prevEpoch = null;
		
	public DataSnapshotBuilder() {
	}
	
	public synchronized DataSnapshot create() {
		return create(runs, prevEpoch);
	}

	public synchronized DataSnapshot create(int skip) {
		// skip 'skip' elements from the beginning
		List<BenchmarkRun> skippedRuns = new LinkedList<>();
		for (BenchmarkRun run : runs) {
			skippedRuns.add(new ImmutableBenchmarkRun(run, skip));
		}
		return create(skippedRuns, prevEpoch);
	}

	public synchronized DataSnapshot create(double skip) {
		// skip 'skip' percent of elements from the beginning
		List<BenchmarkRun> skippedRuns = new LinkedList<>();
		for (BenchmarkRun run : runs) {
			skippedRuns.add(new ImmutableBenchmarkRun(run, (int) (run.getSampleCount() * skip)));
		}
		return create(skippedRuns, prevEpoch);
	}
	
	public synchronized DataSnapshotBuilder setPreviousEpoch(DataSnapshot snapshot) {
		prevEpoch = snapshot;
		return this;
	}
	
	public synchronized DataSnapshotBuilder addRun(BenchmarkRun run) {
		runs.add(new ImmutableBenchmarkRun(run));
		return this;
	}

	private DataSnapshot create(List<BenchmarkRun> runs, DataSnapshot prevEpoch) {
		return new Snapshot(runs, prevEpoch);
	}
	
	private static class Snapshot implements DataSnapshot {
		private List<BenchmarkRun> runs;
		private DataSnapshot prevEpoch;
		
		public Snapshot(List<BenchmarkRun> data, DataSnapshot prev) {
			synchronized (data) {
				runs = new ArrayList<>(data.size());
				for (BenchmarkRun run : data) {
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
		public BenchmarkRun getRun(int index) {
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
