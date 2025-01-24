package android.widget;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;

public class TextView extends View {

  public TextView(Context context) {
    this(DOM.createDiv());
  }

  public TextView(Element element) {
    super(element);
    mLayoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
  }

  public String getText() {
    return element.getInnerText();
  }

  public void setText(int stringId) {
    setText(Context.resources.getString(stringId));
  }

  public void setText(CharSequence text) {
    if (text == null) {
      element.setInnerHTML("");
    } else if (text instanceof Spanned) {
      element.setInnerHTML(text.toString());
    } else {
      element.setInnerHTML(text.toString().replace("\n", "<br/>"));
    }
  }

  public void setWidth(int pixels) {
    // TODO(ewpatton): Real implementation
  }

  public void setPadding(int left, int top, int right, int bottom) {
    // TODO(ewpatton): Real implementation
  }

  public void setHint(CharSequence hint) {
    // TODO(ewpatton): Real implementation
  }

  public void addTextChangedListener(TextWatcher watcher) {
    // TODO(ewpatton): Real implementation
  }

  public void setTextAppearance(Context context, int resId) {
    // TODO(ewpatton): Real implementation
  }

  public void setGravity(int gravity) {
    // TODO(ewpatton): Real implementation
  }

  public void setShadowLayer(float radius, float dx, float dy, int color) {
    // TODO(ewpatton): Real implementation
  }

  public void setSingleLine() {
    setSingleLine(true);
  }

  public void setSingleLine(boolean singleLine) {
    // TODO(ewpatton): Real implementation
  }

  public ColorStateList getTextColors() {
    // TODO(ewpatton): Real implementation
    return null;
  }

  public void setTextColor(int color) {
    element.getStyle().setProperty("color", Color.getHtmlColor(color));
  }

  public native int getLineHeight()
    /*-{
      return parseInt(document.defaultView.getComputedStyle(this.@android.view.View::element,null).getPropertyValue("line-height"));
    }-*/;
}
