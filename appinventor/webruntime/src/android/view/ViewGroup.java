package android.view;

import com.google.gwt.dom.client.Element;

import com.google.gwt.dom.client.Style;
import java.util.ArrayList;

public class ViewGroup extends View {

  static final String TAG = "ViewGroup";

  ArrayList<View> childViews = new ArrayList<View>();

  public ViewGroup(Element element) {
    super(element);
  }

  public void addView(View v) {
    element.appendChild(v.getElement());
    childViews.add(v);
  }

  public void addView(View v, LayoutParams params) {
    element.appendChild(v.getElement());
    childViews.add(v);
    if (params.width == LayoutParams.WRAP_CONTENT) {
      v.getElement().getStyle().clearWidth();
    } else if (params.width == LayoutParams.MATCH_PARENT) {
      v.getElement().getStyle().setWidth(100, Style.Unit.PCT);
    } else {
      v.getElement().getStyle().setWidth(params.width, Style.Unit.PX);
    }
    if (params.height == LayoutParams.WRAP_CONTENT) {
      v.getElement().getStyle().clearHeight();
    } else if (params.height == LayoutParams.MATCH_PARENT) {
      v.getElement().getStyle().setHeight(100, Style.Unit.PCT);
    } else {
      v.getElement().getStyle().setHeight(params.height, Style.Unit.PX);
    }
  }

  public View getChildAt(int index) {
    return childViews.get(index);
  }

  public int getChildCount() {
    return childViews.size();
  }

  public void removeAllViews() {
    element.removeAllChildren();
  }

  public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    // TODO(ewpatton): Real implementation
  }

  public static class LayoutParams {
    public static final int FILL_PARENT = -1;
    public static final int MATCH_PARENT = -1;
    public static final int WRAP_CONTENT = -2;

    public int width;
    public int height;

    public LayoutParams(int width, int height) {
      this.width = width;
      this.height = height;
    }

    public LayoutParams(LayoutParams source) {
      this.width = source.width;
      this.height = source.height;
    }
  }

  public static class MarginLayoutParams extends ViewGroup.LayoutParams {
    public int leftMargin;
    public int topMargin;
    public int rightMargin;
    public int bottomMargin;
    private int startMargin = DEFAULT_MARGIN_RELATIVE;
    private int endMargin = DEFAULT_MARGIN_RELATIVE;

    byte mMarginFlags;

    public static final int DEFAULT_MARGIN_RELATIVE = Integer.MIN_VALUE;

    private static final int LAYOUT_DIRECTION_MASK = 0x00000003;
    private static final int LEFT_MARGIN_UNDEFINED_MASK = 0x00000004;
    private static final int RIGHT_MARGIN_UNDEFINED_MASK = 0x00000008;
    private static final int RTL_COMPATIBILITY_MODE_MASK = 0x00000010;
    private static final int NEED_RESOLUTION_MASK = 0x00000020;

    private static final int DEFAULT_MARGIN_RESOLVED = 0;
    private static final int UNDEFINED_MARGIN = DEFAULT_MARGIN_RELATIVE;

    public MarginLayoutParams(int width, int height) {
      super(width, height);
    }

    public void setMargins(int left, int top, int right, int bottom) {
      leftMargin = left;
      topMargin = top;
      rightMargin = right;
      bottomMargin = bottom;
      mMarginFlags &= ~LEFT_MARGIN_UNDEFINED_MASK;
      mMarginFlags &= ~RIGHT_MARGIN_UNDEFINED_MASK;
      if (isMarginRelative()) {
        mMarginFlags |= NEED_RESOLUTION_MASK;
      } else {
        mMarginFlags &= ~NEED_RESOLUTION_MASK;
      }
    }

    public boolean isMarginRelative() {
      return (startMargin != DEFAULT_MARGIN_RELATIVE || endMargin != DEFAULT_MARGIN_RELATIVE);
    }
  }

  protected void onSetLayoutParams(View child, LayoutParams layoutParams) {
    requestLayout();
  }
}
