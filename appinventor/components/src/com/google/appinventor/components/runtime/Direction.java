
// TODO: Not sure which package these should go in.
package com.google.appinventor.components.runtime;

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
}