package android.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;

public class LinearLayout extends ViewGroup {

  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;
  private boolean mBaselineAligned = true;
  private int mGravity = Gravity.START | Gravity.TOP;

  public LinearLayout(Context context) {
    super(DOM.createDiv());
  }

  public LinearLayout(Element element) {
    super(element);
  }

  public LinearLayout(View view) {
    super(DOM.createDiv());
    getElement().appendChild(view.getElement());
  }

  public void setOrientation(int orientation) {
    // TODO(ewpatton): Real implementation
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
  }

  public void setVerticalGravity(int verticalGravity) {
    final int gravity = verticalGravity & Gravity.VERTICAL_GRAVITY_MASK;
    if ((mGravity & Gravity.VERTICAL_GRAVITY_MASK) != gravity) {
      mGravity = (mGravity & ~Gravity.VERTICAL_GRAVITY_MASK) | gravity;
      requestLayout();
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
