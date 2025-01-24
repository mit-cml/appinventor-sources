package android.graphics;

import android.annotation.ColorInt;
import android.annotation.NonNull;

public final class BlendModeColorFilter extends ColorFilter {

  @ColorInt
  final int mColor;
  private final BlendMode mMode;

  public BlendModeColorFilter(@ColorInt int color, @NonNull BlendMode mode) {
    mColor = color;
    mMode = mode;
  }

  /**
   * Returns the ARGB color used to tint the source pixels when this filter
   * is applied.
   *
   * @see Color
   *
   */
  @ColorInt
  public int getColor() {
    return mColor;
  }

  /**
   * Returns the Porter-Duff mode used to composite this color filter's
   * color with the source pixel when this filter is applied.
   *
   * @see BlendMode
   *
   */
  public BlendMode getMode() {
    return mMode;
  }
}
