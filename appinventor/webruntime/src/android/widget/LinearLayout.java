package android.widget;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;

public class LinearLayout extends ViewGroup {
  private static final String LOG_TAG = "LinearLayout";

  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;
  private boolean mBaselineAligned = true;
  private int mGravity = Gravity.START | Gravity.TOP;
  private int mOrientation = HORIZONTAL;

  public LinearLayout(Context context) {
    this(DOM.createDiv());
  }

  public LinearLayout(Element element) {
    super(element);
    setOrientation(HORIZONTAL);
    element.setClassName("LinearLayout");
  }

  public LinearLayout(View view) {
    this(DOM.createDiv());
    getElement().appendChild(view.getElement());
  }

  public void setOrientation(int orientation) {
    element.removeClassName(orientation == HORIZONTAL ? "vertical" : "horizontal");
    element.addClassName(orientation == HORIZONTAL ? "horizontal" : "vertical");
  }

  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // TODO(ewpatton): Real implementation
  }

  public void setHorizontalGravity(int horizontalGravity) {
    final int gravity = horizontalGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
    if ((mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) != gravity) {
      mGravity = (mGravity & ~Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) | gravity;
      requestLayout();
    }
    switch (horizontalGravity) {
      case Gravity.LEFT:
        element.removeClassName("center_horizontal");
        element.removeClassName("right");
        element.addClassName("left");
        break;
      case Gravity.CENTER_HORIZONTAL:
        element.removeClassName("left");
        element.removeClassName("right");
        element.addClassName("center_horizontal");
        break;
      case Gravity.RIGHT:
        element.removeClassName("left");
        element.removeClassName("center_horizontal");
        element.addClassName("right");
        break;
      default:
        Log.e(LOG_TAG, "Bad value to setHorizontalGravity: " + horizontalGravity);
    }
  }

  public void setVerticalGravity(int verticalGravity) {
    final int gravity = verticalGravity & Gravity.VERTICAL_GRAVITY_MASK;
    if ((mGravity & Gravity.VERTICAL_GRAVITY_MASK) != gravity) {
      mGravity = (mGravity & ~Gravity.VERTICAL_GRAVITY_MASK) | gravity;
      requestLayout();
    }
    switch (verticalGravity) {
      case Gravity.TOP:
        element.removeClassName("center_vertical");
        element.removeClassName("bottom");
        element.addClassName("top");
        break;
      case Gravity.CENTER_VERTICAL:
        element.removeClassName("top");
        element.removeClassName("bottom");
        element.addClassName("center_vertical");
        break;
      case Gravity.BOTTOM:
        element.removeClassName("top");
        element.removeClassName("center_vertical");
        element.addClassName("bottom");
        break;
      default:
        Log.e(LOG_TAG, "Bad value to setVerticalGravity: " + verticalGravity);
    }
  }

  public void setBaselineAligned(boolean baselineAligned) {
    mBaselineAligned = baselineAligned;
  }

  public static class LayoutParams extends ViewGroup.MarginLayoutParams {
    public float weight;

    public LayoutParams(int width, int height) {
      super(width, height);
      weight = 0;
    }

    public LayoutParams(int width, int height, float weight) {
      super(width, height);
      this.weight = weight;
    }
  }
}
