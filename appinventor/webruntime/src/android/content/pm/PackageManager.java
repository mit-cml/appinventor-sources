package android.content.pm;

public class PackageManager {
  /**
   * Permission check result: this is returned by {@link #checkPermission}
   * if the permission has been granted to the given package.
   */
  public static final int PERMISSION_GRANTED = 0;

  /**
   * Permission check result: this is returned by {@link #checkPermission}
   * if the permission has not been granted to the given package.
   */
  public static final int PERMISSION_DENIED = -1;
}
