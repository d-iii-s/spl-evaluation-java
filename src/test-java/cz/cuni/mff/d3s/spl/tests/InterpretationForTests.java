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
package cz.cuni.mff.d3s.spl.tests;

import org.junit.Ignore;

import cz.cuni.mff.d3s.spl.MathematicalInterpretation;
import cz.cuni.mff.d3s.spl.Result;
import cz.cuni.mff.d3s.spl.StatisticSnapshot;

@Ignore
public class InterpretationForTests implements MathematicalInterpretation {
	public static final int MINIMUM_SAMPLES_REQUIRED = 10;

	@Override
	public Result isGreaterThan(StatisticSnapshot left, StatisticSnapshot right) {
		if ((left.getSampleCount() < MINIMUM_SAMPLES_REQUIRED)
				|| (right.getSampleCount() < MINIMUM_SAMPLES_REQUIRED)) {
			return Result.CANNOT_COMPUTE;
		}
		return isGreaterThan(left.getArithmeticMean(), right.getArithmeticMean());
	}

	@Override
	public Result isSmallerThan(StatisticSnapshot variable, double constant) {
		if (variable.getSampleCount() < MINIMUM_SAMPLES_REQUIRED) {
			return Result.CANNOT_COMPUTE;
		}
		return isGreaterThan(constant, variable.getArithmeticMean());
	}
	
	private Result isGreaterThan(double left, double right) {
		if (left - right > 0.0) {
			return Result.TRUE;
		} else {
			return Result.FALSE;
		}
	}

}
