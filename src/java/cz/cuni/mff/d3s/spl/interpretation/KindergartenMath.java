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
package cz.cuni.mff.d3s.spl.interpretation;

import cz.cuni.mff.d3s.spl.MathematicalInterpretation;
import cz.cuni.mff.d3s.spl.Result;
import cz.cuni.mff.d3s.spl.StatisticSnapshot;

/** Primitive interpretation that merely compares arithmetic means.
 *
 */
public class KindergartenMath implements MathematicalInterpretation {
	public static final KindergartenMath INSTANCE = new KindergartenMath();
	private static final int MIN_SAMPLE_COUNT = 2;
	
	
	@Override
	public Result isGreaterThan(StatisticSnapshot left, StatisticSnapshot right) {
		if ((left.getSampleCount() < MIN_SAMPLE_COUNT) || (right.getSampleCount() < MIN_SAMPLE_COUNT)) {
			return Result.CANNOT_COMPUTE;
		}
		double meanDifference = left.getArithmeticMean() - right.getArithmeticMean();
		if (meanDifference > 0) {
			return Result.TRUE;
		} else {
			return Result.FALSE;
		}
	}


	@Override
	public Result isSmallerThan(StatisticSnapshot variable, double constant) {
		if (variable.getSampleCount() < MIN_SAMPLE_COUNT) {
			return Result.CANNOT_COMPUTE;
		}
		double meanDifference = constant - variable.getArithmeticMean();
		if (meanDifference > 0) {
			return Result.TRUE;
		} else {
			return Result.FALSE;
		}
	}

}
