package personal.carlthronson;

public class MissingCellException extends Exception {

  private static final long serialVersionUID = 1L;

  // Exception message format
  private static final String MESSAGE_FORMAT = "Cell %s references missing cell %s";

  public MissingCellException(String cell1, String cell2) {
    super(String.format(MESSAGE_FORMAT, cell1, cell2));
  }

}
