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
import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.Formula;
import cz.cuni.mff.d3s.spl.Interpretation;
import cz.cuni.mff.d3s.spl.Result;
import cz.cuni.mff.d3s.spl.formula.Comparison.Operator;
import cz.cuni.mff.d3s.spl.interpretation.KindergartenInterpretation;

/** Helper class for creating very simple formulas without need to write
 * and parse a full-fledged formula.
 *
 */
public class SimpleFormulas {
	public static Formula createAsmallerThanB(String a, String b) {
		return new LeftSmallerThanRight(a, b);
	}
	
	public static Formula createSmallerThanConst(String var, double constant) {
		return new SmallerThanConstant(var, constant);
	}
	
	private static class LeftSmallerThanRight implements Formula {
		private Interpretation apparatus;
		private String leftName;
		private DataSource leftData;
		private String rightName;
		private DataSource rightData;
		
		public LeftSmallerThanRight(String left, String right) {
			apparatus = KindergartenInterpretation.INSTANCE;
			leftName = left;
			rightName = right;
		}

		@Override
		public void setInterpretation(Interpretation apparatus) {
			this.apparatus = apparatus;
		}

		@Override
		public void bind(String variable, DataSource data)
				throws NoSuchElementException {
			if (leftName.equals(variable)) {
				leftData = data;
			} else if (rightName.equals(variable)) {
				rightData = data;
			} else {
				throw new NoSuchElementException("No such variable in the formula.");
			}
		}

		@Override
		public Result evaluate(double significanceLevel) {
			ComparisonResult result = apparatus.compare(leftData.makeSnapshot(), rightData.makeSnapshot());
			return Comparison.relationToResult(Operator.LT, result.get(significanceLevel));
		}
	}
	
	private static class SmallerThanConstant implements Formula {
		private Interpretation interpretation;
		private String sourceName;
		private DataSource data;
		private double constant;
		
		public SmallerThanConstant(String name, double c) {
			interpretation = KindergartenInterpretation.INSTANCE;
			sourceName = name;
			constant = c;
		}

		@Override
		public void setInterpretation(Interpretation interpretation) {
			this.interpretation = interpretation;
		}

		@Override
		public void bind(String variable, DataSource source)
				throws NoSuchElementException {
			if (sourceName.equals(variable)) {
				data = source;
			} else {
				throw new NoSuchElementException("No such variable in the formula.");
			}
		}

		@Override
		public Result evaluate(double significanceLevel) {
			ComparisonResult result = interpretation.compare(data.makeSnapshot(), constant);
			return Comparison.relationToResult(Operator.LT, result.get(significanceLevel));
		}
	}
}
