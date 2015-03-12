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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

public class ArrayUtilsTest {
	private static final double EPSILON = 0.00001;
	
	@Test
	public void makeArraySmokeTest() {
		assertArrayEquals(new double[] { 1.,  2., 5. },
			ArrayUtils.makeArray(Arrays.asList(1.,  2., 5.)), EPSILON);
	}
	
	@Test
	public void makeArrayRecastedSmokeTest() {
		Collection<Long> longs = Arrays.asList((long)1, (long)2, (long)5);
		
		assertArrayEquals(new double[] { 1.,  2., 5. },
			ArrayUtils.makeArrayWithRecast(longs), EPSILON);
	}
}
