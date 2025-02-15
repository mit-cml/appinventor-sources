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
    if (data.length != rows) {
      throw new YailRuntimeError("Matrix dimensions mismatch: Expected " + rows + " rows but got " + data.length, "Matrix Error");
    }
    for (int i = 0; i < data.length; i++) {
      if (data[i].length != cols) {
        throw new YailRuntimeError("Matrix dimensions mismatch: Row " + (i + 1) + " has " + data[i].length + " columns, expected " + cols, "Matrix Error");
      }
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

  public static YailMatrix transpose(YailMatrix matrix) {
    int rows = matrix.rows;
    int cols = matrix.cols;
    double[][] transposedData = new double[cols][rows];
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            transposedData[j][i] = matrix.data[i][j];
        }
    }
    return new YailMatrix(cols, rows, transposedData);
  }

  public static YailMatrix inverse(YailMatrix matrix) {
    if (matrix.rows != matrix.cols) {
        throw new YailRuntimeError("Matrix must be square for inversion", "Matrix Error");
    }

    int n = matrix.rows;
    double[][] augmented = new double[n][2 * n];

    // Augmented matrix [A | I]
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            augmented[i][j] = matrix.data[i][j];
        }
        for (int j = n; j < 2 * n; j++) {
            augmented[i][j] = (i == j - n) ? 1 : 0;
        }
    }

    // Gaussian elimination
    for (int i = 0; i < n; i++) {
        double diag = augmented[i][i];
        if (diag == 0) {
            throw new YailRuntimeError("Matrix is not invertible", "Matrix Error");
        }
        for (int j = 0; j < 2 * n; j++) {
            augmented[i][j] /= diag;
        }

        for (int k = 0; k < n; k++) {
            if (k == i) continue;
            double factor = augmented[k][i];
            for (int j = 0; j < 2 * n; j++) {
                augmented[k][j] -= factor * augmented[i][j];
            }
        }
    }

    double[][] inverseData = new double[n][n];
    for (int i = 0; i < n; i++) {
        System.arraycopy(augmented[i], n, inverseData[i], 0, n);
    }

    return new YailMatrix(n, n, inverseData);
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

  public static YailMatrix add(YailMatrix matrix1, YailMatrix matrix2) {
    if (matrix1.rows != matrix2.rows || matrix1.cols != matrix2.cols) {
      throw new YailRuntimeError("Matrix dimensions must match for addition", "Matrix Error");
    }

    int rows = matrix1.rows;
    int cols = matrix1.cols;
    double[][] result = new double[rows][cols];

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        result[i][j] = matrix1.data[i][j] + matrix2.data[i][j];
      }
    }
    return new YailMatrix(rows, cols, result);
  }

  public static YailMatrix subtract(YailMatrix matrix1, YailMatrix matrix2) {
    if (matrix1.rows != matrix2.rows || matrix1.cols != matrix2.cols) {
      throw new YailRuntimeError("Matrix dimensions must match for subtraction", "Matrix Error");
    }

    int rows = matrix1.rows;
    int cols = matrix1.cols;
    double[][] result = new double[rows][cols];

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        result[i][j] = matrix1.data[i][j] - matrix2.data[i][j];
      }
    }
    return new YailMatrix(rows, cols, result);
  }

  public static YailMatrix multiply(YailMatrix matrix1, Object matrix2OrScalar) {
    if (matrix2OrScalar instanceof Number) {
        double scalar = ((Number) matrix2OrScalar).doubleValue();
        return scalarMultiply(matrix1, scalar);
    } else if (matrix2OrScalar instanceof YailMatrix) {
        YailMatrix matrix2 = (YailMatrix) matrix2OrScalar;
        if (matrix1.cols != matrix2.rows) {
            throw new YailRuntimeError("Matrix multiplication requires matching inner dimensions", "Matrix Error");
        }

        int rows = matrix1.rows;
        int cols = matrix2.cols;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                for (int k = 0; k < matrix1.cols; k++) {
                    result[i][j] += matrix1.data[i][k] * matrix2.data[k][j];
                }
            }
        }
        return new YailMatrix(rows, cols, result);
    } else {
        throw new YailRuntimeError("Invalid argument for matrix multiplication", "Matrix Error");
    }
  }

  public static YailMatrix scalarMultiply(YailMatrix matrix, double scalar) {
    int rows = matrix.rows;
    int cols = matrix.cols;
    double[][] result = new double[rows][cols];

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            result[i][j] = matrix.data[i][j] * scalar;
        }
    }
    return new YailMatrix(rows, cols, result);
  }

  public static YailMatrix power(YailMatrix matrix, int exponent) {
    if (matrix.rows != matrix.cols) {
      throw new YailRuntimeError("Matrix exponentiation requires a square matrix", "Matrix Error");
    }

    if (exponent < 0) {
      throw new YailRuntimeError("Matrix exponent must be non-negative", "Matrix Error");
    }

    YailMatrix result = identityMatrix(matrix.rows);

    for (int i = 0; i < exponent; i++) {
      result = multiply(result, matrix);
    }

    return result;
  }

  private static YailMatrix identityMatrix(int size) {
    double[][] identity = new double[size][size];
    for (int i = 0; i < size; i++) {
      identity[i][i] = 1.0;
    }
    return new YailMatrix(size, size, identity);
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
