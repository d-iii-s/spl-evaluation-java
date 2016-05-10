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

import java.util.List;

/** Various string utilities.
 */
public class StringUtils {
	
	/** Perform reverse of split on a list.
	 * 
	 * @param list Members to be joined.
	 * @return String representation of all members, connected by comma.
	 */
	public static String join(List<?> list) {
		if (list.isEmpty()) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		boolean afterFirst = false;
		for (Object o : list) {
			if (afterFirst) {
				result.append(',');
			}
			result.append(o.toString());
			afterFirst = true;
		}
		return result.toString();
	}
	
	/** Perform reverse of split on a list.
	 * 
	 * @param array Members to be joined.
	 * @param sep Separator between individual members of the array.
	 * @return String representation of all members, delimited by the separator.
	 */
	public static String join(Object[] array, String sep) {
		if (array.length == 0) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		boolean afterFirst = false;
		for (Object o : array) {
			if (afterFirst) {
				result.append(sep);
			}
			result.append(o.toString());
			afterFirst = true;
		}
		return result.toString();
	}
	
	/** Perform revers of split on all arguments.
	 * 
	 * @param objects Objects to be joined.
	 * @return String representation of all objects, connected by comma.
	 */
	public static String join(Object... objects) {
		return join(objects, ",");
	}
	
	/** Format time in reasonable units.
	 *  
	 * @param nanos Time duration in nanoseconds.
	 * @return Reasonable representation of the given name with unit appended.
	 */
	public static String formatTimeUnits(double nanos) {
		if (nanos < 1000) {
			return String.format("%.0fns", nanos);
		}
		double micros = nanos / 1000;
		if (micros < 1000) {
			return String.format("%.0fus", micros);
		}
		double millis = micros / 1000;
		if (millis < 1000) {
			return String.format("%.0fms", millis);
		}
		double sec = millis / 1000;
		millis = millis - sec * 1000;
		return String.format("%.0fs %.fms", sec, millis);
	}
}
