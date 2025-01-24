package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.util.StateSet;
import android.widget.TextView;

public abstract class Drawable {
  static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.SRC_IN;
  static final BlendMode DEFAULT_BLEND_MODE = BlendMode.SRC_IN;
  private int[] mStateSet = StateSet.WILD_CARD;

  public static abstract class ConstantState {
    /**
     * Creates a new Drawable instance from its constant state.
     * <p>
     * <strong>Note:</strong> Using this method means density-dependent
     * properties, such as pixel dimensions or bitmap images, will not be
     * updated to match the density of the target display. To ensure
     * correct scaling, use {@link #newDrawable(Resources)} instead to
     * provide an appropriate Resources object.
     *
     * @return a new drawable object based on this constant state
     * @see #newDrawable(Resources)
     */
    public abstract Drawable newDrawable();

    /**
     * Creates a new Drawable instance from its constant state using the
     * specified resources. This method should be implemented for drawables
     * that have density-dependent properties.
     * <p>
     * The default implementation for this method calls through to
     * {@link #newDrawable()}.
     *
     * @param res the resources of the context in which the drawable will
     *            be displayed
     * @return a new drawable object based on this constant state
     */
    public Drawable newDrawable(Resources res) {
      return newDrawable();
    }

    /**
     * Creates a new Drawable instance from its constant state using the
     * specified resources and theme. This method should be implemented for
     * drawables that have theme-dependent properties.
     * <p>
     * The default implementation for this method calls through to
     * {@link #newDrawable(Resources)}.
     *
     * @param res the resources of the context in which the drawable will
     *            be displayed
     * @param theme the theme of the context in which the drawable will be
     *              displayed
     * @return a new drawable object based on this constant state
     */
    public Drawable newDrawable(Resources res,
        @SuppressWarnings("unused")Theme theme) {
      return newDrawable(res);
    }

    /**
     * Return a bit mask of configuration changes that will impact
     * this drawable (and thus require completely reloading it).
     */
    public abstract int getChangingConfigurations();

    /**
     * Return whether this constant state can have a theme applied.
     */
    public boolean canApplyTheme() {
      return false;
    }
  }

  public ConstantState getConstantState() {
    // TODO(ewpatton): Real implementation
    return null;
  }


  /**
   * Specify an optional color filter for the drawable.
   * <p>
   * If a Drawable has a ColorFilter, each output pixel of the Drawable's
   * drawing contents will be modified by the color filter before it is
   * blended onto the render target of a Canvas.
   * </p>
   * <p>
   * Pass {@code null} to remove any existing color filter.
   * </p>
   * <p class="note"><strong>Note:</strong> Setting a non-{@code null} color
   * filter disables {@link #setTintList(ColorStateList) tint}.
   * </p>
   *
   * @param colorFilter The color filter to apply, or {@code null} to remove the
   *            existing color filter
   */
  public void setColorFilter(ColorFilter colorFilter) {
    // TODO(ewpatton): Real implementation
  }

  public void setColorFilter(int color, PorterDuff.Mode mode) {
    // TODO(ewpatton): Real implementation
  }

  public void setAlpha(int alpha) {
    // TODO(ewpatton): Real implementation
  }

  public int getIntrinsicWidth() {
    // TODO(ewpatton): Real implementation
    return 0;
  }

  public int getIntrinsicHeight() {
    // TODO(ewpatton): Real implementation
    return 0;
  }

  public Drawable mutate() {
    return this;
  }

  /**
   * Describes the current state, as a union of primitve states, such as
   * {@link android.R.attr#state_focused},
   * {@link android.R.attr#state_selected}, etc.
   * Some drawables may modify their imagery based on the selected state.
   * @return An array of resource Ids describing the current state.
   */
  public int[] getState() {
    return mStateSet;
  }

  BlendModeColorFilter updateBlendModeFilter(BlendModeColorFilter blendFilter,
      ColorStateList tint, BlendMode blendMode) {
    if (tint == null || blendMode == null) {
      return null;
    }

    final int color = tint.getColorForState(getState(), Color.TRANSPARENT);
    if (blendFilter == null || blendFilter.getColor() != color
        || blendFilter.getMode() != blendMode) {
      return new BlendModeColorFilter(color, blendMode);
    }
    return blendFilter;
  }
}
