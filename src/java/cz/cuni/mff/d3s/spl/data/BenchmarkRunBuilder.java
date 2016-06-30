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

import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.d3s.spl.BenchmarkRun;

/** Helper class for creating immutable benchmark run.
 *
 */
public class BenchmarkRunBuilder {
	private final List<Long> samples = new LinkedList<>();
	
	public BenchmarkRunBuilder() {
	}

	public BenchmarkRun create() {
		return create(0);
	}

	public BenchmarkRun create(int skip) {
		return new ImmutableBenchmarkRun(samples, skip);
	}

	public BenchmarkRun create(double skip) {
		return new ImmutableBenchmarkRun(samples, (int) (samples.size() * skip));
	}
	
	public synchronized BenchmarkRunBuilder addSamples(long... values) {
		for (long v : values) {
			samples.add(v);
		}
		return this;
	}	
}
