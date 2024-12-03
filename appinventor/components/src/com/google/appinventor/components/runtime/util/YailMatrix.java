package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import gnu.lists.LList;
import gnu.lists.Pair;
import java.util.ArrayList;
import java.util.List;

public class YailMatrix {
  private final int rows;
  private final int cols;
  private final Object[][] data;

  /**
   * Constructs a new YailMatrix with the specified dimensions and initial data.
   *
   * @param rows number of rows in the matrix.
   * @param cols number of columns in the matrix.
   * @param data initial data for the matrix, represented as a 2D array.
   */
  public YailMatrix(int rows, int cols, Object[][] data) {
    if (data.length != rows || (rows > 0 && data[0].length != cols)) {
      throw new IllegalArgumentException("Matrix dimensions do not match the provided data.");
    }
    this.rows = rows;
    this.cols = cols;
    this.data = new Object[rows][cols];
    for (int i = 0; i < rows; i++) {
      System.arraycopy(data[i], 0, this.data[i], 0, cols);
    }
  }

  /**
   * Constructs a new YailMatrix with the specified dimensions, initialized with zeros.
   *
   * @param rows number of rows in the matrix.
   * @param cols number of columns in the matrix.
   */
  public YailMatrix(int rows, int cols) {
    this.rows = rows;
    this.cols = cols;
    this.data = new Object[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        this.data[i][j] = 0;
      }
    }
  }

  /**
   * Factory method to create a YailMatrix from rows, columns, and data.
   *
   * @param rows number of rows.
   * @param cols number of columns.
   * @param data a 2D list representing the matrix data.
   * @return a new YailMatrix instance.
   */
  public static YailMatrix makeMatrix(int rows, int cols, List<List<Object>> data) {
    if (data == null) {
      throw new YailRuntimeError("Matrix data cannot be null", "MatrixError");
    }
    Object[][] matrixData = new Object[rows][cols];
    for (int i = 0; i < rows; i++) {
      List<Object> row = data.get(i);
      for (int j = 0; j < cols; j++) {
        matrixData[i][j] = row.get(j);
      }
    }
    return new YailMatrix(rows, cols, matrixData);
  }

  /**
   * Gets the value of a specific cell in the matrix.
   *
   * @param row row index (0-indexed).
   * @param col column index (0-indexed).
   * @return value at the specified cell.
   */
  public Object getCell(int row, int col) {
    validateIndices(row, col);
    return data[row][col];
  }

  /**
   * Sets the value of a specific cell in the matrix.
   *
   * @param row row index (0-indexed).
   * @param col column index (0-indexed).
   * @param value value to set.
   */
  public void setCell(int row, int col, Object value) {
    validateIndices(row, col);
    data[row][col] = value;
  }

  /**
   * Gets an entire row from the matrix as a YailList.
   *
   * @param row row index (0-indexed).
   * @return row as a YailList.
   */
  public YailList getRow(int row) {
    validateRowIndex(row);
    return YailList.makeList(data[row]);
  }

  /**
   * Gets an entire column from the matrix as a YailList.
   *
   * @param col column index (0-indexed).
   * @return column as a YailList.
   */
  public YailList getColumn(int col) {
    validateColumnIndex(col);
    Object[] column = new Object[rows];
    for (int i = 0; i < rows; i++) {
      column[i] = data[i][col];
    }
    return YailList.makeList(column);
  }

  /**
   * Validates the indices for accessing a matrix cell.
   *
   * @param row row index.
   * @param col column index.
   * @throws YailRuntimeError if the indices are out of bounds.
   */
  private void validateIndices(int row, int col) {
    validateRowIndex(row);
    validateColumnIndex(col);
  }

  /**
   * Validates the row index.
   *
   * @param row row index.
   * @throws YailRuntimeError if the row index is out of bounds.
   */
  private void validateRowIndex(int row) {
    if (row < 0 || row >= rows) {
      throw new YailRuntimeError("Row index out of bounds: " + row, "Matrix Access Error");
    }
  }

  /**
   * Validates the column index.
   *
   * @param col column index.
   * @throws YailRuntimeError if the column index is out of bounds.
   */
  private void validateColumnIndex(int col) {
    if (col < 0 || col >= cols) {
      throw new YailRuntimeError("Column index out of bounds: " + col, "Matrix Access Error");
    }
  }

  /**
   * Gets the matrix dimensions as a string.
   *
   * @return string of the form "rows x cols".
   */
  public String getDimensions() {
    return rows + " x " + cols;
  }

  /**
   * Returns the string representation of the matrix in JSON format.
   *
   * @return a JSON representation of the matrix.
   */
  @Override
  public String toString() {
    List<List<Object>> rowsList = new ArrayList<>();
    for (Object[] row : data) {
      rowsList.add(List.of(row));
    }
    try {
      return JsonUtil.getJsonRepresentation(rowsList);
    } catch (Exception e) {
      throw new YailRuntimeError("Matrix failed to convert to JSON.", "JSON Error");
    }
  }
}

//   /**
//    * Returns a string representation of the matrix.
//    *
//    * @return A string with matrix values.
//    */
//   @Override
//   public String toString() {
//     StringBuilder builder = new StringBuilder();
//     for (Object[] row : data) {
//       builder.append("[");
//       for (Object cell : row) {
//         builder.append(cell).append(" ");
//       }
//       builder.append("]\n");
//     }
//     return builder.toString();
//   }
// '''
// }
