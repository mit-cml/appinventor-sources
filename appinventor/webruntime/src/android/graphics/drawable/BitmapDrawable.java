package android.graphics.drawable;

import android.graphics.Bitmap;

public class BitmapDrawable extends Drawable {
  private Bitmap mBitmap = null;

  public BitmapDrawable() {
    // TODO Auto-generated constructor stub
  }

  public BitmapDrawable(Bitmap bitmap) {
    // TODO Auto-generated constructor stub
    mBitmap = bitmap;
  }

  public Bitmap getBitmap() {
    return mBitmap;
  }
}
