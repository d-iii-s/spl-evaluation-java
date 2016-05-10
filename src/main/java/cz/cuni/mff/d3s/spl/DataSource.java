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

/** Provides access to series of a performance data.
 * 
 * It is expected that the data are continuously changing (new samples
 * coming in), user gets a consistent snapshot only through use of the
 * makeSnapshot() call.
 */
public interface DataSource {
	/** Get consistent view on this data source.
	 * 
	 * @return Immutable snapshot of the data.
	 */
	DataSnapshot makeSnapshot();
}

