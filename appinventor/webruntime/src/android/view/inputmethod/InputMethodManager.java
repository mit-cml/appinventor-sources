package android.view.inputmethod;

import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.View;

public class InputMethodManager {

  /**
   * Synonym for {@link #hideSoftInputFromWindow(IBinder, int, ResultReceiver)}
   * without a result: request to hide the soft input window from the
   * context of the window that is currently accepting input.
   *
   * @param windowToken The token of the window that is making the request,
   * as returned by {@link View#getWindowToken() View.getWindowToken()}.
   * @param flags Provides additional operating flags.  Currently may be
   * 0 or have the {@link #HIDE_IMPLICIT_ONLY} bit set.
   */
  public boolean hideSoftInputFromWindow(IBinder windowToken, int flags) {
    return false;
  }
}
