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

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

/** Utility class to read data from streams.
 *
 */
public class DataStoreReader {
	
	/** Reads samples from a data stream.
	 * 
	 * @param is Input stream with data.
	 * @return Data store with the samples.
	 */
	public static ImmutableDataStore read(InputStream is) {
		Collection<Long> samples = new LinkedList<>();
		Scanner sc = new Scanner(is);
		while (sc.hasNextLong()) {
			samples.add(sc.nextLong());
		}
		sc.close();
		
		return new ImmutableDataStore(samples);
	}
}
