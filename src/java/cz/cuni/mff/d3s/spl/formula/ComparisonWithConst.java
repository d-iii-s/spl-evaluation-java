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

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import cz.cuni.mff.d3s.spl.ComparisonResult;
import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.Formula;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.Result;

/** Formula node: actual comparison of a data set with a constant.
*
*/
public class ComparisonWithConst implements Formula {
	/* Internally, we always assume SOURCE OP CONST */
	
	private DataSource source;
	private final String sourceName;
	private final double constant;
	private final Comparison.Operator operator;
	private Interpretation interpretation;
	
	public ComparisonWithConst(String leftVariable, double rightConstant, Comparison.Operator op) {
		sourceName = leftVariable;
		constant = rightConstant;
		operator = op;
	}
	
	@Override
	public void setInterpretation(Interpretation interpretation) {
		this.interpretation = interpretation;
	}

	@Override
	public void bind(String variable, DataSource data) {
		if (sourceName.equals(variable)) {
			source = data;
		} else {
			throw new NoSuchElementException(String.format(
					"Uknown variable %s in comparison.", variable));
		}
	}

	@Override
	public Result evaluate(double significanceLevel) {
		if (source == null) {
			// TODO: throw exception?
			return Result.CANNOT_COMPUTE;
		}
		
		ComparisonResult result = interpretation.compare(source.makeSnapshot(), constant);
		
		ComparisonResult.Relation rel = result.get(significanceLevel);
		
		return Comparison.relationToResult(operator, rel);
	}

	@Override
	public Set<String> getVariables() {
		Set<String> variables = new HashSet<>();
		variables.add(sourceName);
		return variables;
	}

}
