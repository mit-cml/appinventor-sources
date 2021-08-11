package com.google.appinventor.components.runtime.util;

public class Vector2D {
  // vector with tail at (0, 0) and head at (x, y)
  private double x;
  private double y;

  public Vector2D(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double magnitude() {
    return Math.sqrt(magnitudeSquared());
  }

  public double magnitudeSquared() {
    return x * x + y * y;
  }

  public boolean isGreater(Vector2D that) {
    return this.magnitudeSquared() > that.magnitudeSquared();
  }

  public Vector2D getNormalVector() {
    // dot product = x * y + y * (-x) = 0
    return new Vector2D(y, -x);
  }

  public static Vector2D difference(Vector2D vector1, Vector2D vector2) {
    return new Vector2D(vector1.getX() - vector2.getX(), vector1.getY() - vector2.getY());
  }

  public static Vector2D addition(Vector2D vector1, Vector2D vector2) {
    return new Vector2D(vector1.getX() + vector2.getX(), vector1.getY() + vector2.getY());
  }

  public static double dotProduct(Vector2D vector1, Vector2D vector2) {
    return vector1.getX() * vector2.getX() + vector1.getY() * vector2.getY();
  }

  public Vector2D unitVector() {
    return new Vector2D(getX() / magnitude(), getY() / magnitude());
  }

  // rotate the vector by angle radians
  public void rotate(double angle) {
    double newX  = x * Math.cos(angle) - y * Math.sin(angle);
    double newY = x * Math.sin(angle) + y * Math.cos(angle);
    x = newX;
    y = newY;
  }
}