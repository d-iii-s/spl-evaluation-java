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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cz.cuni.mff.d3s.spl.Result;
import cz.cuni.mff.d3s.spl.formula.LogicConst;
import cz.cuni.mff.d3s.spl.tests.DataForTest;

public class LogicConstTest {

	@Test(expected=java.util.NoSuchElementException.class)
	public void constantContainsNoVariable() {
		LogicConst.TRUE.bind("abc", new DataForTest(5., 50));
	}
	
	@Test
	public void evaluateTrue() {
		assertEquals(Result.TRUE, LogicConst.TRUE.evaluate());
	}
	
	@Test
	public void evaluateFalse() {
		assertEquals(Result.FALSE, LogicConst.FALSE.evaluate());
	}
	
	@Test
	public void evaluateCannotCompute() {
		assertEquals(Result.CANNOT_COMPUTE, LogicConst.UNKNOWN.evaluate());
	}
}
