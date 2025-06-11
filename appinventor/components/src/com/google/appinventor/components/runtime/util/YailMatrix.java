// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024-2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import gnu.lists.LList;
import gnu.lists.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

public class YailMatrix {
  private final int[] dims;
  private final double[] data;

  public YailMatrix(int[] dims, double fillValue) {
    this.dims = dims;
    int totalSize = 1;
    for (int d : dims) {
      if (d < 1) {
        throw new YailRuntimeError("Invalid dimension size: " + d, "Matrix Error");
      }
      totalSize *= d;
    }
    this.data = new double[totalSize];
    Arrays.fill(this.data, fillValue);
  }

  public YailMatrix(int[] dims, double[] dataValues) {
    this.dims = dims;
    int totalSize = 1;
    for (int d : dims) {
      if (d < 1) {
        throw new YailRuntimeError("Invalid dimension size: " + d, "Matrix Error");
      }
      totalSize *= d;
    }
    if (dataValues.length != totalSize) {
      throw new YailRuntimeError("Data length invalid: expected " + totalSize + " but got " + dataValues.length, "Matrix Error");
    }
    this.data = dataValues;
  }

  public YailMatrix(int rows, int cols, double[][] matrixData) {
    this(new int[] { rows, cols }, flatten2D(rows, cols, matrixData));
  }

  private static double[] flatten2D(int rows, int cols, double[][] matrixData) {
    if (matrixData.length != rows) {
      throw new YailRuntimeError("Matrix dimensions mismatch: expected " + rows + " rows but got " + matrixData.length, "Matrix Error");
    }
    double[] flat = new double[rows * cols];
    for (int i = 0; i < rows; i++) {
      if (matrixData[i].length != cols) {
        throw new YailRuntimeError("Matrix dimensions mismatch: row " + (i+1) + " has " + matrixData[i].length + " columns, expected " + cols, "Matrix Error");
      }
      for (int j = 0; j < cols; j++) {
        flat[i * cols + j] = matrixData[i][j];
      }
    }
    return flat;
  }

  private int getFlatIndex(int... indices) {
    if (indices.length != dims.length) {
      throw new YailRuntimeError( "Expected " + dims.length + " indices but got " + indices.length, "Matrix Error");
    }
    int offset = 0;
    int multiplier = 1;
    for (int i = dims.length - 1; i >= 0; i--) {
      int idx = indices[i];
      if (idx < 1 || idx > dims[i]) {
        throw new YailRuntimeError("Index out of bounds for dimension " + (i + 1) + ": " + idx, "Matrix Access Error");
      }
      offset += (idx - 1) * multiplier;
      multiplier *= dims[i];
    }
    return offset;
  }

  public static YailMatrix makeMatrix(Object... dataValues) {
    List<Object> argsList = new ArrayList<>();
    Object elt = dataValues[0];
    while (elt != LList.Empty) {
      if (!(elt instanceof Pair)) {
        throw new YailRuntimeError("Invalid matrix data: " + elt, "Matrix Error");
      }
      Pair pair = (Pair) elt;
      argsList.add(pair.getCar());
      elt = pair.getCdr();
    }
    Object[] args = argsList.toArray();

    int rows = ((Number) args[0]).intValue();
    int cols = ((Number) args[1]).intValue();
    double[][] matrixData = new double[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        matrixData[i][j] = ((Number) args[2 + i * cols + j]).doubleValue();
      }
    }
    return new YailMatrix(rows, cols, matrixData);
  }

  public static YailMatrix makeMultidimMatrix(Object dimsObj, Object initObj) {
    List<Object> rawDims = new ArrayList<>();
    if (dimsObj instanceof List) {
      rawDims.addAll((List<?>) dimsObj);
    } else if (dimsObj instanceof LList) {
      Object elt = dimsObj;
      while (elt != LList.Empty) {
        Pair p = (Pair) elt;
        rawDims.add(p.getCar());
        elt = p.getCdr();
      }
    } else {
      throw new YailRuntimeError("Invalid dims list", "Matrix Error");
    }
    int[] dims = new int[rawDims.size()];
    for (int i = 0; i < rawDims.size(); i++) {
      Object d = rawDims.get(i);
      if (!(d instanceof Number)) {
        throw new YailRuntimeError("Dimension not a number: " + d, "Matrix Error");
      }
      dims[i] = ((Number) d).intValue();
      if (dims[i] < 1) {
        throw new YailRuntimeError("Invalid dimension size: " + dims[i], "Matrix Error");
      }
    }
    double initValue = ((Number) initObj).doubleValue();
    return new YailMatrix(dims, initValue);
  }

  public double getCell(Object... rawIndices) {
    int[] indices = new int[rawIndices.length];
    for (int i = 0; i < rawIndices.length; i++) {
      Object o = rawIndices[i];
      if (!(o instanceof Number)) {
        throw new YailRuntimeError("Index not a number: " + o, "Matrix Access Error");
      }
      indices[i] = ((Number) o).intValue();
    }
    return data[getFlatIndex(indices)];
  }

  public void setCell(Object valueObj, Object... rawIndices) {
    double value = ((Number) valueObj).doubleValue();
    int[] indices = new int[rawIndices.length];
    for (int i = 0; i < rawIndices.length; i++) {
      Object o = rawIndices[i];
      if (!(o instanceof Number)) {
        throw new YailRuntimeError("Index not a number: " + o, "Matrix Access Error");
      }
      indices[i] = ((Number) o).intValue();
    }
    data[getFlatIndex(indices)] = value;
  }

  public LList getRow(int row) {
    if (dims.length != 2) {
      throw new YailRuntimeError("getRow only valid on 2D matrices", "Matrix Error");
    }
    int cols = dims[1];
    double[] rowArr = new double[cols];
    for (int j = 0; j < cols; j++) {
      rowArr[j] = getCell(row, j + 1);
    }
    return arrayToLList(rowArr);
  }

  public LList getColumn(int col) {
    if (dims.length != 2) {
      throw new YailRuntimeError("getColumn only valid on 2D matrices", "Matrix Error");
    }
    int rows = dims[0];
    double[] colArr = new double[rows];
    for (int i = 0; i < rows; i++) {
      colArr[i] = getCell(i + 1, col);
    }
    return arrayToLList(colArr);
  }

  // Convert a Java double[] array to LList
  private LList arrayToLList(double[] array) {
    Object[] newArray = new Object[array.length];
    for (int i = 0; i < array.length; i++) {
        newArray[i] = array[i];
    }
    return LList.makeList(newArray, 0);
  }

  public static YailMatrix transpose(YailMatrix m) {
    if (m.dims.length != 2) {
      throw new YailRuntimeError("transpose only valid on 2D matrices", "Matrix Error");
    }
    int r = m.dims[0], c = m.dims[1];
    int[] newDims = { c, r };
    double[] result = new double[r * c];
    for (int i = 1; i <= r; i++) {
      for (int j = 1; j <= c; j++) {
        result[(j-1)*r + (i-1)] = m.getCell(i, j);
      }
    }
    return new YailMatrix(newDims, result);
  }

  public static YailMatrix inverse(YailMatrix m) {
    if (m.dims.length != 2 || m.dims[0] != m.dims[1]) {
      throw new YailRuntimeError("Matrix must be square for inversion", "Matrix Error");
    }
    int n = m.dims[0];
    // Augmented matrix [ A | I ]
    double[][] aug = new double[n][2 * n];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        aug[i][j] = m.data[i * n + j];
      }
      for (int j = n; j < 2 * n; j++) {
        aug[i][j] = (i == (j - n)) ? 1.0 : 0.0;
      }
    }
    // Gaussian elimination
    for (int i = 0; i < n; i++) {
      double diag = aug[i][i];
      if (diag == 0) {
        throw new YailRuntimeError("Matrix is not invertible", "Matrix Error");
      }
      for (int j = 0; j < 2 * n; j++) {
        aug[i][j] /= diag;
      }
      for (int k = 0; k < n; k++) {
        if (k == i) continue;
        double factor = aug[k][i];
        for (int j = 0; j < 2 * n; j++) {
          aug[k][j] -= factor * aug[i][j];
        }
      }
    }
    double[][] invData = new double[n][n];
    for (int i = 0; i < n; i++) {
      System.arraycopy(aug[i], n, invData[i], 0, n);
    }
    return new YailMatrix(n, n, invData);
  }

  public static YailMatrix rotateLeft(YailMatrix m) {
    if (m.dims.length != 2) {
      throw new YailRuntimeError("rotateLeft only valid for 2D matrices", "Matrix Error");
    }
    int rows = m.dims[0], cols = m.dims[1];
    double[][] result = new double[cols][rows];
    for (int i = 1; i <= cols; i++) {
      for (int j = 1; j <= rows; j++) {
        result[i-1][j-1] = m.getCell(j, cols - i + 1);
      }
    }
    return new YailMatrix(cols, rows, result);
  }

  public static YailMatrix rotateRight(YailMatrix m) {
    if (m.dims.length != 2) {
      throw new YailRuntimeError("rotateRight only valid for 2D matrices", "Matrix Error");
    }
    int rows = m.dims[0], cols = m.dims[1];
    double[][] result = new double[cols][rows];
    for (int i = 1; i <= cols; i++) {
      for (int j = 1; j <= rows; j++) {
        result[i-1][j-1] = m.getCell(rows - j + 1, i);
      }
    }
    return new YailMatrix(cols, rows, result);
  }

  public static YailMatrix add(Object... args) {
    if (args.length == 1) {
      List<Object> flat = new ArrayList<>();
      Object elt = args[0];
      while (elt != LList.Empty) {
        if (!(elt instanceof Pair)) {
          throw new YailRuntimeError("yail-matrix-add: expected list of matrices", "Matrix Error");
        }
        Pair p = (Pair) elt;
        flat.add(p.getCar());
        elt = p.getCdr();
      }
      args = flat.toArray();
    }
    YailMatrix sum = (YailMatrix) args[0];
    for (int i = 1; i < args.length; i++) {
      Object o = args[i];
      if (!(o instanceof YailMatrix)) {
        throw new YailRuntimeError(
            "yail-matrix-add argument " + (i+1) + " is not a matrix: " + o,
            "Matrix Error");
      }
      sum = add(sum, (YailMatrix) o);
    }
    return sum;
  }

  public static YailMatrix add(YailMatrix a, YailMatrix b) {
    if (!Arrays.equals(a.dims, b.dims)) {
      throw new YailRuntimeError("Dimensions must match for addition", "Matrix Error");
    }
    double[] result = new double[a.data.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = a.data[i] + b.data[i];
    }
    return new YailMatrix(a.dims, result);
  }

  public static YailMatrix subtract(YailMatrix a, YailMatrix b) {
    if (!Arrays.equals(a.dims, b.dims)) {
      throw new YailRuntimeError("Dimensions must match for subtraction", "Matrix Error");
    }
    double[] result = new double[a.data.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = a.data[i] - b.data[i];
    }
    return new YailMatrix(a.dims, result);
  }

  public static YailMatrix multiply(Object... args) {
    if (args.length == 1 && args[0] instanceof LList) {
      List<Object> flat = new ArrayList<>();
      Object elt = args[0];
      while (elt != LList.Empty) {
        Pair p = (Pair) elt;
        flat.add(p.getCar());
        elt = p.getCdr();
      }
      args = flat.toArray();
    }
    YailMatrix product = (YailMatrix) args[0];
    for (int i = 1; i < args.length; i++) {
      product = multiply(product, args[i]);
    }
    return product;
  }
  
  public static YailMatrix multiply(YailMatrix a, Object other) {
    if (other instanceof Number) {
      double s = ((Number) other).doubleValue();
      return scalarMultiply(a, s);
    } else if (other instanceof YailMatrix) {
      YailMatrix b = (YailMatrix) other;
      if (a.dims.length != 2 || b.dims.length != 2) {
        throw new YailRuntimeError("Both operands must be 2-D matrices", "Matrix Error");
      }
      int aRows = a.dims[0], aCols = a.dims[1];
      int bRows = b.dims[0], bCols = b.dims[1];
      if (aCols != bRows) {
        throw new YailRuntimeError("Inner dimensions must match for multiplication: " + aCols + " ≠ " + bRows, "Matrix Error");
      }
      int[] outDims = { aRows, bCols };
      double[] out = new double[aRows * bCols];
      for (int i = 1; i <= aRows; i++) {
        for (int j = 1; j <= bCols; j++) {
          double sum = 0;
          for (int k = 1; k <= aCols; k++) {
            sum += a.getCell(i, k) * b.getCell(k, j);
          }
          out[(i - 1) * bCols + (j - 1)] = sum;
        }
      }
      return new YailMatrix(outDims, out);
    } else {
      throw new YailRuntimeError("Cannot multiply matrix by " + other, "Matrix Error");
    }
  }

  public static YailMatrix scalarMultiply(YailMatrix m, double s) {
    double[] result = new double[m.data.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = m.data[i] * s;
    }
    return new YailMatrix(m.dims, result);
  }

  public static YailMatrix power(YailMatrix m, int exp) {
    if (m.dims.length != 2 || m.dims[0] != m.dims[1]) {
      throw new YailRuntimeError("power only valid on square 2D", "Matrix Error");
    }
    int n = m.dims[0];
    YailMatrix result = identityMatrix(n);
    for (int i = 0; i < exp; i++) {
      result = multiply(result, m);
    }
    return result;
  }

  private static YailMatrix identityMatrix(int n) {
    int[] idDims = { n, n };
    double[] diag = new double[n * n];
    for (int i = 0; i < n; i++) {
      diag[i * n + i] = 1.0;
    }
    return new YailMatrix(idDims, diag);
  }

  public static YailList matrixToAlist(YailMatrix m) {
    return toYailList(m, 0, new int[0]);
  }

  private static YailList toYailList(YailMatrix m, int depth, int[] prefix) {
    int size = m.dims[depth];
    List<Object> bucket = new ArrayList<>(size);
    for (int i = 1; i <= size; i++) {
      int[] next = Arrays.copyOf(prefix, prefix.length + 1);
      next[prefix.length] = i;
      if (depth == m.dims.length - 1) {
        bucket.add(m.data[m.getFlatIndex(next)]);
      } else {
        bucket.add(toYailList(m, depth + 1, next));
      }
    }
    return YailList.makeList(bucket);
  }

  public static boolean matrixEqual(YailMatrix a, YailMatrix b) {
    return Arrays.equals(a.dims, b.dims) && Arrays.equals(a.data, b.data);
  }

  /**
   * Return a strictly syntactically correct JSON text
   * representation of this YailMatrix.
   */
  public String toJSONString() {
    try {
      YailList asList = matrixToAlist(this);
      return JsonUtil.getJsonRepresentation(asList);
    } catch (JSONException e) {
      throw new YailRuntimeError("Matrix failed to convert to JSON.", "JSON Creation Error.");
    }
  }

  public static YailMatrix fromJsonArray(JSONArray arr) throws JSONException {
    List<Integer> dimsList = new ArrayList<>();
    if (!detectDims(arr, dimsList)) {
      throw new JSONException("Not a uniform numeric array");
    }
    int[] dims = dimsList.stream().mapToInt(i -> i).toArray();
    int totalSize = 1;
    for (int d : dims) totalSize *= d;
    double[] flat = new double[totalSize];
    flatten(arr, dimsList, 0, flat, new int[]{0});
    return new YailMatrix(dims, flat);
  }

  private static boolean detectDims(JSONArray arr, List<Integer> dims) throws JSONException {
    dims.add(arr.length());
    if (arr.length() == 0) {
      return true;
    }
    Object first = arr.get(0);
    if (first instanceof JSONArray) {
      int len = ((JSONArray) first).length();
      for (int i = 1; i < arr.length(); i++) {
        Object o = arr.get(i);
        if (!(o instanceof JSONArray) || ((JSONArray)o).length() != len) {
          return false;
        }
      }
      return detectDims((JSONArray) first, dims);
    } else {
      for (int i = 0; i < arr.length(); i++) {
        if (!(arr.get(i) instanceof Number)) {
          return false;
        }
      }
      return true;
    }
  }

  private static void flatten(JSONArray arr, List<Integer> dims, int depth, double[] flat, int[] writeIndex) throws JSONException {
    if (depth == dims.size() - 1) {
      for (int i = 0; i < arr.length(); i++) {
        flat[writeIndex[0]++] = ((Number)arr.get(i)).doubleValue();
      }
    } else {
      for (int i = 0; i < arr.length(); i++) {
        flatten(arr.getJSONArray(i), dims, depth + 1, flat, writeIndex);
      }
    }
  }

  /**
   * Returns the string representation of the matrix.
   *
   * @return a string representing the matrix.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[\n");
    for (int i = 1; i <= dims[0]; i++) {
      sb.append("  ").append(buildString(1, new int[]{i}));
      if (i < dims[0]) sb.append(",\n");
    }
    sb.append("\n]");
    return sb.toString();
  }

  private String buildString(int depth, int[] prefix) {
    if (depth == dims.length) {
      return Double.toString(data[getFlatIndex(prefix)]);
    }
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 1; i <= dims[depth]; i++) {
      if (i > 1) sb.append(", ");
      int[] next = Arrays.copyOf(prefix, prefix.length + 1);
      next[prefix.length] = i;
      sb.append(buildString(depth + 1, next));
    }
    sb.append("]");
    return sb.toString();
  }
}
