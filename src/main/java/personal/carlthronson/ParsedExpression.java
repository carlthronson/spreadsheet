package personal.carlthronson;

import java.util.List;

public class ParsedExpression {

  private List<Object> terms;

  public List<Object> getTerms() {
    return terms;
  }

  public void setTerms(List<Object> terms) {
    this.terms = terms;
  }

  @Override
  public String toString() {
    return this.terms.toString();
  }
}
