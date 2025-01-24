package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.shapes.Shape;

public class ShapeDrawable extends Drawable {
  private ShapeState mShapeState;
  private BlendModeColorFilter mBlendModeColorFilter;
  private boolean mMutated;

  /**
   * ShapeDrawable constructor.
   */
  public ShapeDrawable() {
    this(new ShapeState(), null);
  }

  /**
   * Creates a ShapeDrawable with a specified Shape.
   *
   * @param s the Shape that this ShapeDrawable should be
   */
  public ShapeDrawable(Shape s) {
    this(new ShapeState(), null);

    mShapeState.mShape = s;
  }


  /**
   * Defines the intrinsic properties of this ShapeDrawable's Shape.
   */
  static final class ShapeState extends ConstantState {
    final Paint mPaint;

    int mChangingConfigurations;
    int[] mThemeAttrs;
    Shape mShape;
    ColorStateList mTint;
    BlendMode mBlendMode = DEFAULT_BLEND_MODE;
    Rect mPadding;
    int mIntrinsicWidth;
    int mIntrinsicHeight;
    int mAlpha = 255;

    /**
     * Constructs a new ShapeState.
     */
    ShapeState() {
      mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    /**
     * Constructs a new ShapeState that contains a deep copy of the
     * specified ShapeState.
     *
     * @param orig the state to create a deep copy of
     */
    ShapeState(ShapeState orig) {
      mChangingConfigurations = orig.mChangingConfigurations;
      mPaint = new Paint(orig.mPaint);
      mThemeAttrs = orig.mThemeAttrs;
      if (orig.mShape != null) {
        mShape = orig.mShape;
      }
      mTint = orig.mTint;
      mBlendMode = orig.mBlendMode;
      if (orig.mPadding != null) {
        mPadding = new Rect(orig.mPadding);
      }
      mIntrinsicWidth = orig.mIntrinsicWidth;
      mIntrinsicHeight = orig.mIntrinsicHeight;
      mAlpha = orig.mAlpha;
    }

    @Override
    public boolean canApplyTheme() {
      return mThemeAttrs != null;
    }

    @Override
    public Drawable newDrawable() {
      return new ShapeDrawable(new ShapeState(this), null);
    }

    @Override
    public Drawable newDrawable(Resources res) {
      return new ShapeDrawable(new ShapeState(this), res);
    }

    @Override
    public int getChangingConfigurations() {
      return mChangingConfigurations;
    }
  }

  /**
   * The one constructor to rule them all. This is called by all public
   * constructors to set the state and initialize local properties.
   */
  private ShapeDrawable(ShapeState state, Resources res) {
    mShapeState = state;

    updateLocalState();
  }

  /**
   * Initializes local dynamic properties from state. This should be called
   * after significant state changes, e.g. from the One True Constructor and
   * after inflating or applying a theme.
   */
  private void updateLocalState() {
    mBlendModeColorFilter = updateBlendModeFilter(mBlendModeColorFilter, mShapeState.mTint,
        mShapeState.mBlendMode);
  }


  /**
   * Returns the Shape of this ShapeDrawable.
   */
  public Shape getShape() {
    return mShapeState.mShape;
  }

  /**
   * Sets the Shape of this ShapeDrawable.
   */
  public void setShape(Shape s) {
    mShapeState.mShape = s;
    //updateShape();
  }

  /**
   * Returns the Paint used to draw the shape.
   */
  public Paint getPaint() {
    return mShapeState.mPaint;
  }
}
