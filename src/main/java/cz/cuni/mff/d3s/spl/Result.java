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

/** Result of evaluation of SPL formula.
 * 
 */
public enum Result {
	/** The result cannot be computed.
	 * 
	 * There can be several reasons, e.g. insufficient amount of data
	 * (number of collected samples too small). 
	 */
	CANNOT_COMPUTE {
		@Override
		public String toString() {
			return "cannot compute";
		}
	},

	/** The SPL formula evaluates to true. */
	TRUE {
		@Override
		public String toString() {
			return "complies";
		}
	},

	/** The SPL formula evaluates to false. */
	FALSE {
		@Override
		public String toString() {
			return "violates";
		}
	}
}