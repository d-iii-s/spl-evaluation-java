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

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.utils.PrimitiveIterables;

/** Immutable implementation of BenchmarkRun that makes and returns
 * always copy of the initial data.
 *
 */
public class ImmutableBenchmarkRun implements BenchmarkRun {
	private final long[] data;

	public ImmutableBenchmarkRun(long... samples) {
		data = Arrays.copyOf(samples, samples.length);
	}

	public ImmutableBenchmarkRun(Collection<Long> samples) {
		this(samples, 0);
	}
	
	public ImmutableBenchmarkRun(Collection<Long> samples, int skip) {
		int size = samples.size() - skip;
		int idx = 0 - skip;
		data = new long[size];
		for (Long val : samples) {
			if (idx >= 0) data[idx] = val;
			idx++;
		}
	}
	
	public ImmutableBenchmarkRun(BenchmarkRun run) {
		synchronized (run) {
			data = new long[run.getSampleCount()];
			for (int i = 0; i < data.length; i++) {
				data[i] = run.getSample(i);
			}
		}
	}

	@Override
	public Iterable<Long> getSamples() {
		return PrimitiveIterables.makeIterable(data);
	}

	@Override
	public int getSampleCount() {
		return data.length;
	}

	@Override
	public long getSample(int index) {
		return data[index];
	}
}
