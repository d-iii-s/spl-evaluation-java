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

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

public class RingBufferTest {
	private static final Integer[] INTEGER_ARRAY_TYPE = new Integer[0];
	
	private static void assertRBContent(RingBuffer<Integer> buffer,
			Integer... expected) {
		Collection<Integer> data = buffer.get();
		Integer[] actual  = data.toArray(INTEGER_ARRAY_TYPE);
		assertArrayEquals(expected, actual);
	}
	
	@Test
	public void smokeTest() {
		RingBuffer<Integer> buffer = new RingBuffer<>(3);
		buffer.add(0);
		assertRBContent(buffer, 0);
		
		buffer.add(1);
		assertRBContent(buffer, 0, 1);
		
		buffer.add(2);
		assertRBContent(buffer, 0, 1, 2);
		
		buffer.add(3);
		assertRBContent(buffer, 1, 2, 3);
	}
}
