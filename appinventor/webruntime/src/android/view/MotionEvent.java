package android.view;

public class MotionEvent {
  public static final int ACTION_DOWN             = 0;
  public static final int ACTION_UP               = 1;
  public static final int ACTION_MOVE             = 2;
  public static final int ACTION_CANCEL           = 3;

  public final int getAction() {
    // TODO: Real implementation
    return ACTION_CANCEL;
  }
}
