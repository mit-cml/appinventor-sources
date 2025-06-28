package android.graphics.drawable;

public class ColorDrawable extends Drawable {
  private final int color;

  public ColorDrawable(int argb) {
    this.color = argb;
  }

  public int getColor() {
    return color;
  }
}
