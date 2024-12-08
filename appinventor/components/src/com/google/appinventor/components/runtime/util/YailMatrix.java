package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import java.util.ArrayList;
import java.util.List;

public class YailMatrix {
  private final int rows;
  private final int cols;
  private final YailList data;

  /**
   * Constructs a new YailMatrix with the specified dimensions and initial data.
   *
   * @param rows number of rows in the matrix.
   * @param cols number of columns in the matrix.
   * @param data a 2D YailList representing the matrix data.
   */
  public YailMatrix(int rows, int cols, YailList data) {
    validateDataDimensions(rows, cols, data);
    this.rows = rows;
    this.cols = cols;
    this.data = data;
  }

  /**
   * Factory method to create a YailMatrix from rows, columns, and data.
   *
   * @param rows number of rows.
   * @param cols number of columns.
   * @param data a 2D YailList representing the matrix data.
   * @return a new YailMatrix instance.
   */
  public static YailMatrix makeMatrix(int rows, int cols, YailList data) {
    return new YailMatrix(rows, cols, data);
  }

  /**
   * Gets the value of a specific cell in the matrix.
   *
   * @param row row index (1-indexed).
   * @param col column index (1-indexed).
   * @return value at the specified cell.
   */
  public Object getCell(int row, int col) {
    validateIndices(row, col);
    YailList rowList = (YailList) data.getObject(row - 1);
    return rowList.getObject(col - 1);
  }

  /**
   * Sets the value of a specific cell in the matrix.
   *
   * @param row row index (1-indexed).
   * @param col column index (1-indexed).
   * @param value value to set.
   */
  public void setCell(int row, int col, int value) {
    validateIndices(row, col);
    YailList rowList = (YailList) data.getObject(row - 1);
    List<Object> updatedRow = new ArrayList<>(rowList);
    updatedRow.set(col - 1, value);
    data.set(row - 1, YailList.makeList(updatedRow));
  }

  /**
   * Gets an entire row from the matrix as a YailList.
   *
   * @param row row index (1-indexed).
   * @return row as a YailList.
   */
  public YailList getRow(int row) {
    validateRowIndex(row);
    return (YailList) data.getObject(row - 1);
  }

  /**
   * Gets an entire column from the matrix as a YailList.
   *
   * @param col column index (1-indexed).
   * @return column as a YailList.
   */
  public YailList getColumn(int col) {
    validateColumnIndex(col);
    List<Object> column = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      YailList rowList = (YailList) data.getObject(i);
      column.add(rowList.getObject(col - 1));
    }
    return YailList.makeList(column);
  }

  /**
   * Gets the dimensions of the matrix as a string.
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
    return data.toJSONString();
  }

  /**
   * Validates the data dimensions match the specified rows and columns.
   *
   * @param rows expected number of rows.
   * @param cols expected number of columns.
   * @param data the data to validate.
   */
  private void validateDataDimensions(int rows, int cols, YailList data) {
    if (data.size() != rows) {
      throw new YailRuntimeError("Matrix row size mismatch", "Matrix Error");
    }
    for (int i = 0; i < rows; i++) {
      YailList row = (YailList) data.getObject(i);
      if (row.size() != cols) {
        throw new YailRuntimeError("Matrix column size mismatch", "Matrix Error");
      }
    }
  }

  /**
   * Validates the indices for accessing a matrix cell.
   *
   * @param row row index (1-indexed).
   * @param col column index (1-indexed).
   */
  private void validateIndices(int row, int col) {
    validateRowIndex(row);
    validateColumnIndex(col);
  }

  /**
   * Validates the row index.
   *
   * @param row row index (1-indexed).
   */
  private void validateRowIndex(int row) {
    if (row < 1 || row > rows) {
      throw new YailRuntimeError("Row index out of bounds: " + row, "Matrix Access Error");
    }
  }

  /**
   * Validates the column index.
   *
   * @param col column index (1-indexed).
   */
  private void validateColumnIndex(int col) {
    if (col < 1 || col > cols) {
      throw new YailRuntimeError("Column index out of bounds: " + col, "Matrix Access Error");
    }
  }
}
