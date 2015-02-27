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

/** Extends the read-only Data with possibility to add new samples.
 */
public interface DataStore extends Data {
	/** Add a new sample to the data source.
	 * 
	 * @param when Wall-clock time when the sample was collected.
	 * @param value Value of the sample (such as execution duration).
	 */
	void addValue(long when, long value);
	
	/** Remove all data.
	 * 
	 */
	void clear();
}
