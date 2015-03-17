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
package cz.cuni.mff.d3s.spl.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import cz.cuni.mff.d3s.spl.Formula;
import cz.cuni.mff.d3s.spl.Result;
import cz.cuni.mff.d3s.spl.data.RingDataSource;
import cz.cuni.mff.d3s.spl.formula.SplFormula;

/** Simple example for using SPL evaluation to determine which
 * of the collections is the fastest.
 *
 */
public class NaiveComparisonOfCollections {
	private static final int SAMPLES_COLLECTED = 100;
	private static final int COLLECTION_SIZE = 1000;
	private static final int LOOPS = SAMPLES_COLLECTED * 10000;
	
	public static class Benchmark {
		public Collection<Integer> collection;
		public RingDataSource data;
		public volatile boolean blackHole;
		
		public Benchmark(Collection<Integer> impl, int collectionSize) {
			collection = impl;
			for (int i = 0; i < collectionSize; i++) {
				collection.add(i);
			}
			data = RingDataSource.create(0, 1, SAMPLES_COLLECTED);
			data.startRun();
		}
		
		public void benchmark() {
			Integer obj = collection.size() + 1;
			long start = System.nanoTime();
			blackHole = collection.contains(obj);
			long end = System.nanoTime();
			data.addSamples(end - start);
		}
		
		public String getName() {
			return String.format("%s[%d]", collection.getClass().getName(),
					collection.size());
		}
	}

	public static void main(String[] args) {
		List<Benchmark> benchmarks = new LinkedList<>();
		benchmarks.add(new Benchmark(new LinkedList<Integer>(), COLLECTION_SIZE));
		benchmarks.add(new Benchmark(new ArrayList<Integer>(), COLLECTION_SIZE));
		benchmarks.add(new Benchmark(new Vector<Integer>(), COLLECTION_SIZE));
		
		for (int i = 0; i < LOOPS; i++) {
			Collections.shuffle(benchmarks);
			
			for (Benchmark b : benchmarks) {
				b.benchmark();
			}
		}
		
		Formula formula = SplFormula.create("left < right");
		for (Benchmark left : benchmarks) {
			for (Benchmark right : benchmarks) {
				if (left == right) {
					continue;
				}
				formula.bind("left", left.data);
				formula.bind("right", right.data);
				
				Result result = formula.evaluate(0.95);
				System.out.printf("%s < %s: %s\n", left.getName(),
						right.getName(), result);
						
			}
		}
	}

}
