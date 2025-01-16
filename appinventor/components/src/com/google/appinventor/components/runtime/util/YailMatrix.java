package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import gnu.lists.LList;
import gnu.lists.Pair;
import java.util.ArrayList;
import java.util.List;

public class YailMatrix {
  private final int rows;
  private final int cols;
  private final double[][] data;

  public YailMatrix(int rows, int cols, double[][] data) {
      if (data.length != rows || data[0].length != cols) {
          throw new YailRuntimeError("Matrix dimensions mismatch", "Matrix Error");
      }
      this.rows = rows;
      this.cols = cols;
      this.data = data;
  }

  public static YailMatrix makeMatrix(Object... dataValues) {
    List<Object> argsList = new ArrayList<>();
    Object element = dataValues[0];
    while (element != LList.Empty) {
        if (!(element instanceof Pair)) {
            throw new YailRuntimeError("Invalid structure: Expected Pair but got: " +
                                       element.getClass().getName(), "Matrix Error");
        }
        Pair pair = (Pair) element;
        argsList.add(pair.getCar());
        element = pair.getCdr();
    }

    Object[] args = argsList.toArray();

    int rows = ((Number) args[0]).intValue();
    int cols = ((Number) args[1]).intValue();

    if (args.length != 2 + rows * cols) {
        throw new YailRuntimeError("Matrix data size invalid: expected " + (rows * cols) +
                                   " values but got " + (args.length - 2), "Matrix Error");
    }

    double[][] matrixData = new double[rows][cols];
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            int index = 2 + i * cols + j;
            Object value = args[index];
            matrixData[i][j] = ((Number) value).doubleValue();
        }
    }

    return new YailMatrix(rows, cols, matrixData);
  }

  public double getCell(int row, int col) {
      validateIndices(row, col);
      return data[row - 1][col - 1];
  }

  public void setCell(int row, int col, double value) {
      validateIndices(row, col);
      data[row - 1][col - 1] = value;
  }

  public LList getRow(int row) {
      validateRowIndex(row);
      return arrayToLList(data[row - 1]);
  }

  public LList getColumn(int col) {
      validateColumnIndex(col);
      double[] colData = new double[rows];
      for (int i = 0; i < rows; i++) {
          colData[i] = data[i][col - 1];
      }
      return arrayToLList(colData);
  }

  // Convert a Java double[] array to LList
  private LList arrayToLList(double[] array) {
      Object[] newArray = new Object[array.length];
      for (int i = 0; i < array.length; i++) {
          newArray[i] = array[i];
      }
      return LList.makeList(newArray, 0);
  }

  public String getDimensions() {
    return rows + " x " + cols;
  }

  private void validateIndices(int row, int col) {
    validateRowIndex(row);
    validateColumnIndex(col);
  }

  private void validateRowIndex(int row) {
    if (row < 1 || row > rows) {
      throw new YailRuntimeError("Row index out of bounds: " + row, "Matrix Access Error");
    }
  }

  private void validateColumnIndex(int col) {
    if (col < 1 || col > cols) {
      throw new YailRuntimeError("Column index out of bounds: " + col, "Matrix Access Error");
    }
  }

  /**
   * Returns the string representation of the matrix in row-major order.
   *
   * @return a string representing the matrix.
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < rows; i++) {
      builder.append("[");
      for (int j = 0; j < cols; j++) {
        builder.append(getCell(i + 1, j + 1));
        if (j < cols - 1) {
          builder.append(", ");
        }
      }
      builder.append("]");
      if (i < rows - 1) {
        builder.append(", ");
      }
    }
    builder.append("]");
    return builder.toString();
  }
}
