/* Generated By:JavaCC: Do not edit this line. FormulaParserConstants.java */
package cz.cuni.mff.d3s.spl.formula;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
interface FormulaParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int IDENTIFIER = 5;
  /** RegularExpression Id. */
  int REAL = 6;
  /** RegularExpression Id. */
  int REAL2 = 7;
  /** RegularExpression Id. */
  int INT = 8;
  /** RegularExpression Id. */
  int CMP_GT = 9;
  /** RegularExpression Id. */
  int CMP_LT = 10;
  /** RegularExpression Id. */
  int LOGIC_AND = 11;
  /** RegularExpression Id. */
  int LOGIC_OR = 12;
  /** RegularExpression Id. */
  int LOGIC_IMPLY = 13;
  /** RegularExpression Id. */
  int LEFT_PAREN = 14;
  /** RegularExpression Id. */
  int RIGHT_PAREN = 15;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "<IDENTIFIER>",
    "<REAL>",
    "<REAL2>",
    "<INT>",
    "\">\"",
    "\"<\"",
    "\"&&\"",
    "\"||\"",
    "\"=>\"",
    "\"(\"",
    "\")\"",
  };

}
