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
			data = RingDataSource.create(1, SAMPLES_COLLECTED);
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
