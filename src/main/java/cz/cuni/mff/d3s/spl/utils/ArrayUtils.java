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
package cz.cuni.mff.d3s.spl.utils;

import java.util.Collection;

/** Helper methods to convert from collections of boxed primitives to arrays.
 */
public class ArrayUtils {
	
	/** Create double array from collection of boxed doubles.
	 * 
	 * @param iter Collection of boxed doubles.
	 * @return Copy of the collection as an array.
	 */
	public static double[] makeArray(Collection<Double> iter) {
		synchronized (iter) {
			double[] result = new double[iter.size()];
			
			int index = 0;
			for (Double d : iter) {
				result[index] = d;
				index++;
			}
			
			return result;
		}
	}
	
	/** Create double array from collection of boxed longs.
	 * 
	 * @param iter Collection of boxed longs.
	 * @return Copy of the collection as an array.
	 */
	public static double[] makeArrayWithRecast(Collection<Long> iter) {
		synchronized (iter) {
			double[] result = new double[iter.size()];
			
			int index = 0;
			for (Long l : iter) {
				result[index] = l;
				index++;
			}
			
			return result;
		}
	}
}
