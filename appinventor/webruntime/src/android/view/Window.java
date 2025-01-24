package android.view;

import com.google.gwt.dom.client.Document;

public class Window {
  public static final int FEATURE_ACTION_BAR = 8;
  private Callback mCallback = new Callback() {
    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
    }
  };
  private int mForcedWindowFlags = 0;
  private final WindowManager.LayoutParams mWindowAttributes =
      new WindowManager.LayoutParams();
  private boolean mHasSoftInputMode = false;
  private int mFeatures;
  private int mLocalFeatures;
  private Window mContainer;
  private View mDecorView = new View(Document.get().getElementById("activity"));

  public final Callback getCallback() {
    return mCallback;
  }

  public void setCallback(Callback callback) {
    mCallback = callback;
  }

  public interface Callback {
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs);
  }

  public final WindowManager.LayoutParams getAttributes() {
    return mWindowAttributes;
  }

  public void setSoftInputMode(int mode) {
    final WindowManager.LayoutParams attrs = getAttributes();
    if (mode != WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
      attrs.softInputMode = mode;
      mHasSoftInputMode = true;
    } else {
      mHasSoftInputMode = false;
    }
    dispatchWindowAttributesChanged(attrs);
  }

  public void addFlags(int flags) {
    setFlags(flags, flags);
  }

  public void clearFlags(int flags) {
    setFlags(0, flags);
  }

  public void setFlags(int flags, int mask) {
    final WindowManager.LayoutParams attrs = getAttributes();
    attrs.flags = (attrs.flags&~mask) | (flags&mask);
    mForcedWindowFlags |= mask;
    dispatchWindowAttributesChanged(attrs);
  }

  /**
   * {@hide}
   */
  protected void dispatchWindowAttributesChanged(WindowManager.LayoutParams attrs) {
    if (mCallback != null) {
      mCallback.onWindowAttributesChanged(attrs);
    }
  }

  public boolean requestFeature(int featureId) {
    final int flag = 1<<featureId;
    mFeatures |= flag;
    mLocalFeatures |= mContainer != null ? (flag&~mContainer.mFeatures) : flag;
    return (mFeatures&flag) != 0;
  }

  public View getDecorView() {
    // TODO(ewpatton): Real implementation
    return mDecorView;
  }

  public View findViewById(int id) {
    return getDecorView().findViewById(id);
  }

  public View findViewById(String id) {
    return getDecorView().findViewById(id);
  }
}
