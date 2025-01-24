package android.widget;

import android.content.Context;
import android.view.View;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;

public class ScrollView extends FrameLayout {
  private boolean mFillViewport;

  public ScrollView() {
    super();
    DivElement el = (DivElement) getElement();
    el.addClassName("ScrollView");
  }

  public ScrollView(Context context) {
    this();
  }

  public ScrollView(Element element) {
    super(element);
  }

  public void scrollTo(int x, int y) {
    DivElement.as(element).setScrollLeft(x);
    DivElement.as(element).setScrollTop(y);
  }

  public int getScrollX() {
    return DivElement.as(element).getScrollLeft();
  }

  public int getScrollY() {
    return DivElement.as(element).getScrollTop();
  }

  public boolean fullScroll(int direction) {
    if (direction == View.FOCUS_UP) {
      DivElement.as(element).setScrollTop(0);
    } else if (direction == View.FOCUS_DOWN) {
      DivElement.as(element).setScrollTop(9999999);
    }
    return true;
  }
  /**
   * Indicates this ScrollView whether it should stretch its content height to fill
   * the viewport or not.
   *
   * @param fillViewport True to stretch the content's height to the viewport's
   *        boundaries, false otherwise.
   *
   * @attr ref android.R.styleable#ScrollView_fillViewport
   */
  public void setFillViewport(boolean fillViewport) {
    if (fillViewport != mFillViewport) {
      element.addClassName("fillViewport");
      mFillViewport = fillViewport;
      requestLayout();
    }
  }
}
