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
package cz.cuni.mff.d3s.spl;

/** A snapshot of the performance data.
 */
@SuppressWarnings("javadoc")
public interface DataSnapshot {
	/** Tells number of runs contained in this snapshot.
	 *
	 * @return Number of runs in the snapshot. 
	 */
	int getRunCount();
	
	/** Provides access to a specific run inside the snapshot.
	 * 
	 * @param index Run (zero based) index we want to retrieve.
	 * @return Run at given index.
	 * @throws IndexOutOfBoundsException When the index is either negative or
	 * greater or equal to run count.
	 */
	BenchmarkRun getRun(int index);
	
	/** Provides iteration over all runs inside the snapshot.
	 * 
	 * @return All runs in the order they were added.
	 */
	Iterable<BenchmarkRun> getRuns();
}

