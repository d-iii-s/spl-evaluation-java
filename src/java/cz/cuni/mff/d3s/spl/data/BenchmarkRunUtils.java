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

/** Helper methods for working with the BenchmarkRun interface.
 */
public class BenchmarkRunUtils  {
	
	/** Merge individual benchmark runs into a single one.
	 * 
	 * <p>
	 * The sample ordering preserves order inside a run and
	 * runs are ordered in the same way they were iterated.
	 * 
	 * @param runs Individual runs to merge.
	 * @return Merged run.
	 */
	public static BenchmarkRun merge(Iterable<BenchmarkRun> runs) {
		BenchmarkRunBuilder builder = new BenchmarkRunBuilder();
		
		synchronized (runs) {
			for (BenchmarkRun r : runs) {
				synchronized (r) {
					for (long l : r.getSamples()) {
						builder.addSamples(l);
					}
				}
			}
		}
		
		return builder.create();
	}
}
