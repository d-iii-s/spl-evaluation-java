/*
 * Copyright 2014 Charles University in Prague
 * Copyright 2014 Vojtech Horky
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
 * <p>
 * It allows to compare two data sources (that is, their snapshots) or a
 * data source with a constant.
 * 
 * <p>
 * The result is returned as a special object that provides more details
 * about the computed statistic than bare true/false result of the
 * comparison.
 */
public interface Interpretation {
	/** Compare two data snapshots against each other.
	 * 
	 * @param left Left-hand operand.
	 * @param right Right-hand operand.
	 * @return Comparison of the operands according to the current
	 * interpretation.
	 */
	public ComparisonResult compare(DataSnapshot left, DataSnapshot right);
	
	/** Compara a data snapshot with a constant.
	 * 
	 * @param data Left-hand operand in a form of a data snapshot.
	 * @param value Right-hand operand in a form of a constant.
	 * @return Comparison of the operands according to the current
	 * interpretation.
	 */
	public ComparisonResult compare(DataSnapshot data, double value);
}
