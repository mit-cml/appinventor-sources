// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import com.google.appinventor.components.annotations.Asset;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.IsColor;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.IceCreamSandwichUtil;
import com.google.appinventor.components.runtime.util.KitkatUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;
import android.view.MotionEvent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.graphics.Shader;
import	android.graphics.drawable.BitmapDrawable;

import java.io.IOException;

/**
 * Underlying base class for click-based components, not directly accessible to Simple programmers.
 *
 */
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public abstract class ButtonBase extends AndroidViewComponent
    implements OnClickListener, OnFocusChangeListener, OnLongClickListener, View.OnTouchListener, AccessibleComponent {

  private static final String LOG_TAG = "ButtonBase";

  private final android.widget.Button view;

  // Constant for shape
  // 10px is the radius of the rounded corners.
  // 10px was chosen for esthetic reasons.
  private static final float ROUNDED_CORNERS_RADIUS = 10f;
  private static final float[] ROUNDED_CORNERS_ARRAY = new float[] { ROUNDED_CORNERS_RADIUS,
      ROUNDED_CORNERS_RADIUS, ROUNDED_CORNERS_RADIUS, ROUNDED_CORNERS_RADIUS,
      ROUNDED_CORNERS_RADIUS, ROUNDED_CORNERS_RADIUS, ROUNDED_CORNERS_RADIUS,
      ROUNDED_CORNERS_RADIUS };

  // Constant background color for buttons with a Shape other than default
  private static final int SHAPED_DEFAULT_BACKGROUND_COLOR = Color.LTGRAY;

  //Backing for background repeat mode
  private int backgroundRepeatMode;

  // Backing for text alignment
  private int textAlignment;

  // Backing for background color
  private int backgroundColor;

  // Backing for font typeface
  private int fontTypeface;

  // Backing for font bold
  private boolean bold;

  // Used for determining if visual feedback should be provided for buttons that have images
  private boolean showFeedback=true;

  // Backing for font italic
  private boolean italic;

  // Backing for text color
  private int textColor;

  // Backing for button shape
  private int shape;

  // Image path
  private String imagePath = "";

  // This is our handle on Android's nice 3-d default button.
  private Drawable defaultButtonDrawable;

  private Drawable myBackgroundDrawable = null;

  // This is our handle in Android's default button color states;
  private ColorStateList defaultColorStateList;

  // This is the Drawable corresponding to the Image property.
  // If an Image has never been set or if the most recent Image
  // could not be loaded, this is null.
  private Drawable backgroundImageDrawable;

  //Whether or not the button is in high contrast mode
  private boolean isHighContrast = false;

  //Whether or not the button is in big text mode
  private boolean isBigText = false;

  /**
   * The minimum width of a button for the current theme.
   *
   * We store this statically because it should be constant across all buttons in the app.
   */
  private static int defaultButtonMinWidth = 0;

  /**
   * The minimum height of a button for the current theme.
   *
   * We store this statically because it should be constant across all buttons in the app.
   */
  private static int defaultButtonMinHeight = 0;

  /**
   * Creates a new ButtonBase component.
   *
   * @param container  container, component will be placed in
   */
  public ButtonBase(ComponentContainer container) {
    super(container);
    view = new android.widget.Button(container.$context());

    // Save the default values in case the user wants them back later.
    defaultButtonDrawable = view.getBackground();
    defaultColorStateList = view.getTextColors();
    defaultButtonMinWidth = KitkatUtil.getMinWidth(view);
    defaultButtonMinHeight = KitkatUtil.getMinHeight(view);

    // Adds the component to its designated container
    container.$add(this);

    // Listen to clicks and focus changes
    view.setOnClickListener(this);
    view.setOnFocusChangeListener(this);
    view.setOnLongClickListener(this);
    view.setOnTouchListener(this);
    IceCreamSandwichUtil.setAllCaps(view, false);

    // Default property values
    TextAlignment(Component.ALIGNMENT_CENTER);
    // BackgroundColor and Image are dangerous properties:
    // Once either of them is set, the 3D bevel effect for the button is
    // irretrievable, except by reloading defaultButtonDrawable, defined above.
    BackgroundColor(Component.COLOR_DEFAULT);
    Image("");
    Enabled(true);
    fontTypeface = Component.TYPEFACE_DEFAULT;
    TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
    FontSize(Component.FONT_DEFAULT_SIZE);
    Text("");
    TextColor(Component.COLOR_DEFAULT);
    Shape(Component.BUTTON_SHAPE_DEFAULT);
    BackgroundRepeatMode(Component.BACKGROUND_REPEAT_NONE);
  }

  public void Initialize(){
    updateAppearance();
  }
    /**
     * If a custom background images is specified for the button, then it will lose the pressed
     * and disabled image effects; no visual feedback.
     * The approach below is to provide a visual feedback if and only if an image is assigned
     * to the button. In this situation, we overlay a gray background when pressed and
     * release when not-pressed.
     */
    @Override
    public boolean onTouch(View view, MotionEvent me) {
      //NOTE: We ALWAYS return false because we want to indicate that this listener has not
      //been consumed. Using this approach, other listeners (e.g. OnClick) can process as normal.
      if (me.getAction() == MotionEvent.ACTION_DOWN) {
        //button pressed, provide visual feedback AND return false
        if (ShowFeedback() && (AppInventorCompatActivity.isClassicMode() || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
          view.getBackground().setAlpha(70); // translucent
          view.invalidate();
        }
        TouchDown();
      } else if (me.getAction() == MotionEvent.ACTION_UP ||
              me.getAction() == MotionEvent.ACTION_CANCEL) {
        //button released, set button back to normal AND return false
        if (ShowFeedback() && (AppInventorCompatActivity.isClassicMode() || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
          view.getBackground().setAlpha(255); // opaque
          view.invalidate();
        }
        TouchUp();
      }

      return false;
    }

  @Override
  public View getView() {
    return view;
  }

  /**
   * Indicates that the `%type%` was pressed down.
   */
  @SimpleEvent(description = "Indicates that the %type% was pressed down.")
  public void TouchDown() {
    EventDispatcher.dispatchEvent(this, "TouchDown");
  }

  /**
   * Indicates that the `%type%` has been released.
   */
  @SimpleEvent(description = "Indicates that the %type% has been released.")
  public void TouchUp() {
    EventDispatcher.dispatchEvent(this, "TouchUp");
  }

  /**
   * Indicates the cursor moved over the `%type%` so it is now possible
   * to click it.
   */
  @SimpleEvent(description = "Indicates the cursor moved over the %type% so " +
      "it is now possible to click it.")
  public void GotFocus() {
    EventDispatcher.dispatchEvent(this, "GotFocus");
  }

  /**
   * Indicates the cursor moved away from the `%type%` so it is now no
   * longer possible to click it.
   */
  @SimpleEvent(description = "Indicates the cursor moved away from " +
      "the %type% so it is now no longer possible to click it.")
  public void LostFocus() {
    EventDispatcher.dispatchEvent(this, "LostFocus");
  }

   /**
   * Returns the repeat mode of the button background image.
   *
   * @return  one of {@link Component#BACKGROUND_REPEAT_MODE_NONE},
   *          {@link Component#BACKGROUND_REPEAT_MODE_XY},
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      userVisible = false)
  public int BackgroundRepeatMode() {
    return backgroundRepeatMode;
  }

  /**
   * Specifies the repeat mode for the button background image. This does not check that the argument is a legal    
   * value.
   *
   * @param shape one of {@link Component#BACKGROUND_REPEAT_MODE_NONE},
   *          {@link Component#BACKGROUND_REPEAT_MODE_XY},
   *  
   * @throws IllegalArgumentException if shape is not a legal value.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BACKGROUND_REPEAT_MODE,
      defaultValue = Component.BACKGROUND_REPEAT_NONE + "")
  @SimpleProperty(description = "Specified background image repeat mode (none, xy)"
      , userVisible = false)
  public void BackgroundRepeatMode(int mode) {
    this.backgroundRepeatMode = mode;
    updateAppearance();
  }



  /**
   * Returns the alignment of the `%type%`'s text: center, normal
   * (e.g., left-justified if text is written left to right), or
   * opposite (e.g., right-justified if text is written left to right).
   *
   * @return  one of {@link Component#ALIGNMENT_NORMAL},
   *          {@link Component#ALIGNMENT_CENTER} or
   *          {@link Component#ALIGNMENT_OPPOSITE}
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Left, center, or right.",
      userVisible = false)
  public int TextAlignment() {
    return textAlignment;
  }

  /**
   * Specifies the alignment of the `%type%`'s text. Valid values are:
   * `0` (normal; e.g., left-justified if text is written left to right),
   * `1` (center), or
   * `2` (opposite; e.g., right-justified if text is written left to right).
   *
   * @param alignment  one of {@link Component#ALIGNMENT_NORMAL},
   *                   {@link Component#ALIGNMENT_CENTER} or
   *                   {@link Component#ALIGNMENT_OPPOSITE}
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTALIGNMENT,
                    defaultValue = Component.ALIGNMENT_CENTER + "")
  @SimpleProperty(userVisible = false)
  public void TextAlignment(int alignment) {
    this.textAlignment = alignment;
    TextViewUtil.setAlignment(view, alignment, true);
  }

  /**
   * Returns the style of the `%type%`.
   *
   * @return  one of {@link Component#BUTTON_SHAPE_DEFAULT},
   *          {@link Component#BUTTON_SHAPE_ROUNDED},
   *          {@link Component#BUTTON_SHAPE_RECT} or
   *          {@link Component#BUTTON_SHAPE_OVAL}
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      userVisible = false)
  public int Shape() {
    return shape;
  }

  /**
   * Specifies the shape of the `%type%`. The valid values for this property are `0` (default),
   * `1` (rounded), `2` (rectangle), and `3` (oval). The `Shape` will not be visible if an
   * {@link #Image()} is used.
   *
   * @internaldoc
   * This does not check that the argument is a legal value.
   *
   * @param shape one of {@link Component#BUTTON_SHAPE_DEFAULT},
   *          {@link Component#BUTTON_SHAPE_ROUNDED},
   *          {@link Component#BUTTON_SHAPE_RECT} or
   *          {@link Component#BUTTON_SHAPE_OVAL}
   *
   * @throws IllegalArgumentException if shape is not a legal value.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BUTTON_SHAPE,
      defaultValue = Component.BUTTON_SHAPE_DEFAULT + "")
  @SimpleProperty(description = "Specifies the shape of the %type% (default, rounded," +
      " rectangular, oval). The shape will not be visible if an Image is being displayed.",
      userVisible = false)
  public void Shape(int shape) {
    this.shape = shape;
    updateAppearance();
  }

  /**
   * Returns the path of the `%type%`'s image.
   *
   * @return  the path of the button's image
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Image to display on button.")
  public String Image() {
    return imagePath;
  }

  /**
   * Specifies the path of the `%type%`'s image. If there is both an `Image` and a
   * {@link #BackgroundColor()} specified, only the `Image` will be visible.
   *
   * @internaldoc
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path  the path of the button's image
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty(description = "Specifies the path of the image of the %type%.  " +
      "If there is both an Image and a BackgroundColor, only the Image will be " +
      "visible.")
  public void Image(@Asset String path) {
    // If it's the same as on the prior call and the prior load was successful,
    // do nothing.
    if (path.equals(imagePath) && backgroundImageDrawable != null) {
      return;
    }

    imagePath = (path == null) ? "" : path;

    // Clear the prior background image.
    backgroundImageDrawable = null;
    // Load image from file.
    if (imagePath.length() > 0) {
      try {
        backgroundImageDrawable = MediaUtil.getBitmapDrawable(container.$form(), imagePath);
      } catch (IOException ioe) {
        // TODO(user): Maybe raise Form.ErrorOccurred.
        Log.e(LOG_TAG, "Unable to load " + imagePath);
        // Fall through with a value of null for backgroundImageDrawable.
      }
    }

    // Update the appearance based on the new value of backgroundImageDrawable.
    updateAppearance();
  }

  /**
   * Returns the `%type%`'s background color as an alpha-red-green-blue
   * integer.
   *
   * @return  background RGB color with alpha
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Returns the button's background color")
  @IsColor
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * Specifies the `%type%`'s background color as an alpha-red-green-blue
   * integer.  If an {@link #Image(String)} has been set, the color
   * change will not be visible until the {@link #Image(String)} is removed.
   *
   * @internaldoc
   * If the parameter is {@link Component#COLOR_DEFAULT}, the original beveling is restored.
   *
   * @param argb background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
                    defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
  @SimpleProperty(description = "Specifies the background color of the %type%. " +
      "The background color will not be visible if an Image is being displayed.")
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    updateAppearance();
  }

  // Update appearance based on values of backgroundImageDrawable, backgroundColor and shape.
  // Images take precedence over background colors.
  private void updateAppearance() {
    // If there is no background image,
    // the appearance depends solely on the background color and shape.
    if (backgroundImageDrawable == null) {
      if (shape == Component.BUTTON_SHAPE_DEFAULT) {
        if (backgroundColor == Component.COLOR_DEFAULT) {
          // If there is no background image and color is default,
          // restore original 3D bevel appearance.
          if (isHighContrast || container.$form().HighContrast()) {
            ViewUtil.setBackgroundDrawable(view, null);
            ViewUtil.setBackgroundDrawable(view, getSafeBackgroundDrawable());
            view.getBackground().setColorFilter(Component.COLOR_BLACK, PorterDuff.Mode.SRC_ATOP);
          }
          else {
            ViewUtil.setBackgroundDrawable(view, defaultButtonDrawable);
          }

        } else if (backgroundColor == Component.COLOR_NONE) {
          // Clear the background image.
          ViewUtil.setBackgroundDrawable(view, null);
          //Now we set again the default drawable
          ViewUtil.setBackgroundDrawable(view, getSafeBackgroundDrawable());
          view.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.CLEAR);
        } else {
          // Clear the background image.
          ViewUtil.setBackgroundDrawable(view, null);
          //Now we set again the default drawable
          ViewUtil.setBackgroundDrawable(view, getSafeBackgroundDrawable());
          //@Author NMD (Next Mobile Development) [nmdofficialhelp@gmail.com]
          view.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP);
        }
      } else {
        // If there is no background image and the shape is something other than default,
        // create a drawable with the appropriate shape and color.
        setShape();
      }
      TextViewUtil.setMinSize(view, defaultButtonMinWidth, defaultButtonMinHeight);
    } else {
      // If there is a background image
      ViewUtil.setBackgroundImage(view, backgroundImageDrawable);
      TextViewUtil.setMinSize(view, 0, 0);
      setBackgroundImageRepeatMode();
    }
  }

  private Drawable getSafeBackgroundDrawable() {
    if (myBackgroundDrawable == null) {
      Drawable.ConstantState state = defaultButtonDrawable.getConstantState();
      if (state != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
        try {
          myBackgroundDrawable = state.newDrawable().mutate();
        } catch (NullPointerException e) {
          // We see this on SDK 7, but given we can't easily test every version
          // this is meant as a safeguard.
          Log.e(LOG_TAG, "Unable to clone button drawable", e);
          myBackgroundDrawable = defaultButtonDrawable;
        }
      } else {
        // Since we can't make a copy of the default we'll just use it directly
        myBackgroundDrawable = defaultButtonDrawable;
      }
    }
    return myBackgroundDrawable;
  }

  private ColorStateList createRippleState () {

    int[][] states = new int[][] { new int[] { android.R.attr.state_enabled} };
    int enabled_color = defaultColorStateList.getColorForState(view.getDrawableState(), android.R.attr.state_enabled);
    int[] colors = new int[] { Color.argb(70, Color.red(enabled_color), Color.green(enabled_color),
            Color.blue(enabled_color)) };

    return new ColorStateList(states, colors);
  }

  // Throw IllegalArgumentException if backgroundRepeatMode has illegal value.
  private void setBackgroundImageRepeatMode() {
    BitmapDrawable bd = (BitmapDrawable) backgroundImageDrawable;
    switch(backgroundRepeatMode) {
      case 0:
        bd.setTileModeXY(null, null);
        break;
      case 1:
        bd.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  // Throw IllegalArgumentException if shape has illegal value.
  private void setShape() {
    ShapeDrawable drawable = new ShapeDrawable();

    // Set shape of drawable.
    switch (shape) {
      case Component.BUTTON_SHAPE_ROUNDED:
        drawable.setShape(new RoundRectShape(ROUNDED_CORNERS_ARRAY, null, null));
        break;
      case Component.BUTTON_SHAPE_RECT:
        drawable.setShape(new RectShape());
        break;
      case Component.BUTTON_SHAPE_OVAL:
        drawable.setShape(new OvalShape());
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Set drawable to the background of the button.
    if (!AppInventorCompatActivity.isClassicMode() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      ViewUtil.setBackgroundDrawable(view, new RippleDrawable(createRippleState(), drawable, drawable));
    } else {
      ViewUtil.setBackgroundDrawable(view, drawable);
    }

    if (backgroundColor == Component.COLOR_NONE) {
      view.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.CLEAR);
    }
    else if (backgroundColor == Component.COLOR_DEFAULT) {
      if (isHighContrast || container.$form().HighContrast()) {
        view.getBackground().setColorFilter(Component.COLOR_BLACK, PorterDuff.Mode.SRC_ATOP);
      } else {
        view.getBackground().setColorFilter(SHAPED_DEFAULT_BACKGROUND_COLOR, PorterDuff.Mode.SRC_ATOP);
      }
    }
    else {
      view.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP);
    }

    view.invalidate();
  }

  /**
   * If set, user can tap `%type%` to cause action.
   *
   * @return  {@code true} indicates enabled, {@code false} disabled
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description = "If set, user can tap %type% to cause action.")
  public boolean Enabled() {
    return TextViewUtil.isEnabled(view);
  }

  /**
   * Specifies whether the `%type%` should be active and clickable.
   *
   * @param enabled  {@code true} for enabled, {@code false} disabled
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    TextViewUtil.setEnabled(view, enabled);
  }

  /**
   * If set, the text of the `%type%` will attempt to use a bold font.
   * If bold has been requested, this property will return `true`{:.logic.block}, even if the
   * {@link #FontTypeface()} does not support bold.
   *
   * @return  {@code true} indicates bold, {@code false} normal
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "If set, %type% text is displayed in bold.")
  public boolean FontBold() {
    return bold;
  }

  /**
   * Specifies whether the text of the `%type%` should be bold.
   * Some fonts do not support bold.
   *
   * @param bold  {@code true} indicates bold, {@code false} normal
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public void FontBold(boolean bold) {
    this.bold = bold;
    TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
  }

  /**
   * Specifies if a visual feedback should be shown when a `%type%` with an assigned
   * {@link #Image()} is pressed.
   *
   * @param showFeedback  {@code true} enables showing feedback,
   *                 {@code false} disables it
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "True")
  @SimpleProperty(description = "Specifies if a visual feedback should be shown " +
          " for a %type% that has an image as background.")

  public void ShowFeedback(boolean showFeedback) {
    this.showFeedback =showFeedback;
  }

    /**
     * Returns true if the text of the `%type%` should be bold.
     * If bold has been requested, this property will return true, even if the
     * font does not support bold.
     *
     * @suppressdoc
     * @return {@code true} indicates visual feedback will be shown,
     *                 {@code false} visual feedback will not be shown
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Returns the visual feedback state of the %type%")
    public boolean ShowFeedback() {
        return showFeedback;
    }

    /**
   * If set, the text of the `%type%` will attempt to use an italic font.
   * If italic has been requested, this property will return `true`{:.logic.block}, even if the
   * font does not support italic.
   *
   * @return  {@code true} indicates italic, {@code false} normal
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "If set, %type% text is displayed in italics.")
  public boolean FontItalic() {
    return italic;
  }

  /**
   * Specifies whether the text of the `%type%` should be italic.
   * Some fonts do not support italic.
   *
   * @param italic  {@code true} indicates italic, {@code false} normal
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public void FontItalic(boolean italic) {
    this.italic = italic;
    TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
  }

  /**
   * The size of the font used for rendering the {@link #Text(String)}.
   *
   * @internaldoc
   * Returns the text font size of the `%type%`, measured in sp(scale-independent pixels).
   *
   * @return  font size in sp(scale-independent pixels).
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Point size for %type% text.")
  public float FontSize() {
    return TextViewUtil.getFontSize(view, container.$context());
  }

  /**
   * Specifies the text font size of the `%type%`, measured in sp(scale-independent pixels).
   *
   * @param size  font size in sp(scale-independent pixels)
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = Component.FONT_DEFAULT_SIZE + "")
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public void FontSize(float size) {
    if (size == FONT_DEFAULT_SIZE && container.$form().BigDefaultText()) {
      TextViewUtil.setFontSize(view, 24);
    } else {
      TextViewUtil.setFontSize(view, size);
    }
  }

  /**
   * The text font face of the `%type%`. Valid values are `0` (default), `1` (serif), `2` (sans
   * serif), or `3` (monospace).
   *
   * @return  one of {@link Component#TYPEFACE_DEFAULT},
   *          {@link Component#TYPEFACE_SERIF},
   *          {@link Component#TYPEFACE_SANSSERIF} or
   *          {@link Component#TYPEFACE_MONOSPACE}
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Font family for %type% text.",
      userVisible = false)
  public int FontTypeface() {
    return fontTypeface;
  }

  /**
   * Specifies the text font face of the `%type%` as default, serif, sans
   * serif, or monospace.
   *
   * @param typeface  one of {@link Component#TYPEFACE_DEFAULT},
   *                  {@link Component#TYPEFACE_SERIF},
   *                  {@link Component#TYPEFACE_SANSSERIF} or
   *                  {@link Component#TYPEFACE_MONOSPACE}
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE,
      defaultValue = Component.TYPEFACE_DEFAULT + "")
  @SimpleProperty(
      userVisible = false)
  public void FontTypeface(int typeface) {
    fontTypeface = typeface;
    TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
  }

  /**
   * Returns the text displayed by the `%type%`.
   *
   * @return  button caption
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Text to display on %type%.")
  public String Text() {
    return TextViewUtil.getText(view);
  }

  /**
   * Specifies the text displayed by the `%type%`.
   *
   * @param text  new caption for button
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void Text(String text) {
    TextViewUtil.setText(view, text);
  }

  /**
   * Returns the text color of the `%type%` as an alpha-red-green-blue
   * integer.
   *
   * @return  text RGB color with alpha
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Color for button text.")
  @IsColor
  public int TextColor() {
    return textColor;
  }

  /**
   * Specifies the text color of the `%type%` as an alpha-red-green-blue
   * integer.
   *
   * @param argb  text RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
  @SimpleProperty
  public void TextColor(int argb) {
    // TODO(user): I think there is a way of only setting the color for the enabled state
    textColor = argb;
    if (argb != Component.COLOR_DEFAULT) {
      TextViewUtil.setTextColor(view, argb);
    } else {
      if (isHighContrast || container.$form().HighContrast()){
        TextViewUtil.setTextColor(view, Color.WHITE);
      }
      else {
        TextViewUtil.setTextColors(view, defaultColorStateList);
      }
    }
  }

  public abstract void click();

  // Override this if your component actually will consume a long
  // click.  A 'false' returned from this function will cause a long
  // click to be interpreted as a click (and the click function will
  // be called).
  public boolean longClick() {
    return false;
  }

  // OnClickListener implementation

  @Override
  public void onClick(View view) {
    click();
  }

  // OnFocusChangeListener implementation

  @Override
  public void onFocusChange(View previouslyFocused, boolean gainFocus) {
    if (gainFocus) {
      GotFocus();
    } else {
      LostFocus();
    }
  }

  // OnLongClickListener implementation

  @Override
  public boolean onLongClick(View view) {
    return longClick();
  }

  @Override
  public void setHighContrast(boolean isHighContrast) {
    //background of button
    if (backgroundImageDrawable == null && shape == Component.BUTTON_SHAPE_DEFAULT && backgroundColor == Component.COLOR_DEFAULT) {
      if (isHighContrast) {
        ViewUtil.setBackgroundDrawable(view, null);
        ViewUtil.setBackgroundDrawable(view, getSafeBackgroundDrawable());
        view.getBackground().setColorFilter(Component.COLOR_BLACK, PorterDuff.Mode.SRC_ATOP);
      } else {
        ViewUtil.setBackgroundDrawable(view, defaultButtonDrawable);
      }
    }

    //color of text
    if (textColor == Component.COLOR_DEFAULT) {
      if (isHighContrast) {
        TextViewUtil.setTextColor(view, Color.WHITE);
      } else {
        TextViewUtil.setTextColors(view, defaultColorStateList);
      }
    }
  }

  @Override
  public boolean getHighContrast() {
    return isHighContrast;
  }

  @Override
  public void setLargeFont(boolean isLargeFont) {
    if (TextViewUtil.getFontSize(view, container.$context()) == 24.0 || TextViewUtil.getFontSize(view, container.$context()) == Component.FONT_DEFAULT_SIZE) {
      if (isLargeFont) {
        TextViewUtil.setFontSize(view, 24);
      } else {
        TextViewUtil.setFontSize(view, Component.FONT_DEFAULT_SIZE);
      }
    }
  }

  @Override
  public boolean getLargeFont() {
    return isBigText;
  }

}
