package android.view;

public class Gravity {
  /** Raw bit indicating the gravity for an axis has been specified. */
  public static final int AXIS_SPECIFIED = 0x0001;

  /** Bits defining the horizontal axis. */
  public static final int AXIS_X_SHIFT = 0;
  /** Bits defining the vertical axis. */
  public static final int AXIS_Y_SHIFT = 4;

  /** Raw bit controlling how the left/top edge is placed. */
  public static final int AXIS_PULL_BEFORE = 0x0002;
  /** Raw bit controlling how the right/bottom edge is placed. */
  public static final int AXIS_PULL_AFTER = 0x0004;

  public static final int NO_GRAVITY = 0x00000000;
  public static final int TOP = 0x00000030;
  public static final int CENTER = 0x00000011;
  public static final int BOTTOM = 0x00000050;
  public static final int RELATIVE_LAYOUT_DIRECTION = 0x00800000;

  /** Push object to the left of its container, not changing its size. */
  public static final int LEFT = (AXIS_PULL_BEFORE|AXIS_SPECIFIED)<<AXIS_X_SHIFT;
  /** Push object to the right of its container, not changing its size. */
  public static final int RIGHT = (AXIS_PULL_AFTER|AXIS_SPECIFIED)<<AXIS_X_SHIFT;

  public static final int START = 0x00800003;
  public static final int END = RELATIVE_LAYOUT_DIRECTION | RIGHT;
  public static final int RELATIVE_HORIZONTAL_GRAVITY_MASK = START | END;
  public static final int VERTICAL_GRAVITY_MASK = (AXIS_SPECIFIED |
      AXIS_PULL_BEFORE | AXIS_PULL_AFTER) << AXIS_Y_SHIFT;

  public static final int CENTER_VERTICAL = AXIS_SPECIFIED<<AXIS_Y_SHIFT;
  public static final int CENTER_HORIZONTAL = AXIS_SPECIFIED<<AXIS_X_SHIFT;
}
