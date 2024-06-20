package personal.carlthronson;

public class InvalidTermException extends Exception {

  private static final String MESSAGE_FORMAT = "Cell expression: '%s' term: %s";
  private static final long serialVersionUID = 1L;

  public InvalidTermException(Cell cell, String term) {
    super(String.format(MESSAGE_FORMAT, cell, term));
  }

}
