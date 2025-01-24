package android.widget;

import android.content.Context;
import android.view.ViewGroup;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

public class FrameLayout extends ViewGroup {
  public FrameLayout() {
    this(Document.get().createDivElement());
  }

  public FrameLayout(Context context) {
    this();
  }

  public FrameLayout(Element el) {
    super(el);
    el.setClassName("FrameLayout");
  }

  public static class LayoutParams extends MarginLayoutParams {
    public LayoutParams(int width, int height) {
      super(width, height);
    }
  }
}
