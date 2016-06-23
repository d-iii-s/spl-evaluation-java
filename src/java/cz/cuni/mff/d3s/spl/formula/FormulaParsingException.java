package cz.cuni.mff.d3s.spl.formula;

/** Wrapping exception for a parsing error within a formula.
 *
 * Since formulas would be probably prepared by developers this is
 * an unchecked exception.
 */
public class FormulaParsingException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public FormulaParsingException(ParseException cause, String formula) {
		super(String.format("Parsing error in \"%s\" near %s (line %d, column %d).",
				getFirstCharacters(formula, 15),
				cause.currentToken.image,
				cause.currentToken.endLine,
				cause.currentToken.endColumn), cause);
	}
	
	public FormulaParsingException(TokenMgrError cause, String formula) {
		super(String.format("Lexical error in \"%s\" caused by \"%s\".",
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
