package android.graphics.drawable;

import android.graphics.Bitmap;

public class BitmapDrawable extends Drawable {
  private Bitmap mBitmap = null;
  private final String mDataUrl;

  public BitmapDrawable() {
    // TODO Auto-generated constructor stub
    mDataUrl = null;
  }

  public BitmapDrawable(Bitmap bitmap) {
    // TODO Auto-generated constructor stub
    mBitmap = bitmap;
    mDataUrl = null;
  }

  public BitmapDrawable(String dataUrl) {
    mDataUrl = dataUrl;
  }

  public Bitmap getBitmap() {
    return mBitmap;
  }

  public String getDataUrl() {
    return mDataUrl;
  }
}
