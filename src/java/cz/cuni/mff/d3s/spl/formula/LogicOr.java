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

import cz.cuni.mff.d3s.spl.Formula;
import cz.cuni.mff.d3s.spl.Result;

/** Formula node: disjunction of two subformulas.
 *
 */
public class LogicOr extends LogicOp {

	public LogicOr(Formula left, Formula right) {
		super(left, right);
	}

	/*
	 * We are using Kleene three-value logic.
	 */
	@Override
	public Result evaluate(double significanceLevel) {
		Result leftResult = left.evaluate(significanceLevel);
		
		/*
		 * If the left one is TRUE, we do not need
		 * to evaluate the right one.
		 */
		if (leftResult == Result.TRUE) {
			return Result.TRUE;
		}
		
		Result rightResult = right.evaluate(significanceLevel);
		
		/*
		 * The same works other way round.
		 */
		if (rightResult == Result.TRUE) {
			return Result.TRUE;
		}
		
		/*
		 * FALSE is returned only if both are false, otherwise
		 * we return CANNOT_COMPUTE.
		 */
		if ((rightResult == Result.FALSE) && (leftResult == Result.FALSE)) {
			return Result.FALSE;
		} else {
			return Result.CANNOT_COMPUTE;
		}
	}

}
