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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cz.cuni.mff.d3s.spl.Formula;
import cz.cuni.mff.d3s.spl.Result;
import cz.cuni.mff.d3s.spl.formula.LogicAnd;
import cz.cuni.mff.d3s.spl.formula.LogicConst;

@RunWith(Parameterized.class)
public class LogicAndTest extends LogicOpTestBase {
	@Parameters(name = "{index}: {0} && {1} = {2}")
	public static Collection<Object[]> createParameters() {
		return Arrays.asList(new Object[][] {
			{ LogicConst.FALSE,   LogicConst.FALSE,   Result.FALSE },
			{ LogicConst.FALSE,   LogicConst.UNKNOWN, Result.FALSE },
			{ LogicConst.FALSE,   LogicConst.TRUE,    Result.FALSE },
			
			{ LogicConst.UNKNOWN, LogicConst.FALSE,   Result.FALSE },
			{ LogicConst.UNKNOWN, LogicConst.UNKNOWN, Result.CANNOT_COMPUTE },
			{ LogicConst.UNKNOWN, LogicConst.TRUE,    Result.CANNOT_COMPUTE },
			
			{ LogicConst.TRUE,    LogicConst.FALSE,   Result.FALSE },
			{ LogicConst.TRUE,    LogicConst.UNKNOWN, Result.CANNOT_COMPUTE },
			{ LogicConst.TRUE,    LogicConst.TRUE,    Result.TRUE },
		});
	}
	
	public LogicAndTest(final Formula left, final Formula right, final Result result) {
		super(left, right, result);
	}
	
	@Before
	public void setupFormula() {
		constructedFormula = new LogicAnd(leftSubformula, rightSubformula);
		constructedFormula.setInterpretation(interpretation);
	}
}
