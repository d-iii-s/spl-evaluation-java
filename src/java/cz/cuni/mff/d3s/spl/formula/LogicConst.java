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

import java.util.NoSuchElementException;

import cz.cuni.mff.d3s.spl.Data;
import cz.cuni.mff.d3s.spl.Formula;
import cz.cuni.mff.d3s.spl.MathematicalInterpretation;
import cz.cuni.mff.d3s.spl.Result;

/** Formula node: logic constant.
 *
 */
public final class LogicConst implements Formula {
	public static final LogicConst TRUE = new LogicConst(Result.TRUE);
	public static final LogicConst FALSE = new LogicConst(Result.FALSE);
	public static final LogicConst UNKNOWN = new LogicConst(Result.CANNOT_COMPUTE);
	
	private final Result evaluationResult;
	
	private LogicConst(Result result) {
		evaluationResult = result;
	}

	@Override
	public void setInterpretation(MathematicalInterpretation interpretation) {
		/* Do nothing. */
	}

	@Override
	public void bind(String variable, Data data) {
		throw new NoSuchElementException("Constant cannot be binded.");
	}

	@Override
	public Result evaluate() {
		return evaluationResult;
	}
	
	@Override
	public String toString() {
		switch (evaluationResult) {
		case CANNOT_COMPUTE:
			return "uknown";
		case FALSE:
			return "false";
		case TRUE:
			return "true";
		}
		assert false;
		return null;
	}
}
