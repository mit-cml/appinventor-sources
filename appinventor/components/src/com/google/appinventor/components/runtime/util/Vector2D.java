// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

/**
 * Abstraction for a 2-dimensional vector with tail at (0, 0) and the head specified by x and y.
 * Representation similar to the normal unit vector notation of vectors.
 */
public class Vector2D {
  // vector with tail at (0, 0) and head at (x, y)
  private double x;
  private double y;

  /**
   * Constructs a 2D vector with head at (x, y).
   *
   * @param x the x component of the vector
   * @param y the y component of the Vector
   */
  public Vector2D(double x, double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Gets the x component of the vector.
   *
   * @return the x component of the vector
   */
  public double getX() {
    return x;
  }

  /**
   * Gets the y component of the vector.
   *
   * @return the y component of the vector
   */
  public double getY() {
    return y;
  }

  /**
   * Gets the magnitude of the vector.
   *
   * @return the magnitude of the vector
   */
  public double magnitude() {
    return Math.sqrt(magnitudeSquared());
  }

  /**
   * Gets the square of the magnitude of the vector.
   *
   * @return the square of the magnitude of the vector
   */
  public double magnitudeSquared() {
    return x * x + y * y;
  }

  /**
   * Determines if this vector is greater in magnitude than the given vector.
   *
   * @param that the vector to compare magnitude with
   * @return  true if this vector is greater in magnitude, false otherwise
   */
  public boolean isGreater(Vector2D that) {
    return this.magnitudeSquared() > that.magnitudeSquared();
  }

  /**
   * Gets a vector normal to this vector.
   *
   * @return  a vector normal to this vector
   */
  public Vector2D getNormalVector() {
    // dot product = x * y + y * (-x) = 0
    return new Vector2D(y, -x);
  }

  /**
   * Gets the vector obtained by subtraction of the two vectors.
   *
   * @param vector1 the vector that is subtracted from
   * @param vector2 the vector that is subtracted
   * @return  vector obtained by vector1 - vector2
   */
  public static Vector2D difference(Vector2D vector1, Vector2D vector2) {
    return new Vector2D(vector1.getX() - vector2.getX(), vector1.getY() - vector2.getY());
  }

  /**
   * Gets the vector obtained by addition of the two vectors.
   *
   * @param vector1 the first vector
   * @param vector2 the second vector
   * @return  vector obtained by vector1 + vector2
   */
  public static Vector2D addition(Vector2D vector1, Vector2D vector2) {
    return new Vector2D(vector1.getX() + vector2.getX(), vector1.getY() + vector2.getY());
  }

  /**
   * Gets the dot product of the two vectors.
   *
   * @param vector1 the first vector
   * @param vector2 the second vector
   * @return  dot product of vector1 and vector2
   */
  public static double dotProduct(Vector2D vector1, Vector2D vector2) {
    return vector1.getX() * vector2.getX() + vector1.getY() * vector2.getY();
  }

  /**
   * Gets the unit vector in the direction of this vector.
   *
   * @return unit vector in the direction of this vector
   */
  public Vector2D unitVector() {
    return new Vector2D(getX() / magnitude(), getY() / magnitude());
  }

  /**
   * Gets the vector closest to this vector from the given vectors.
   *
   * @param vectors list of vectors from which to choose the closest vector
   * @return  the closest vector to this vector
   */
  public Vector2D getClosestVector(java.util.List<Vector2D> vectors) {
    Vector2D closestVector = vectors.get(0);
    double minDistance = Double.MAX_VALUE;

    for (Vector2D v : vectors) {
      double distance = Vector2D.difference(this, v).magnitudeSquared();
      if (distance < minDistance) {
        minDistance = distance;
        closestVector = v;
      }
    }

    return closestVector;
  }

  /**
   * Rotate this vector about the origin by specified radians.
   *
   * @param angle the angle with which to rotate the vector, in radians.
   */
  public void rotate(double angle) {
    double newX  = x * Math.cos(angle) - y * Math.sin(angle);
    double newY = x * Math.sin(angle) + y * Math.cos(angle);
    x = newX;
    y = newY;
  }
}
