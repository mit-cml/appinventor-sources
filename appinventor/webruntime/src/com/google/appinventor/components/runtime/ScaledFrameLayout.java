package com.google.appinventor.components.runtime;

import android.content.Context;
import android.view.ViewGroup;
import com.google.gwt.dom.client.Document;

public class ScaledFrameLayout extends ViewGroup {
  public ScaledFrameLayout(Context context) {
    super(Document.get().createDivElement());
  }

  public native void setScale(float scale) /*-{
    this.@android.view.View::element.style.transform = "scale(" + scale + ")";
  }-*/;


}
