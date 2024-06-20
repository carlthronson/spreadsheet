package personal.carlthronson;

import java.util.ArrayList;
import java.util.List;

/**
 * Row
 * 
 * This class is just to hold a list of cells
 * 
 * @author carl
 *
 */
public class Row {

  List<Cell> cells = new ArrayList<>();

  public void add(Cell cell) {
    cells.add(cell);
  }

  public List<Cell> getCells() {
    return cells;
  }

  @Override
  public String toString() {
    return this.cells.toString();
  }
}
