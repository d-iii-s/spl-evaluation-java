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

import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.Formula;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.Result;

/** Formula node: common parent for all logic operations.
 *
 */
abstract class LogicOp implements Formula {
	protected Formula left;
	protected Formula right;
	protected Set<String> variables;
	
	public LogicOp(Formula left, Formula right) {
		this.left = left;
		this.right = right;
		variables = new HashSet<>();
		variables.addAll(left.getVariables());
		variables.addAll(right.getVariables());
	}

	@Override
	public void setInterpretation(Interpretation interpretation) {
		left.setInterpretation(interpretation);
		right.setInterpretation(interpretation);
	}

	@Override
	public void bind(String variable, DataSource data) {
		bindToMultiple(variable, data, left, right);
	}
	
	static final void bindToMultiple(String variable, DataSource data, Formula... formulas) {
		boolean somewhereBinded = false;
		RuntimeException lastException = null;
		
		for (Formula f : formulas) {
			try {
				f.bind(variable, data);
				somewhereBinded = true;
			} catch (NoSuchElementException e) {
				lastException = e;
			}
		}
		if (!somewhereBinded) {
			if (lastException != null) {
				throw lastException;
			}
		}
	}
	
	@Override
	abstract public Result evaluate(double significanceLevel);

	@Override
	public Set<String> getVariables() {
		return null;
	}
}
