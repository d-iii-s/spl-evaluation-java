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

import cz.cuni.mff.d3s.spl.ComparisonResult;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.interpretation.KindergartenInterpretation;

@Ignore
public class InterpretationForTests implements Interpretation {
	public static final double DEFAULT_SIGNIFICANCE_LEVEL = 0.9;
	
	public static final int MINIMUM_SAMPLES_REQUIRED = 10;

	@Override
	public ComparisonResult compare(DataSnapshot left, DataSnapshot right) {
		return KindergartenInterpretation.INSTANCE.compare(left, right);
	}

	@Override
	public ComparisonResult compare(DataSnapshot data, double value) {
		return KindergartenInterpretation.INSTANCE.compare(data, value);
	}


}
