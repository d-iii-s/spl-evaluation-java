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

import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.interpretation.KindergartenInterpretation;

/** Wrapper class for holding formula defaults.
 *
 */
public class Defaults {
	private static Interpretation defaultInterpretation = KindergartenInterpretation.INSTANCE;
	
	/** Set default SPL interpretation.
	 * 
	 * @param intr New default interpretation.
	 */
	public static synchronized void setInterpretation(Interpretation intr) {
		defaultInterpretation = intr;
	}
	
	/** Get default SPL interpretation.
	 * 
	 * <p>
	 * Unless you override the initial value by calling
	 * Defaults#setInterpretation(), KindergartenInterpretation will
	 * be returned.
	 * 
	 * @return Default SPL interpretation.
	 */
	public static synchronized Interpretation  getInterpretation() {
		return defaultInterpretation;
	}
}
