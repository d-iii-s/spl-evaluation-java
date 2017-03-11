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

import java.util.Set;

/** SPL formula.
 * 
 * It is up to implementing classes to construct the actual formula
 * and provide the implementation how to evaluate it.
 * 
 * The expected usage is following: user creates the formula, using
 * aliases for the random variables.
 * She then binds the aliases to actual random variables.
 * Before evaluating, it is necessary to set the mathematical apparatus
 * used for evaluating.
 * The apparatus can be used to determine relation between two random variables
 * (greater/smaller, etc.).
 * The evaluate then returns result of the evaluation at the time of calling
 * the function.
 */
public interface Formula {
	
	/** Set how to evaluate statistical tests and other mathematical
	 * operations.
	 * 
	 * @param interpretation The mathematical interpretation to use.
	 */
	void setInterpretation(Interpretation interpretation);
	
	/** Bind given variable alias to a concrete data source.
	 * 
	 * This method must be called for all variables in the formula prior
	 * formula evaluation.
	 * 
	 * @param variable Variable name as used in the formula.
	 * @param data Actual data source to use (must not be <code>null</code>).
	 */
	void bind(String variable, DataSource data);
	
	/** Evaluate the formula.
	 * 
	 * @param significanceLevel Requested significance level of the result.
	 * @return Whether the formula evaluates to true.
	 */
	Result evaluate(double significanceLevel);

	/** Get set of variables, which needs to be bound before evaluation.
	 *
	 * @return Set of variables inside formula.
	 */
	Set<String> getVariables();
}
