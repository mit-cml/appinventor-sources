package android.graphics;

public class Paint {
  public static final int ANTI_ALIAS_FLAG     = 0x01;

  private int mColor;

  public Paint() {

  }

  public Paint(int color) {
    mColor = color;
  }

  public Paint(Paint orig) {
    this.mColor = orig.mColor;
  }

  public void setAntiAlias(boolean aa) {
    // TODO(ewpatton): Real implementation
  }

  public void setColor(int color) {
    mColor = color;
  }

  public Xfermode setXfermode(Xfermode xfermode) {
    // TODO(ewpatton): Real implementation
    return xfermode;
  }
}
