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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/** Simple ring buffer with option of unlimited size.
 */
public class RingBuffer<E> {
	
	private List<E> data = new LinkedList<>();
	private int maxSize = Integer.MAX_VALUE;
	
	/** Creates ring-buffer with virtually unlimited size.
	 * 
	 * <p>
	 * The actual size is Integer.MAX_VALUE.
	 */
	public RingBuffer() {
	}
	
	public RingBuffer(int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("Ring buffer size must be positive");
		}
		maxSize = size;
	}
	
	public synchronized void add(E element) {
		data.add(element);
		if (data.size() > maxSize) {
			data.remove(0);
		}
	}
	
	public synchronized Collection<E> get() {
		return Collections.unmodifiableCollection(data);
	}
	
	public int getRingSize() {
		return maxSize;
	}
}
