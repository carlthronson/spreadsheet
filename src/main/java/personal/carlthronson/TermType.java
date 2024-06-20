package personal.carlthronson;

import java.math.BigDecimal;

/**
 * TermType
 * 
 * The values of this enum represent the different types of terms
 * That can appear in an expression
 * 
 * There are helper methods to determine the term type from
 * a token that has not been evaluated and is therefore just a String
 * or a term that has already been evaluated and is therefore either
 * a TermType or a BigDecimal or a String object
 * 
 * @author carl
 *
 */
public enum TermType {

  ADD, MINUS, DISALLOWED_OPERATION, REFERENCE, NUMBER, INVALID_TERM;

  public static TermType fromTerm(Object term) {
    if (term instanceof TermType) {
      return (TermType)term;
    } else if (term instanceof BigDecimal) {
      return NUMBER;
    }
    return fromToken(term.toString());
  }

  public static TermType fromToken(String token) {
    switch (token.charAt(0)) {
    case PLUS_OPERATOR:
      return ADD;
    case MINUS_OPERATOR:
      return MINUS;
    case DIVIDE_OPERATOR:
    case MULTIPLY_OPERATOR:
    case OPEN_PARENTHESIS:
    case CLOSE_PARENTHESIS:
      return DISALLOWED_OPERATION;
    default:
      if (Character.isAlphabetic(token.charAt(0))) {
        return REFERENCE;
      } else {
        try {
          new BigDecimal(token);
          return NUMBER;
        } catch (NumberFormatException ex) {
          return INVALID_TERM;
        }
      }
    }
  }

  private static final char PLUS_OPERATOR = '+';
  private static final char MINUS_OPERATOR = '-';
  private static final char DIVIDE_OPERATOR = '/';
  private static final char MULTIPLY_OPERATOR = '*';
  private static final char OPEN_PARENTHESIS = '(';
  private static final char CLOSE_PARENTHESIS = ')';

  // Expression operator characters
  private static final String VALID_OPERATORS = "+-/*()";

  static String getDelimiters() {
    return VALID_OPERATORS;
  }

}
