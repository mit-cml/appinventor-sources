package com.google.appinventor.components.runtime;

import android.R;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.SdkLevel;

/**
 * This class is used to display a Slider.
 * <p>The Slider is a progress bar that adds a draggable thumb. You can touch the thumb and drag
 * left or right to set the slider thumb position. As Slider thumb is dragged, it will trigger
 * PositionChanged event, reporting the position of the Slider thumb. The Slider uses the following
 * default values. However these values can be changed through designer or block editor
 * <ul>
 * <li>MinValue</li>
 * <li>MaxValue</li>
 * <li>ThumbPosition</li>
 * </ul></p>
 *
 * @author kashi01@gmail.com (M. Hossein Amerkashi)
 * @author hal@mit.edu (Hal Abelson)
 */
@DesignerComponent(version = YaVersion.SLIDER_COMPONENT_VERSION,
    description = "A Slider is a progress bar that adds a draggable thumb. You can touch " +
        "the thumb and drag left or right to set the slider thumb position. " +
        "As the Slider thumb is dragged, it will trigger the PositionChanged event, " +
        "reporting the position of the Slider thumb. The reported position of the " +
        "Slider thumb can be used to dynamically update another component " +
        "attribute, such as the font size of a TextBox or the radius of a Ball.",
    category = ComponentCategory.USERINTERFACE)
@SimpleObject
public class Slider extends AndroidViewComponent implements SeekBar.OnSeekBarChangeListener {
  private final static String LOG_TAG = "Slider";
  private static final boolean DEBUG = false;

  private final SeekBar seekbar;

  // slider mix, max, and thumb positions
  private float minValue;
  private float maxValue;
  // thumbPosition is a number between minValue and maxValue
  private float thumbPosition;
  private boolean thumbEnabled;

  // the total slider width
  private LayerDrawable fullBar;
  // the part of the slider to the left of the thumb
  private ClipDrawable beforeThumb;

  // colors of the bar after and before the thumb position
  private int rightColor;
  private int leftColor;

  private final static int initialRightColor = Component.COLOR_GRAY;
  private final static String initialRightColorString = Component.DEFAULT_VALUE_COLOR_GRAY;
  private final static int initialLeftColor = Component.COLOR_ORANGE;
  private final static String initialLeftColorString = Component.DEFAULT_VALUE_COLOR_ORANGE;

  // seekbar.getThumb was introduced in API level 16 and the component warns the user
  // that apps using Sliders won't work if the API level is below 16.  But for very old systems the
  // app won't even *load* because the verifier will reject getThumb.  I don't know how old - the
  // rejection happens on Donut but not on Gingerbread.
  // The purpose of SeekBarHelper class is to avoid getting rejected by the Android verifier when the
  // Slider component code is loaded into a device with API level less than Gingerbread.
  // We do this trick by putting the use of getThumb in the class SeekBarHelper and arranging for
  // the class to be compiled only if the API level is at least Gingerbread.  This same trick is
  // used in implementing the Sound component.
  private class SeekBarHelper {
    public void getThumb(int alpha) {
      seekbar.getThumb().mutate().setAlpha(alpha);
    }
  }

  public final boolean referenceGetThumb = (SdkLevel.getLevel() >= SdkLevel.LEVEL_JELLYBEAN);

  /**
   * Creates a new Slider component.
   *
   * @param container container that the component will be placed in
   */
  public Slider(ComponentContainer container) {
    super(container);
    seekbar = new SeekBar(container.$context());

    fullBar = (LayerDrawable) seekbar.getProgressDrawable();
    beforeThumb = (ClipDrawable) fullBar.findDrawableByLayerId(R.id.progress);
    leftColor = initialLeftColor;
    rightColor = initialRightColor;
    setSliderColors();

    // Adds the component to its designated container
    container.$add(this);

    // Initial property values
    minValue = Component.SLIDER_MIN_VALUE;
    maxValue = Component.SLIDER_MAX_VALUE;
    thumbPosition = Component.SLIDER_THUMB_VALUE;
    thumbEnabled = true;

    seekbar.setOnSeekBarChangeListener(this);

    //NOTE(kashi01): The boundaries for Seekbar are between 0-100 and there is no lower-limit that could
    // be set. We keep the SeekBar effectively at [0-100] and calculate thumb position within that
    // range.
    seekbar.setMax(100);

    // Based on given minValue, maxValue, and thumbPosition, determine where the seekbar
    // thumb position would be within normal SeekBar 0-100 range
    // !!! check this.  maybe don't want to pass the args???
    setSeekbarPosition();

    if (DEBUG) {
      Log.d(LOG_TAG, "Slider initial min, max, thumb values are: " +
          MinValue() + "/" + MaxValue() + "/" + ThumbPosition());
    }

    if (DEBUG) {
      Log.d(LOG_TAG, "API level is " + SdkLevel.getLevel());
    }

  }

  // NOTE(hal): On old phones, up through 2.2.2 and maybe higher, the color of the bar doesn't
  // change until the thumb is moved.  I'm ignoring that problem.
  private void setSliderColors() {
   fullBar.setColorFilter(rightColor,PorterDuff.Mode.SRC);
   beforeThumb.setColorFilter(leftColor, PorterDuff.Mode.SRC);
 }

  /**
   * Given min, max, thumb position, this method determines where the thumb position would be
   * within normal SeekBar 0-100 range
   *
   * @param min   value for slider
   * @param max   value for slider
   * @param thumb the slider thumb position
   */

 // Set the seekbar position based on minValue, maxValue, and thumbPosition
 // seekbar position is an integer in the range [0,100] and is determined by MinValue,
 // MaxValue and ThumbPosition
 private void setSeekbarPosition() {
    float seekbarPosition = ((thumbPosition - minValue) / (maxValue - minValue)) * 100;

    if (DEBUG) {
      Log.d(LOG_TAG, "Trying to recalculate seekbar position "
        + minValue + "/" + maxValue + "/" + thumbPosition + "/" + seekbarPosition);
    }

    // Set the thumb position on the seekbar
    seekbar.setProgress((int) seekbarPosition);
  }

  /**
   * Sets whether or not the slider thumb should be shown
   *
   * @param enabled Whether or not the slider thumb should be shown
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    defaultValue = "True")
  @SimpleProperty(description = "Sets whether or not to display the slider thumb.",
     userVisible = true)
  public void ThumbEnabled(boolean enabled) {
    thumbEnabled = enabled;
    int alpha = thumbEnabled ? 255 : 0;
    if (referenceGetThumb) {
      new SeekBarHelper().getThumb(alpha);
    }
    seekbar.setEnabled(thumbEnabled);

  }

  /**
   * Whether or not the slider thumb is being be shown
   *
   * @return Whether or not the slider thumb is being be shown
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns whether or not the slider thumb is being be shown",
      userVisible = true)
  public boolean ThumbEnabled() {
    return thumbEnabled;
  }

  /**
   * Sets the slider thumb position.
   *
   * @param position the position of the slider thumb. This value should be between
   *                 sliderMinValue and sliderMaxValue. If this value is not within the min and
   *                 max, then it will be calculated.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = Component.SLIDER_THUMB_VALUE + "")
  @SimpleProperty(description = "Sets the position of the slider thumb. " +
      "If this value is greater than MaxValue, then it will be set to same value as MaxValue. " +
      "If this value is less than MinValue, then it will be set to same value as MinValue.",
      userVisible = true)
  public void ThumbPosition(float position) {
    // constrain thumbPosition between minValue and maxValue
    thumbPosition = Math.max(Math.min(position, maxValue), minValue);
    if (DEBUG) {
      Log.d(LOG_TAG, "ThumbPosition is set to: " + thumbPosition);}
    setSeekbarPosition();
    PositionChanged(thumbPosition);
  }

  /**
   * Returns the position of slider thumb
   *
   * @return the slider thumb position
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns the position of slider thumb", userVisible = true)
  public float ThumbPosition() {
    return thumbPosition;
  }


  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = Component.SLIDER_MIN_VALUE + "")
  @SimpleProperty(description = "Sets the minimum value of slider.  Changing the minimum value " +
      "also resets Thumbposition to be halfway between the (new) minimum and the maximum. " +
      "If the new minimum is greater than the current maximum, then minimum and maximum will " +
      "both be set to this value.  Setting MinValue resets the thumb position to halfway " +
      "between MinValue and MaxValue and signals the PositionChanged event.",
      userVisible = true)
  public void MinValue(float value) {
    minValue = value;
    // increase maxValue if necessary to accommodate the new minimum
    maxValue = Math.max(value, maxValue);

    if (DEBUG) {
      Log.d(LOG_TAG, "Min value is set to: " + value);
    }
    ThumbPosition ((minValue + maxValue) / 2.0f);
  }


  /**
   * Returns the value of slider min value.
   *
   * @return the value of slider min value.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns the value of slider min value.", userVisible = true)
  public float MinValue() {
    return minValue;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = Component.SLIDER_MAX_VALUE + "")
  @SimpleProperty(description = "Sets the maximum value of slider.  Changing the maximum value " +
      "also resets Thumbposition to be halfway between the minimum and the (new) maximum. " +
      "If the new maximum is less than the current minimum, then minimum and maximum will both " +
      "be set to this value.  Setting MaxValue resets the thumb position to halfway " +
      "between MinValue and MaxValue and signals the PositionChanged event.",
      userVisible = true)
  public void MaxValue(float value) {
    maxValue = value;
    minValue = Math.min(value, minValue);

    if (DEBUG) {
     Log.d (LOG_TAG, "Max value is set to: " + value);
    }
    ThumbPosition ((minValue + maxValue) / 2.0f);
  }

  /**
   * Returns the slider max value
   *
   * @return the slider max value
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns the slider max value.", userVisible = true)
  public float MaxValue() {
    return maxValue;
  }


  /**
   * Returns the color of the slider bar to the left of the thumb, as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @return left color in the format 0xAARRGGBB, which includes
   * alpha, red, green, and blue components
   */
  @SimpleProperty(
      description = "The color of slider to the left of the thumb.",
      category = PropertyCategory.APPEARANCE)
  public int ColorLeft() {
    return leftColor;
  }

  /**
   * Specifies the color of the slider bar to the left of the thumb as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @param argb background color in the format 0xAARRGGBB, which
   * includes alpha, red, green, and blue components
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = initialLeftColorString)
  @SimpleProperty
  public void ColorLeft(int argb) {
    leftColor = argb;
    setSliderColors();
  }

  /**
   * Returns the color of the slider bar to the right of the thumb, as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @return right color in the format 0xAARRGGBB, which includes
   * alpha, red, green, and blue components
   */
  @SimpleProperty(
      description = "The color of slider to the left of the thumb.",
      category = PropertyCategory.APPEARANCE)
  public int ColorRight() {
    return rightColor;
  }

  /**
   * Specifies the color of the slider bar to the right of the thumb as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @param argb background color in the format 0xAARRGGBB, which
   * includes alpha, red, green, and blue components
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = initialRightColorString)
  @SimpleProperty
  public void ColorRight(int argb) {
    rightColor = argb;
    setSliderColors();
  }

  @Override
  public View getView() {
    return seekbar;
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    //progress has been changed. Set the sliderThumbPosition and then trigger the event

    //Now convert this progress value (which is between 0-100), back to a value between the
    //range that user has set within minValue, maxValue
    thumbPosition = ((maxValue - minValue) * (float) progress / 100)
        + minValue;

    if (DEBUG) {
    Log.d(LOG_TAG, "onProgressChanged progress value [0-100]: " + progress
        + ", reporting to user as: " + thumbPosition);
    }

    //Trigger the event, reporting this new value
    PositionChanged(thumbPosition);
  }

  /**
   * Indicates that position of the slider thumb has changed.
   */
  @SimpleEvent
  public void PositionChanged(float thumbPosition) {
    EventDispatcher.dispatchEvent(this, "PositionChanged", thumbPosition);
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    // TODO Auto-generated method stub
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    // TODO Auto-generated method stub
  }

  /**
   * Returns the component's vertical height, measured in pixels.
   *
   * @return height in pixels
   */
  @Override
  public int Height() {
    //NOTE(kashi01): overriding and removing the annotation, because we don't want to give user
    //ability to change the slider height and don't want display this in our block editor
    return getView().getHeight();
  }

  /**
   * Specifies the component's vertical height, measured in pixels.
   *
   * @param height in pixels
   */
  @Override
  public void Height(int height) {
    //NOTE(kashi01): overriding and removing the annotation, because we don't want to give user
    //ability to change the slider height and don't want display this in our block editor
    container.setChildHeight(this, height);
  }

}
