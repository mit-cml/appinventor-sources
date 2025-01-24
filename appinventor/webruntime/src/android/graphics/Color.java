package android.graphics;

public class Color {

  public static final int BLACK = 0xff000000;
  public static final int BLUE = 0xff0000ff;
  public static final int CYAN = 0xff00ffff;
  public static final int DKGRAY = 0xff444444;
  public static final int GRAY = 0xff888888;
  public static final int GREEN = 0xff00ff00;
  public static final int LTGRAY = 0xffcccccc;
  public static final int MAGENTA = 0xffff00ff;
  public static final int RED = 0xffff0000;
  public static final int TRANSPARENT = 0x00000000;
  public static final int WHITE = 0xffffffff;
  public static final int YELLOW = 0xffffff00;

  public static int parseColor(String colorString) {
    return Integer.decode(colorString.replace("#", "0x"));
  }

  /**
   * Not part of Android, used for internal purposes
   */
  public static String getHtmlColor(int color) {
    String hex = Integer.toHexString(color);
    while (hex.length() < 6) {
      hex = "0" + hex;
    }
    return "#" + hex;
  }

  public static int argb(int alpha, int red, int green, int blue) {
    return (alpha << 24) | (red << 16) | (green << 8) | blue;
  }

  public static int red(int color) {
    return (color >> 16) & 0xff;
  }

  public static int green(int color) {
    return (color >> 8) & 0xff;
  }

  public static int blue(int color) {
    return color & 0xff;
  }
}
