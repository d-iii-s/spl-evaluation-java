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

import java.util.Arrays;
import java.util.Collection;

import cz.cuni.mff.d3s.spl.DataStore;
import cz.cuni.mff.d3s.spl.StatisticSnapshot;


public class ImmutableDataStore implements DataStore {
	
	private long[] data;
	
	/** Create from all values in the array.
	 * 
	 * @param samples Individual samples.
	 */
	public ImmutableDataStore(long[] samples) {
		this(samples, samples.length);
	}
	
	/** Create from first few values in the array.
	 * 
	 * @param samples Individual samples.
	 * @param count How many (starting at index 0) samples to actually use.
	 */
	public ImmutableDataStore(long[] samples, int count) {
		data = Arrays.copyOf(samples, count);
	}
	
	/** Create from all values in the collection.
	 * 
	 * @param samples Individual samples.
	 */
	public ImmutableDataStore(Collection<Long> samples) {
		 data = makeArray(samples);
	}
	
	private static long[] makeArray(Collection<Long> col) {
		long[] result = new long[col.size()];
		int index = 0;
		for (Long i : col) {
			result[index] = i;
			index++;
		}
		return result;
	}
	
	/** Get all the samples that constructs this data store.
	 * 
	 * @return Freely modifiable array with all the samples.
	 */
	public long[] getSamples() {
		return Arrays.copyOf(data, data.length);
	}
	
	@Override
	public synchronized StatisticSnapshot getStatisticSnapshot() {
		return new SummaryStatisticSnapshot(data);
	}

	@Override
	public synchronized void addValue(long when, long value) {
		throw new UnsupportedOperationException("Impossible to add data to ImmutableDataStore.");
	}

	@Override
	public synchronized void clear() {
		throw new UnsupportedOperationException("Impossible to clear data in ImmutableDataStore.");
	}

}
