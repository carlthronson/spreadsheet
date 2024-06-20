package personal.carlthronson;

public class CycleException extends Exception {

  private static final long serialVersionUID = 1L;

  // Exception message format
  private static final String MESSAGE_FORMAT = "Cell reference cycle detected %s -> %s ";

  public CycleException(String cell1, String cell2) {
    super(String.format(MESSAGE_FORMAT, cell1, cell2));
  }

}
