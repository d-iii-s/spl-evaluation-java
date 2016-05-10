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

import cz.cuni.mff.d3s.spl.ComparisonResult;
import cz.cuni.mff.d3s.spl.ComparisonResult.Relation;
import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.Formula;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.Result;

/** Formula node: actual comparison of two data sets.
 *
 */
public class Comparison implements Formula {
	public enum Operator {
		LT,
		GT
	}
	
	private class NamedDataSource {
		public DataSource data;
		public String name;
		public NamedDataSource(String name) {
			this.name = name;
		}
		public boolean bind(String sourceName, DataSource source) {
			if (this.name.equals(sourceName)) {
				data = source;
				return true;
			} else {
				return false;
			}
		}
		public boolean valid() {
			return data != null;
		}
	}
	
	private NamedDataSource left;
	private NamedDataSource right;
	private Operator operator;
	private Interpretation interpretation;
	
	public Comparison(String left, String right, Operator op) {
		this.left = new NamedDataSource(left);
		this.right = new NamedDataSource(right);
		operator = op;
	}
	
	@Override
	public void setInterpretation(Interpretation interpretation) {
		this.interpretation = interpretation;
	}

	@Override
	public void bind(String variable, DataSource data) {
		boolean leftOkay = left.bind(variable, data);
		boolean rightOkay = right.bind(variable, data);
		if (!leftOkay && !rightOkay) {
			throw new NoSuchElementException(String.format(
					"Uknown variable %s in comparison.", variable));
		}
	}

	@Override
	public Result evaluate(double significanceLevel) {
		if (!left.valid() || !right.valid()) {
			// TODO: throw exception?
			return Result.CANNOT_COMPUTE;
		}
		
		ComparisonResult cmp = interpretation.compare(left.data.makeSnapshot(), right.data.makeSnapshot());
		Relation rel = cmp.get(significanceLevel);
		
		return relationToResult(operator, rel);
	}

	public static Result relationToResult(Operator op, Relation rel) {
		if (rel == Relation.UNKNOWN) {
			return Result.CANNOT_COMPUTE;
		}
		
		if (op == Operator.LT) {
			if (rel == Relation.LESS_THAN) {
				return Result.TRUE;
			} else {
				return Result.FALSE;
			}
		} else if (op == Operator.GT) {
			if (rel == Relation.GREATER_THAN) {
				return Result.TRUE;
			} else {
				return Result.FALSE;
			}
		} else {
			assert false : "Unreachable branch reached :-(.";
			// Make the compiler happy.
			return Result.CANNOT_COMPUTE;
		}
	}
}
