package personal.carlthronson;

import java.io.FileReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Spreadsheet {

  // Logging message formats
  private static final String DEBUG_MSG_FMT_ROWS = "Rows: %s";
  private static final String DEBUG_MSG_FMT_GRAPH = "Graph: %s";
  private static final String DEBUG_MSG_FMT_VALUES = "Values: %s";

  // Default file name
  private static final String DEFAULT_INPUT_FILE_NAME = "input.csv";

  // Label format
  private static final String CELL_LABEL_FORMAT = "%s%d";

  // Default column delimiter
  private static final String SPREADSHEET_COLUMN_DELIMITER = ",";

  // Column labels
  private static final char FIRST_COLUMN_LABEL = 'A';
  private static final char LAST_ALLOWED_COLUMN_LABEL = 'Z';

  // Logging
  private static Logger logger = Logger.getLogger(Spreadsheet.class.getName());

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    try {
      // Get the file name to read data
      String fileName = DEFAULT_INPUT_FILE_NAME;
      if (args.length > 0) {
        fileName = args[0];
      }

      // Read in rows of raw data and log result
      List<Row> rows = readRows(new FileReader(fileName));
      String message = String.format(DEBUG_MSG_FMT_ROWS, rows);
      logger.log(Level.INFO, message);

      // Parse raw data into expression objects and
      // Create a graph with cell labels as the keys
      // And log result
      Map<String, ParsedExpression> graph = parseCellText(rows);
      message = String.format(DEBUG_MSG_FMT_GRAPH, graph);
      logger.log(Level.INFO, message);

      // Check for cycles and/or missing cells
      for (String label : graph.keySet()) {
        Set<String> seen = new HashSet<>();
        validateCellReferences(label, seen, graph);
      }

      // Compute values
      Map<String, BigDecimal> values = computeValues(graph);
      message = String.format(DEBUG_MSG_FMT_VALUES, values);
      logger.log(Level.INFO, message);

      // Write values
      writeData(rows, values, System.out);
    } catch (Exception ex) {
      logger.log(Level.SEVERE, ex.getMessage(), ex);
      return;
    }
  }

  /**
   * Read in rows of raw data
   * 
   * Every line is one row of data Split each line into tokens Every token is one
   * cell Each row can only have max cell of: LAST_ALLOWED_COLUMN_LABEL
   * 
   * @param source
   * @return
   * @throws Exception
   */
  private static List<Row> readRows(Readable source) throws Exception {
    List<Row> rows = new ArrayList<>();
    try (Scanner scanner = new Scanner(source)) {
      while (scanner.hasNextLine()) {
        Row row = new Row();
        String line = scanner.nextLine();
        char columnLabel = FIRST_COLUMN_LABEL;
        for (String token : line.split(SPREADSHEET_COLUMN_DELIMITER)) {
          if (columnLabel > LAST_ALLOWED_COLUMN_LABEL) {
            throw new Exception("Too many columns");
          }
          Cell cell = new Cell();
          cell.setText(token);
          row.add(cell); // Add cell to row
          columnLabel++;
        }
        rows.add(row); // Add row to rows
      }
    }
    return rows;
  }

  /**
   * Parse cell text
   * 
   * Parse raw data into expression objects and
   * Create a graph with cell labels as the keys
   * 
   * @param rows
   * @return
   * @throws Exception
   */
  private static Map<String, ParsedExpression> parseCellText(List<Row> rows) throws Exception {
    Map<String, ParsedExpression> graph = new HashMap<>();
    int rowNumber = 1;
    for (Row row : rows) {
      char columnLabel = FIRST_COLUMN_LABEL;
      for (Cell cell : row.getCells()) {
        String label = String.format(CELL_LABEL_FORMAT, columnLabel, rowNumber);
        try {
          ParsedExpression parsedExpression = parseExpression(cell);
          graph.put(label, parsedExpression);
          columnLabel++;
        } catch (Exception ex) {
          logger.severe("Invalid expression in cell: " + label);
          throw ex;
        }
      }
      rowNumber++;
    }
    return graph;
  }

  /**
   * Parse expression
   * 
   * A cell expression is a mixed floating point expression that only allows
   * operators of plus and minus. This function will tokenize the text into a list
   * of terms. Each term is either an operator, a number or a cell reference
   * 
   * @param cell
   * @return
   * @throws Exception
   */
  private static ParsedExpression parseExpression(Cell cell) throws Exception {
    ParsedExpression expression = new ParsedExpression();
    List<Object> terms = new ArrayList<>();

    // Remove white space in cell text
    String text = cell.getText().replaceAll(" ", "");

    // Tokenize text into operators and operands
    // Use the known valid operands as the delimeters
    // Also return the delimiters as tokens
    StringTokenizer st = new StringTokenizer(text, TermType.getDelimiters(), true);
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      TermType termType = TermType.fromToken(token);
      switch (termType) {
      case NUMBER:
        terms.add(new BigDecimal(token));
        break;
      case REFERENCE:
        terms.add(token);
        break;
      case DISALLOWED_OPERATION:
        throw new DisallowedOperationException(cell, token);
      case INVALID_TERM:
        throw new InvalidTermException(cell, token);
      default:
        // The only possibility remaining is a valid operator
        terms.add(termType);
      }
    }
    expression.setTerms(terms);
    return expression;
  }

  /**
   * 
   * @param label
   * @param seen
   * @param graph
   * @throws Exception
   */
  private static void validateCellReferences(String label, Set<String> seen, Map<String, ParsedExpression> graph)
      throws Exception {
    ParsedExpression parsedExpression = graph.get(label);
    List<Object> terms = parsedExpression.getTerms();
    for (Object term : terms) {
      TermType termType = TermType.fromTerm(term);
      if (termType == TermType.REFERENCE) {
        String cellReference = term.toString();
        if (!graph.containsKey(cellReference)) {
          // If node is not in graph, the referenced cell doesn't exist
          throw new MissingCellException(label, cellReference);
        }
        if (seen.contains(cellReference)) {
          // If we have already visited this node, this is a cycle
          throw new CycleException(label, cellReference);
        } else {
          // Recursively visit the referenced cell
          seen.add(cellReference);
          validateCellReferences(cellReference, seen, graph);
          seen.remove(cellReference);
        }
      }
    }
  }

  /**
   * Compute value of each cell
   * 
   * @param graph
   * @return
   * @throws Exception
   */
  private static Map<String, BigDecimal> computeValues(Map<String, ParsedExpression> graph) throws Exception {
    Map<String, BigDecimal> numbers = new HashMap<>();
    for (String label : graph.keySet()) {
      if (!numbers.containsKey(label)) {
        numbers.put(label, computeValue(label, numbers, graph));
      }
    }
    return numbers;
  }

  /**
   * Compute value of one cell
   * 
   * This is a recursive function that will Recursively compute values for other
   * cells that are refenced Before computing the value for this cell
   * 
   * @param cell
   * @param values
   * @param graph
   * @return
   * @throws Exception
   */
  private static BigDecimal computeValue(String cell, Map<String, BigDecimal> values,
      Map<String, ParsedExpression> graph) throws Exception {
    // Start by getting the parsed expression for this cell
    ParsedExpression parsedExpression = graph.get(cell);
    // Then get the list of terms
    List<Object> terms = parsedExpression.getTerms();

    // Check each term if it is a reference and if so compute it first
    for (Object term : terms) {
      TermType termType = TermType.fromTerm(term);
      if (termType == TermType.REFERENCE) {
        String ref = term.toString();
        if (!values.containsKey(ref)) {
          values.put(ref, computeValue(ref, values, graph));
        }
      }
    }

    // Compute value of this cell
    // Start by setting initial value to zero
    BigDecimal computedValue = BigDecimal.ZERO;

    // Default operation because the first term should be added
    TermType operation = TermType.ADD;

    // Evaluate each term of the expression
    for (Object term : terms) {
      // Check if term is an operator
      BigDecimal augend = null;
      TermType termType = TermType.fromTerm(term);
      switch (termType) {
        case ADD:
        case MINUS:
          operation = (TermType)term;
          continue;
        case NUMBER:
          augend = (BigDecimal) term;
          break;
        case REFERENCE:
          String label = term.toString();
          augend = values.get(label);
          break;
      default:
        break;
      }
      switch (operation) {
      case ADD:
        computedValue = computedValue.add(augend);
        break;
      case MINUS:
        computedValue = computedValue.subtract(augend);
        break;
      default:
        throw new Exception("Unknown operator");
      }
    }
    return computedValue;
  }

  private static void writeData(List<Row> rows, Map<String, BigDecimal> values, PrintStream out) {
    int rowNumber = 1;
    for (Row row : rows) {
      char columnLabel = FIRST_COLUMN_LABEL;
      for (int column = 0; column < row.getCells().size(); column++) {
        columnLabel += column;
        String label = String.format(CELL_LABEL_FORMAT, columnLabel, rowNumber);
        BigDecimal value = values.get(label);
        if (column > 0) {
          out.print(",");
        }
        out.print(value.setScale(2, RoundingMode.HALF_DOWN));
      }
      out.println();
      rowNumber++;
    }
  }

}
