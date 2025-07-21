package android.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;

public class ImageView extends View {

  private ScaleType mScaleType;
  private boolean mAdjustViewBounds = false;

  /**
   * Options for scaling the bounds of an image to the bounds of this view.
   */
  public enum ScaleType {
    /**
     * Scale using the image matrix when drawing. The image matrix can be set using
     * {@link ImageView#setImageMatrix(Matrix)}. From XML, use this syntax:
     * <code>android:scaleType="matrix"</code>.
     */
    MATRIX      (0),
    /**
     * Scale the image using {@link Matrix.ScaleToFit#FILL}.
     * From XML, use this syntax: <code>android:scaleType="fitXY"</code>.
     */
    FIT_XY      (1),
    /**
     * Scale the image using {@link Matrix.ScaleToFit#START}.
     * From XML, use this syntax: <code>android:scaleType="fitStart"</code>.
     */
    FIT_START   (2),
    /**
     * Scale the image using {@link Matrix.ScaleToFit#CENTER}.
     * From XML, use this syntax:
     * <code>android:scaleType="fitCenter"</code>.
     */
    FIT_CENTER  (3),
    /**
     * Scale the image using {@link Matrix.ScaleToFit#END}.
     * From XML, use this syntax: <code>android:scaleType="fitEnd"</code>.
     */
    FIT_END     (4),
    /**
     * Center the image in the view, but perform no scaling.
     * From XML, use this syntax: <code>android:scaleType="center"</code>.
     */
    CENTER      (5),
    /**
     * Scale the image uniformly (maintain the image's aspect ratio) so
     * that both dimensions (width and height) of the image will be equal
     * to or larger than the corresponding dimension of the view
     * (minus padding). The image is then centered in the view.
     * From XML, use this syntax: <code>android:scaleType="centerCrop"</code>.
     */
    CENTER_CROP (6),
    /**
     * Scale the image uniformly (maintain the image's aspect ratio) so
     * that both dimensions (width and height) of the image will be equal
     * to or less than the corresponding dimension of the view
     * (minus padding). The image is then centered in the view.
     * From XML, use this syntax: <code>android:scaleType="centerInside"</code>.
     */
    CENTER_INSIDE (7);

    ScaleType(int ni) {
      nativeInt = ni;
    }
    final int nativeInt;
  }


  public ImageView(Context context) {
    super(DOM.createImg());
  }

  public ImageView(Element element) {
    super(element);
  }

  public void setImageResource(int imageId) {
    element.setAttribute("src", "img/" + Context.resources.getDrawable(imageId));
  }

  public void setImageDrawable(Drawable drawable) {
    if (drawable == null) {
      element.removeAttribute("src");
    } else if (drawable instanceof BitmapDrawable) {
      nativeSetImageDrawable(((BitmapDrawable) drawable).getDataUrl());
    } else {
      throw new IllegalArgumentException(
          "ImageView only supports BitmapDrawable for now.");
    }
    requestLayout();
  }

  private native void nativeSetImageDrawable(String dataUrl) /*-{
    this.@android.view.View::element.src = dataUrl;
  }-*/;

  /**
   * Controls how the image should be resized or moved to match the size
   * of this ImageView.
   *
   * @param scaleType The desired scaling mode.
   *
   * @attr ref android.R.styleable#ImageView_scaleType
   */
  public void setScaleType(ScaleType scaleType) {
    if (scaleType == null) {
      throw new NullPointerException();
    }

    if (mScaleType != scaleType) {
      mScaleType = scaleType;

      requestLayout();
      invalidate();
    }
  }

  public boolean verifyDrawable(Drawable dr) {
    return true;
  }

  public void setContentDescription(CharSequence contentDescription) {
    // Set the content description for accessibility purposes
    element.setAttribute("alt", contentDescription.toString());
  }

  public boolean getAdjustViewBounds() {
    return mAdjustViewBounds;
  }

  public void setAdjustViewBounds(boolean adjustViewBounds) {
    mAdjustViewBounds = adjustViewBounds;
    if (adjustViewBounds) {
      setScaleType(ScaleType.FIT_CENTER);
    }
  }
}
