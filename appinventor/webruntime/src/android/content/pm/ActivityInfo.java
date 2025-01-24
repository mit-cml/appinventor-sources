package android.content.pm;

public class ActivityInfo {

  /**
   * Internal constant used to indicate that the app didn't set a specific orientation value.
   * Different from {@link #SCREEN_ORIENTATION_UNSPECIFIED} below as the app can set its
   * orientation to {@link #SCREEN_ORIENTATION_UNSPECIFIED} while this means that the app didn't
   * set anything. The system will mostly treat this similar to
   * {@link #SCREEN_ORIENTATION_UNSPECIFIED}.
   * @hide
   */
  public static final int SCREEN_ORIENTATION_UNSET = -2;
  /**
   * Constant corresponding to <code>unspecified</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_UNSPECIFIED = -1;
  /**
   * Constant corresponding to <code>landscape</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_LANDSCAPE = 0;
  /**
   * Constant corresponding to <code>portrait</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_PORTRAIT = 1;
  /**
   * Constant corresponding to <code>user</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_USER = 2;
  /**
   * Constant corresponding to <code>behind</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_BEHIND = 3;
  /**
   * Constant corresponding to <code>sensor</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_SENSOR = 4;

  /**
   * Constant corresponding to <code>nosensor</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_NOSENSOR = 5;

  /**
   * Constant corresponding to <code>sensorLandscape</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_SENSOR_LANDSCAPE = 6;

  /**
   * Constant corresponding to <code>sensorPortrait</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_SENSOR_PORTRAIT = 7;

  /**
   * Constant corresponding to <code>reverseLandscape</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8;

  /**
   * Constant corresponding to <code>reversePortrait</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_REVERSE_PORTRAIT = 9;

  /**
   * Constant corresponding to <code>fullSensor</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_FULL_SENSOR = 10;

  /**
   * Constant corresponding to <code>userLandscape</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_USER_LANDSCAPE = 11;

  /**
   * Constant corresponding to <code>userPortrait</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_USER_PORTRAIT = 12;

  /**
   * Constant corresponding to <code>fullUser</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_FULL_USER = 13;

  /**
   * Constant corresponding to <code>locked</code> in
   * the {@link android.R.attr#screenOrientation} attribute.
   */
  public static final int SCREEN_ORIENTATION_LOCKED = 14;
}
