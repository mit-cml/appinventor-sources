
package com.google.appinventor.components.common;

public enum Direction {
  North(1),
  Northeast(2),
  // @Default
  East(3),
  Southeast(4),
  South(-1),
  Southwest(-2),
  West(-3),
  Northwest(-4);

  private int value;

  Direction(int dir) {
    this.value = dir;
  }

  public int getValue() {
    return value;
  }

  private static final Map<String, Direction> lookup = new HashMap<>();

  static {
    for(Direction dir : Direction.values()) {
      lookup.put(dir.getValue(), dir);
    }
  }

  public static Direction get(int dir) {
    return lookup
  }
}
