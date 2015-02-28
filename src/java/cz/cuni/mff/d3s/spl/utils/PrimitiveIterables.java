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

import java.util.Iterator;

/** Iterables over primitive types.
 */
public class PrimitiveIterables {
	
	public static Iterable<Long> makeIterable(final long[] array) {
		return new Iterable<Long>() {
			@Override
			public Iterator<Long> iterator() {
				return new LongIterator(array);
			}
		};
	}
	
	private static class LongIterator implements Iterator<Long> {
		private final long[] array;
		private int index;
		
		public LongIterator(final long[] data) {
			array = data;
			index = 0;
		}
		
		@Override
		public boolean hasNext() {
			return index < array.length;
		}

		@Override
		public Long next() {
			return array[index++];
		}
	}
}
