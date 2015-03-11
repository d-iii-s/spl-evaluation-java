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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cz.cuni.mff.d3s.spl.Formula;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.Result;
import cz.cuni.mff.d3s.spl.tests.InterpretationForTests;

@Ignore
public class LogicOpTestBase {
	protected Interpretation interpretation;
	protected final Formula leftSubformula;
	protected final Formula rightSubformula;
	protected final Result expectedResult;
	protected Formula constructedFormula;

	public LogicOpTestBase(final Formula left, final Formula right, final Result result) {
		expectedResult = result;
		leftSubformula = left;
		rightSubformula = right;
	}
	
	@Before
	public void setupInterpretation() {
		interpretation = new InterpretationForTests();
	}
	
	@Test
	public void evaluationTest() {
		assertEquals(expectedResult, constructedFormula.evaluate(InterpretationForTests.DEFAULT_SIGNIFICANCE_LEVEL));
	}
}
