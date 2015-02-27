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

import cz.cuni.mff.d3s.spl.DataStore;
import cz.cuni.mff.d3s.spl.StatisticSnapshot;

/** Circular buffer with fixed size for storing data samples.
 *
 */
public class SlidingWindowDataStore implements DataStore {
	private static final int DEFAULT_BUFFER_SIZE = 1000;
	
	private ArrayList<Long> buffer;
	private int bufferSize;
	
	public SlidingWindowDataStore() {
		this(DEFAULT_BUFFER_SIZE);
	}
	
	public SlidingWindowDataStore(int bufferSize) {
		if (bufferSize <= 0) {
			throw new IllegalArgumentException(String.format(
				"Buffer size must be greater than zero (not %d)", bufferSize));
		}
		this.bufferSize = bufferSize;
		this.buffer = new ArrayList<>(bufferSize);
	}
	
	public synchronized void add(ImmutableDataStore ds) {
		for (long s : ds.getSamples()) {
			addValue(0, s);
		}
	}

	@Override
	public synchronized StatisticSnapshot getStatisticSnapshot() {
		return new SummaryStatisticSnapshot(buffer);
	}

	@Override
	public synchronized void addValue(long when, long value) {
		// TODO: implement efficiently
		while (buffer.size() >= bufferSize) {
			buffer.remove(0);
		}
		buffer.add(value);
	}

	@Override
	public synchronized void clear() {
		buffer.clear();
	}

}
