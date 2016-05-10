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

/** Represents data from a single benchmark run.
 *
 * Single benchmark run can produce several (thousands of) samples.
 *
 * The run may correspond to a JVM restart, individual samples inside the
 * run the correspond to individual invocations of a certain method, for
 * example
 */
public interface BenchmarkRun {
	/** Get all samples.
	 * 
	 *  @return All samples in the order they were added.
	 */
	Iterable<Long> getSamples();
	
	/** Get number of samples in this run.
	 * 
	 * @return Number of samples in this run.
	 */
	int getSampleCount();
	
	/** Get a single sample.
	 * 
	 * @param index Sample (zero based) index we want to retrieve.
	 * @return Sample value at given index.
	 * @throws IndexOutOfBoundsException When the index is either negative or
	 * greater or equal to sample count.
	 */
	long getSample(int index);
}
