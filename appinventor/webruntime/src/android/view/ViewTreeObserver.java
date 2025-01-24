package android.view;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class ViewTreeObserver {

  private boolean mAlive = true;
  private List<OnGlobalLayoutListener> mOnGlobalLayoutListeners;

  public interface OnGlobalLayoutListener {
    /**
     * Callback method to be invoked when the global layout state or the visibility of views
     * within the view tree changes
     */
    public void onGlobalLayout();
  }

  public ViewTreeObserver(Context context) {
  }

  private void checkIsAlive() {
    if (!mAlive) {
      throw new IllegalStateException("This ViewTreeObserver is not alive, call "
          + "getViewTreeObserver() again");
    }
  }

  /**
   * Register a callback to be invoked when the global layout state or the visibility of views
   * within the view tree changes
   *
   * @param listener The callback to add
   *
   * @throws IllegalStateException If {@link #isAlive()} returns false
   */
  public void addOnGlobalLayoutListener(OnGlobalLayoutListener listener) {
    checkIsAlive();

    if (mOnGlobalLayoutListeners == null) {
      mOnGlobalLayoutListeners = new ArrayList<>();
    }

    mOnGlobalLayoutListeners.add(listener);
  }

}
