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

import cz.cuni.mff.d3s.spl.Data;
import cz.cuni.mff.d3s.spl.Formula;
import cz.cuni.mff.d3s.spl.tests.DataForTest;

public class FormulaErrorHandlingTest {
	
	private Data source;
	
	@Before
	public void setupSource() {
		source = new DataForTest(5., 50);
	}
	
	@Test(expected=FormulaParsingException.class)
	public void emptyFormulaThrows() {
		@SuppressWarnings("unused")
		Formula formula = SplFormula.create("");
	}
	
	@Test(expected=FormulaParsingException.class)
	public void invalidIdentifierThrows() {
		@SuppressWarnings("unused")
		Formula formula = SplFormula.create("96xy < abc");
	}
	
	@Test(expected=FormulaParsingException.class)
	public void lexerErrorDetected() {
		@SuppressWarnings("unused")
		Formula formula = SplFormula.create("xy < abc &&& gh > xy");
	}
	
	@Test(expected=FormulaParsingException.class)
	public void grammarErrorDetected() {
		@SuppressWarnings("unused")
		Formula formula = SplFormula.create("xy < abc && gh > xy || baf");
	}
	
	@Test(expected=java.util.NoSuchElementException.class)
	public void bindingNonexistentSourceInSimpleFormulaThrows() {
		Formula formula = SimpleFormulas.createAsmallerThanB("abc", "def");
		formula.bind("xyz", source);
	}
	
	@Test(expected=java.util.NoSuchElementException.class)
	public void bindingNonexistentSourceInParsedFormulaThrows() {
		Formula formula = SplFormula.create("abc < def");
		formula.bind("xyz", source);
	}
}
