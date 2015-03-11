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

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.utils.ResizableRingBuffer;

/** Ring-buffer based data source.
 *
 */
public class RingDataSource implements DataSource {
	public static RingDataSource create(int maximumRuns, int maximumSamples) {
		return new RingDataSource(maximumRuns, maximumSamples);
	}
	
	public static RingDataSource createWithLimitedNumberOfRuns(int maximumRuns) {
		return new RingDataSource(maximumRuns, Integer.MAX_VALUE);
	}
	
	public static RingDataSource createWithLimitedNumberOfSamples(int maxSamples) {
		return new RingDataSource(Integer.MAX_VALUE, maxSamples);
	}
	
	public static RingDataSource createUnlimited() {
		return new RingDataSource(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	private ResizableRingBuffer<BenchmarkRun> runs;
	private ResizableRingBuffer<Long> lastRun;
	private int maxSamples;
	
	private RingDataSource(int maxRuns, int maxSamples) {
		runs = new ResizableRingBuffer<>(maxRuns);
		this.maxSamples = maxSamples;
	}
	
	public synchronized void startRun() {
		if (lastRun != null) {
			runs.add(new ImmutableBenchmarkRun(lastRun.get()));
		}
		lastRun = new ResizableRingBuffer<>(maxSamples);
	}
	
	public synchronized void addSamples(long... values) {
		if (lastRun == null) {
			lastRun = new ResizableRingBuffer<>(maxSamples);
		}
		for (long v : values) {
			lastRun.add(v);
		}
	}

	@Override
	public synchronized DataSnapshot makeSnapshot() {
		DataSnapshotBuilder builder = new DataSnapshotBuilder();
		for (BenchmarkRun run : runs.get()) {
			builder.addRun(run);
		}
		return null;
	}
}

