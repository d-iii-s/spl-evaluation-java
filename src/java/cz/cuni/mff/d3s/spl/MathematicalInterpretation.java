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
package cz.cuni.mff.d3s.spl;

/** Encapsulates mathematical methods and approaches needed for evaluation
 * of SPL formulas.
 * 
 * The methods include statistical tests for deciding whether some random
 * variable is greater than another one or for finding regressions.
 */
public interface MathematicalInterpretation {
	/** Decides whether one random variable is greater than another one.
	 * 
	 * @param left The supposed greater variable.
	 * @param right The supposed smaller variable.
	 * @return Whether it is statistically sound to say that <code>left</code>
	 * is greater than <code>right</code>.
	 */
	Result isGreaterThan(StatisticSnapshot left, StatisticSnapshot right);
	
	/** Decides whether random variable is smaller than given constant.
	 * 
	 * @param variable Supposedly smaller random variable.
	 * @param constant Constant to compare with.
	 * @return Whether it is statistically sound to say that
	 * <code>variable</code> is smaller than <code>constant</code>.
	 */
	Result isSmallerThan(StatisticSnapshot variable, double constant);
}
