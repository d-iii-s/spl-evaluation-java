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

import java.util.Collection;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.utils.RingBuffer;

/** Ring-buffer based data source.
 *
 */
public class RingDataSource implements DataSource {
	public static RingDataSource create(int maximumEpochs, int maximumRuns, int maximumSamples) {
		return new RingDataSource(maximumEpochs, maximumRuns, maximumSamples);
	}
	
	public static RingDataSource createWithLimitedNumberOfRuns(int maximumRuns) {
		return new RingDataSource(Integer.MAX_VALUE, maximumRuns, Integer.MAX_VALUE);
	}
	
	public static RingDataSource createWithLimitedNumberOfSamples(int maxSamples) {
		return new RingDataSource(Integer.MAX_VALUE, Integer.MAX_VALUE, maxSamples);
	}
	
	public static RingDataSource createUnlimited() {
		return new RingDataSource(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	private RingBuffer<BenchmarkRun> runs;
	private RingBuffer<Long> lastRun;
	private int maxSamples;
	private RingBuffer<DataSnapshotBuilder> epochs;
	
	private RingDataSource(int maxEpochs, int maxRuns, int maxSamples) {
		this.runs = new RingBuffer<>(maxRuns);
		this.maxSamples = maxSamples;
		this.lastRun = null;
		
		if (maxEpochs == 0) {
			epochs = RingBuffer.createEmpty();
		} else {
			epochs = new RingBuffer<>(maxEpochs);
		}
	}
	
	/** Start a new epoch when collecting data.
	 * 
	 * <p>
	 * Effectively it empties current runs and samples to an old epoch, starting
	 * with empty data source.
	 */
	public synchronized void startEpoch() {
		epochs.add(buildSnapshot());
		runs = new RingBuffer<>(runs.getRingSize());
		lastRun = new RingBuffer<>(maxSamples);
	}
	
	/** Start a new run.
	 */
	public synchronized void startRun() {
		if (lastRun != null) {
			runs.add(new ImmutableBenchmarkRun(lastRun.get()));
		}
		lastRun = new RingBuffer<>(maxSamples);
	}
	
	/** Add samples to the current run.
	 * 
	 * <p>
	 * This method automatically starts a new run if no run was started yet.
	 * 
	 * @param values Samples values to add.
	 */
	public synchronized void addSamples(long... values) {
		if (lastRun == null) {
			lastRun = new RingBuffer<>(maxSamples);
		}
		for (long v : values) {
			lastRun.add(v);
		}
	}

	@Override
	public synchronized DataSnapshot makeSnapshot() {
		DataSnapshotBuilder[] epochBuilders = epochs.get().toArray(new DataSnapshotBuilder[0]);
		
		DataSnapshot lastSnapshot = null;
		for (int i = epochBuilders.length - 1; i >= 0; i--) {
			epochBuilders[i].setPreviousEpoch(lastSnapshot);
			lastSnapshot = epochBuilders[i].create();
		}
		
		return buildSnapshot().setPreviousEpoch(lastSnapshot).create();
	}
	
	private DataSnapshotBuilder buildSnapshot() {
		DataSnapshotBuilder builder = new DataSnapshotBuilder();
		
		Collection<BenchmarkRun> currentRuns = runs.get();
		
		int start = 0;
		if ((lastRun != null) && (currentRuns.size() == runs.getRingSize())) {
			start = 1;
		}
		
		int index = 0;
		for (BenchmarkRun run : currentRuns) {
			if (index >= start) {
				builder.addRun(run);
			}
			index++;
		}
		
		if (lastRun != null) {
			builder.addRun(new ImmutableBenchmarkRun(lastRun.get()));
		}
		
		return builder;
	}
}

