package android.view;

import android.AndroidManifest;
import android.Res;
import android.animation.StateListAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.Log;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

public class View {
  static final String TAG = "View";
  public static final int VISIBLE = 0;
  public static final int INVISIBLE = 4;
  public static final int GONE = 8;

  public static final int FOCUS_DOWN = 130;
  public static final int FOCUS_UP = 33;

  static final int PFLAG_FORCE_LAYOUT                = 0x00001000;

  public Element element = null;
  int id;
  protected View mParent = null;
  public int mPrivateFlags;
  private OnTouchListener mOnTouchListener;
  private OnLongClickListener mOnLongClickListener;
  private OnClickListener mOnClickListener;
  private OnFocusChangeListener mOnFocusChangeListener;
  protected ViewGroup.LayoutParams mLayoutParams;
  private JavaScriptObject nativeClickListener;

  public void setOnTouchListener(OnTouchListener l) {
    mOnTouchListener = l;
  }

  public void setOnLongClickListener(OnLongClickListener l) {
    mOnLongClickListener = l;
  }

  public void setOnFocusChangeListener(OnFocusChangeListener l) {
    mOnFocusChangeListener = l;
  }

  /**
   * Special tree observer used when mAttachInfo is null.
   */
  private ViewTreeObserver mFloatingTreeObserver;

  public View(Element element) {
    this.element = element;
  }

  public View(Widget widget) {
    this.element = widget.getElement();
  }

  public final Context getContext() {
    return AndroidManifest.applicatonContext;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getTop() {
    return element.getOffsetTop();
  }

  public int getLeft() {
    return element.getOffsetLeft();
  }

  public int getRight() {
    return element.getOffsetLeft() + element.getClientWidth();
  }

  public int getBottom() {
    return element.getOffsetTop() + element.getClientHeight();
  }

  public int getHeight() {
    return element.getClientHeight();
  }

  public int getWidth() {
    return element.getClientWidth();
  }

  public void getLocationOnScreen(int[] location) {
    location[0] = element.getAbsoluteLeft() - Window.getScrollLeft();
    location[1] = element.getAbsoluteTop() - Window.getScrollTop();
  }

  public View findViewById(int id) {
    View view = ViewFactory.findViewById(getElement(), Context.resources.getIdAsString(id));
    if (view != null) {
      view.setId(id);
    }
    return view;
  }

  public View findViewById(String id) {
    return ViewFactory.findViewById(getElement(), id);
  }

  public void setVisibility(int visibility) {
    if ((visibility & View.INVISIBLE) != 0) {
      element.addClassName(Res.R.style().invisible());
    } else {
      element.removeClassName(Res.R.style().invisible());
    }

    if ((visibility & View.GONE) != 0) {
      element.addClassName(Res.R.style().gone());
    } else {
      element.removeClassName(Res.R.style().gone());
    }
  }

  public int getVisibility() {
    if (element.hasClassName(Res.R.style().invisible())) {
      return View.INVISIBLE;
    }
    if (element.hasClassName(Res.R.style().gone())) {
      return View.GONE;
    }
    return View.VISIBLE;
  }

  public void setEnabled(boolean enabled) {
    if (enabled) {
      element.removeAttribute("disabled");
    } else {
      element.setAttribute("disabled", "disabled");
    }
  }

  public boolean isEnabled() {
    return !element.hasAttribute("disabled");
  }

  public void setOnClickListener(final OnClickListener listener) {
    Event.setEventListener(element, new EventListener() {
      @Override
      public void onBrowserEvent(Event event) {
        listener.onClick(View.this);
      }
    });
    Event.sinkEvents(element, Event.ONCLICK);
  }

  public native void setFocusable(boolean focusable) /*-{
    if (focusable) {
      this.@android.view.View::element.setAttribute("tabindex", "0");
    } else {
      this.@android.view.View::element.removeAttribute("tabindex");
    }
  }-*/;

  public native void setClickable(boolean clickable) /*-{
    if (this.@android.view.View::nativeClickListener != null) {
      this.@android.view.View::element.removeEventListener("click", this.@android.view.View::nativeClickListener);
      this.@android.view.View::nativeClickListener = null;
    }
    if (clickable) {
      this.@android.view.View::element.setAttribute("role", "button");
      this.@android.view.View::nativeClickListener = $entry(function(event) {
        if (this.@android.view.View::mOnClickListener != null) {
          this.@android.view.View::mOnClickListener.@android.view.View.OnClickListener::onClick(*)(this);
        }
      });
    } else {
      this.@android.view.View::element.removeAttribute("role");
    }
  }-*/;

  public static interface OnClickListener {
    abstract void onClick(View v);
  }

  public Element getElement() {
    return element;
  }

  /**
   * No JS Threads, so run now
   *
   * @param action
   * @return
   */
  public boolean post(Runnable action) {
    action.run();
    return true;
  }

  public void invalidate() {
    onDraw(null);
  }

  protected void onDraw(Canvas canvas) {

  }

  public void setBackgroundColor(int color) {
    element.getStyle().setProperty("background", Color.getHtmlColor(color));
  }

  public void setBackgroundDrawable(Drawable background) {
    if (background instanceof BitmapDrawable) {
      setBackgroundColor(Color.TRANSPARENT);
      nativeSetBackgroundDrawable(((BitmapDrawable) background).getDataUrl());
    } else if (background instanceof ColorDrawable) {
      setBackgroundColor(((ColorDrawable) background).getColor());
    } else if (background != null) {
      Log.w("View", "Unsupported background drawable type: " + background.getClass().getName());
    }
  }

  private native void nativeSetBackgroundDrawable(String dataUrl) /*-{
    var img;
    var el = this.@android.view.View::element;
    if (el.querySelector(".background-image")) {
      img = el.querySelector(".background-image");
    } else {
      img = $doc.createElement("img");
      img.className = "background-image";
      el.appendChild(img);
      img.style.visibility = "hidden"; // Hide the image element
    }
    img.src = dataUrl;
    el.style.backgroundImage = "url('" + img.src + "')";
    el.style.backgroundSize = "cover";
    el.style.backgroundRepeat = "no-repeat";
    el.style.backgroundPosition = "center center";
    el.style.backgroundColor = "transparent";
    el.style.backgroundAttachment = "fixed";
    el.style.backgroundClip = "padding-box";
    el.style.backgroundOrigin = "padding-box";
    el.style.backgroundBlendMode = "normal";
    el.classList.add("has-bg-image");
  }-*/;

  @Override
  public boolean equals(Object v) {
    if (!(v instanceof View)) {
      return false;
    }
    return ((View) v).id == id;
  }

  /**
   * Returns the ViewTreeObserver for this view's hierarchy. The view tree
   * observer can be used to get notifications when global events, like
   * layout, happen.
   *
   * The returned ViewTreeObserver observer is not guaranteed to remain
   * valid for the lifetime of this View. If the caller of this method keeps
   * a long-lived reference to ViewTreeObserver, it should always check for
   * the return value of {@link ViewTreeObserver#isAlive()}.
   *
   * @return The ViewTreeObserver for this view's hierarchy.
   */
  public ViewTreeObserver getViewTreeObserver() {
    if (mFloatingTreeObserver == null) {
      mFloatingTreeObserver = new ViewTreeObserver(getContext());
    }
    return mFloatingTreeObserver;
  }

  /**
   * <p>Finds the topmost view in the current view hierarchy.</p>
   *
   * @return the topmost view containing this view
   */
  public View getRootView() {
    View parent = this;

    while (parent.mParent instanceof View) {
      parent = (View) parent.mParent;
    }

    return parent;
  }

  public View getParent() {
    return mParent;
  }

  public void requestLayout() {
    if (mParent != null && !mParent.isLayoutRequested()) {
      mParent.requestLayout();
    }
  }

  public boolean isLayoutRequested() {
    return (mPrivateFlags & PFLAG_FORCE_LAYOUT) == PFLAG_FORCE_LAYOUT;
  }

  public IBinder getWindowToken() {
    return null;
  }

  public static class MeasureSpec {
    private static final int MODE_SHIFT = 30;
    private static final int MODE_MASK  = 0x3 << MODE_SHIFT;

    /**
     * Measure specification mode: The parent has not imposed any constraint
     * on the child. It can be whatever size it wants.
     */
    public static final int UNSPECIFIED = 0 << MODE_SHIFT;

    /**
     * Measure specification mode: The parent has determined an exact size
     * for the child. The child is going to be given those bounds regardless
     * of how big it wants to be.
     */
    public static final int EXACTLY     = 1 << MODE_SHIFT;

    /**
     * Measure specification mode: The child can be as large as it wants up
     * to the specified size.
     */
    public static final int AT_MOST     = 2 << MODE_SHIFT;

    public static int getMode(int measureSpec) {
      //noinspection ResourceType
      return (measureSpec & MODE_MASK);
    }

    public static int getSize(int measureSpec) {
      return (measureSpec & ~MODE_MASK);
    }
  }

  public void setBackgroundResource(int resId) {
    // TODO(ewpatton): Real implementation
    setBackgroundColor(Context.resources.getColor(resId));
  }

  protected final void setMeasuredDimension(int measuredWidth, int measuredHeight) {
    // TODO(ewpatton): Real implementation
  }

  public interface OnTouchListener {
    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *        the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    boolean onTouch(View v, MotionEvent event);
  }

  public interface OnFocusChangeListener {
    /**
     * Called when the focus state of a view has changed.
     *
     * @param v The view whose state has changed.
     * @param hasFocus The new focus state of v.
     */
    void onFocusChange(View v, boolean hasFocus);
  }

  public interface OnLongClickListener {
    /**
     * Called when a view has been clicked and held.
     *
     * @param v The view that was clicked and held.
     * @return true if the callback consumed the long click, false otherwise.
     */
    boolean onLongClick(View v);
  }

  public Drawable getBackground() {
    // TODO(ewpatton): Real implementation
    return null;
  }

  public ViewOutlineProvider getOutlineProvider() {
    // TODO(ewpatton): Real implementation
    return null;
  }

  public void setOutlineProvider(ViewOutlineProvider provider) {
    // TODO(ewpatton): Real implementation
  }

  public StateListAnimator getStateListAnimator() {
    // TODO(ewpatton): Real implementation
    return null;
  }

  public void setStateListAnimator(StateListAnimator stateListAnimator) {
    // TODO(ewpatton): Real implementation
  }

  public final int[] getDrawableState() {
    return new int[0];
  }

  public ViewGroup.LayoutParams getLayoutParams() {
    return mLayoutParams;
  }

  public void setLayoutParams(ViewGroup.LayoutParams params) {
    if (params == null) {
      throw new NullPointerException("Layout parameters cannot be null");
    }
    mLayoutParams = params;
    resolveLayoutParams();
    if (mParent instanceof ViewGroup) {
      ((ViewGroup) mParent).onSetLayoutParams(this, params);
    }
    requestLayout();
  }

  public void resolveLayoutParams() {
    // TODO(ewpatton): Real implementation
  }

  public native void setRotation(float rotation) /*-{
    this.@android.view.View::element.style.transform = "rotate(" + rotation + "deg)";
  }-*/;

  public void clearAnimation() {
    // TODO(ewpatton): Real implementation
    element.getStyle().setProperty("animation", "none");
  }

  public void startAnimation(Object animation) {
    // TODO(ewpatton): Real implementation
  }
}
