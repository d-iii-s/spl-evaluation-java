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
package cz.cuni.mff.d3s.spl.formula;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import cz.cuni.mff.d3s.spl.Result;
import cz.cuni.mff.d3s.spl.formula.Comparison;
import cz.cuni.mff.d3s.spl.formula.Comparison.Operator;
import cz.cuni.mff.d3s.spl.tests.DataForTest;
import cz.cuni.mff.d3s.spl.tests.InterpretationForTests;

public class ComparisonTest {

	private Comparison lessThan;
	
	@Before
	public void setUp() {
		lessThan = new Comparison("A", "B", Operator.LT);
		lessThan.setInterpretation(new InterpretationForTests());
	}
	
	@Test
	public void validValuesForLessThan() {
		lessThan.bind("A", new DataForTest(4, 100));
		lessThan.bind("B", new DataForTest(5, 100));
		assertEquals(Result.TRUE, lessThan.evaluate(InterpretationForTests.DEFAULT_SIGNIFICANCE_LEVEL));
	}
	
	@Test
	public void invalidValuesForLessThan() {
		lessThan.bind("A", new DataForTest(5, 100));
		lessThan.bind("B", new DataForTest(4, 100));
		assertEquals(Result.FALSE, lessThan.evaluate(InterpretationForTests.DEFAULT_SIGNIFICANCE_LEVEL));
	}
}
