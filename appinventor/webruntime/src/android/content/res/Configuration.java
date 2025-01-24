package android.content.res;

public class Configuration {

  /** Constant for {@link #orientation}: a value indicating that no value has been set. */
  public static final int ORIENTATION_UNDEFINED = 0;
  /** Constant for {@link #orientation}, value corresponding to the
   * <a href="{@docRoot}guide/topics/resources/providing-resources.html#OrientationQualifier">port</a>
   * resource qualifier. */
  public static final int ORIENTATION_PORTRAIT = 1;
  /** Constant for {@link #orientation}, value corresponding to the
   * <a href="{@docRoot}guide/topics/resources/providing-resources.html#OrientationQualifier">land</a>
   * resource qualifier. */
  public static final int ORIENTATION_LANDSCAPE = 2;
  /** @deprecated Not currently supported or used. */
  @Deprecated public static final int ORIENTATION_SQUARE = 3;

  public int orientation;
}
