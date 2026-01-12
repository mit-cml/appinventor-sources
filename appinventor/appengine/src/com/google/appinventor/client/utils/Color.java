package com.google.appinventor.client.utils;

public class Color {
  public static final Color BLACK = new Color(0, 0, 0);

  private final String color;

  public Color(int r, int g, int b) {
    this.color = "rgb(" + r + "," + g + "," + b + ")";
  }

  public Color(int r, int g, int b, float a) {
    this.color = "rgba(" + r + "," + g + "," + b + "," + a + ")";
  }

  @Override
  public String toString() {
    return color;
  }
}
