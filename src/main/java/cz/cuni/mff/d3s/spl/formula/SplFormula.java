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

import cz.cuni.mff.d3s.spl.Formula;

/** Wrapper for parsing an SPL formula.
 *
 */
public class SplFormula {
	public static Formula create(String formula) throws FormulaParsingException {
		return FormulaParser.parse(formula);
	}
	
	public static class SplParseException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public SplParseException(Throwable cause) {
			super(cause);
		}
		
		SplParseException(ParseException cause, String formula) {
			super(String.format("Parsing error in \"%s\" near %s (line %d, column %d).",
					getFirstCharacters(formula, 15),
					cause.currentToken.image,
					cause.currentToken.endLine,
					cause.currentToken.endColumn), cause);
		}
		
		SplParseException(TokenMgrError cause, String formula) {
			super(String.format("Lexical error in \"%s\".",
					getFirstCharacters(formula, 15),
					cause.getMessage()), cause);
			
		}
		
		private static String getFirstCharacters(String input, int recommendedLength) {
			if (input.length() + 1 <= recommendedLength) {
				return input;
			} else {
				return input.substring(0, recommendedLength - 3) + "...";
			}
		}
	}
}
