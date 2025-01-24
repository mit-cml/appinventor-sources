package android.view;

import android.graphics.PixelFormat;
import android.os.Parcelable;

public interface WindowManager extends ViewManager {
  public static class LayoutParams extends ViewGroup.LayoutParams {
    /**
     * X position for this window.  With the default gravity it is ignored.
     * When using {@link Gravity#LEFT} or {@link Gravity#START} or {@link Gravity#RIGHT} or
     * {@link Gravity#END} it provides an offset from the given edge.
     */
    public int x;

    /**
     * Y position for this window.  With the default gravity it is ignored.
     * When using {@link Gravity#TOP} or {@link Gravity#BOTTOM} it provides
     * an offset from the given edge.
     */
    public int y;

    /**
     * Indicates how much of the extra space will be allocated horizontally
     * to the view associated with these LayoutParams. Specify 0 if the view
     * should not be stretched. Otherwise the extra pixels will be pro-rated
     * among all views whose weight is greater than 0.
     */
    public float horizontalWeight;

    /**
     * Indicates how much of the extra space will be allocated vertically
     * to the view associated with these LayoutParams. Specify 0 if the view
     * should not be stretched. Otherwise the extra pixels will be pro-rated
     * among all views whose weight is greater than 0.
     */
    public float verticalWeight;


    /**
     * Window flag: hide all screen decorations (such as the status bar) while
     * this window is displayed.  This allows the window to use the entire
     * display space for itself -- the status bar will be hidden when
     * an app window with this flag set is on the top layer. A fullscreen window
     * will ignore a value of {@link #SOFT_INPUT_ADJUST_RESIZE} for the window's
     * {@link #softInputMode} field; the window will stay fullscreen
     * and will not resize.
     *
     * <p>This flag can be controlled in your theme through the
     * {@link android.R.attr#windowFullscreen} attribute; this attribute
     * is automatically set for you in the standard fullscreen themes
     * such as {@link android.R.style#Theme_NoTitleBar_Fullscreen},
     * {@link android.R.style#Theme_Black_NoTitleBar_Fullscreen},
     * {@link android.R.style#Theme_Light_NoTitleBar_Fullscreen},
     * {@link android.R.style#Theme_Holo_NoActionBar_Fullscreen},
     * {@link android.R.style#Theme_Holo_Light_NoActionBar_Fullscreen},
     * {@link android.R.style#Theme_DeviceDefault_NoActionBar_Fullscreen}, and
     * {@link android.R.style#Theme_DeviceDefault_Light_NoActionBar_Fullscreen}.</p>
     *
     * @deprecated Use {@link WindowInsetsController#hide(int)} with {@link WindowInsets.Type#statusBars()}
     * instead.
     */
    @Deprecated
    public static final int FLAG_FULLSCREEN      = 0x00000400;

    /**
     * Window flag: override {@link #FLAG_FULLSCREEN} and force the
     * screen decorations (such as the status bar) to be shown.
     *
     * @deprecated This value became API "by accident", and shouldn't be used by 3rd party
     * applications.
     */
    @Deprecated
    public static final int FLAG_FORCE_NOT_FULLSCREEN   = 0x00000800;

    /**
     * Window type: a normal application window.  The {@link #token} must be
     * an Activity token identifying who the window belongs to.
     * In multiuser systems shows only on the owning user's window.
     */
    public static final int TYPE_APPLICATION        = 2;

    public static final int SOFT_INPUT_STATE_UNSPECIFIED = 0;

    public static final int SOFT_INPUT_STATE_ALWAYS_HIDDEN = 3;

    public static final int SOFT_INPUT_ADJUST_UNSPECIFIED = 0x00;

    public static final int SOFT_INPUT_ADJUST_RESIZE = 0x10;

    /**
     * The desired bitmap format.  May be one of the constants in
     * {@link android.graphics.PixelFormat}. The choice of format
     * might be overridden by {@link #setColorMode(int)}. Default is OPAQUE.
     */
    public int format;

    public int type;

    public int flags;

    public int softInputMode;

    public LayoutParams() {
      super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      type = TYPE_APPLICATION;
      format = PixelFormat.OPAQUE;
    }

    public LayoutParams(int _type) {
      super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      type = _type;
      format = PixelFormat.OPAQUE;
    }

    public LayoutParams(int _type, int _flags) {
      super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      type = _type;
      flags = _flags;
      format = PixelFormat.OPAQUE;
    }

    public LayoutParams(int _type, int _flags, int _format) {
      super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      type = _type;
      flags = _flags;
      format = _format;
    }

    public LayoutParams(int w, int h, int _type, int _flags, int _format) {
      super(w, h);
      type = _type;
      flags = _flags;
      format = _format;
    }

    public LayoutParams(int w, int h, int xpos, int ypos, int _type,
        int _flags, int _format) {
      super(w, h);
      x = xpos;
      y = ypos;
      type = _type;
      flags = _flags;
      format = _format;
    }
  }
}
