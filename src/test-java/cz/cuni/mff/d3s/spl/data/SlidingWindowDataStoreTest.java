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

import static org.junit.Assert.*;

import org.junit.Test;

import cz.cuni.mff.d3s.spl.StatisticSnapshot;
import cz.cuni.mff.d3s.spl.data.SummaryStatisticSnapshot;

public class SlidingWindowDataStoreTest {
	private final static double EPSILON = 0.01;

	@Test
	public void emptySourceGivesZeros() {
		StatisticSnapshot stat = new SummaryStatisticSnapshot(new long[0]);
		assertEquals(0, stat.getSampleCount());
		assertEquals(0, stat.getArithmeticMean(), EPSILON);
	}
	
	@Test
	public void fewSamplesTest() {
		// 10 values, sum 101
		long[] samples = {9,10,11,9,12,9,10,10,11,10};
		
		StatisticSnapshot stat = new SummaryStatisticSnapshot(samples);
		assertEquals(10, stat.getSampleCount());
		assertEquals(10.1, stat.getArithmeticMean(), EPSILON);
	}
}
