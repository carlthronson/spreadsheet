package personal.carlthronson;

/**
 * Cell
 * 
 * This is just a place to hold text
 * That is read in from the file
 * 
 * @author carl
 *
 */
public class Cell {

  private String text;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return this.text;
  }
}
