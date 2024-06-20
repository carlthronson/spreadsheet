package personal.carlthronson;

public class DisallowedOperationException extends InvalidTermException {

  private static final long serialVersionUID = 1L;

  public DisallowedOperationException(Cell cell, String term) {
    super(cell, term);
  }

}
