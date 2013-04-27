package com.google.appinventor.components.runtime;

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

/**
 * This class is used to display a Slider.
 * <p>The Slider is like a ProgressBar that adds a draggable thumb. You can touch the thumb and drag
 * left or right to set the slider thumb position. As Slider thumb is dragged, it will trigger
 * PositionChanged event, reporting the position of the Slider thumb. The Slider uses the following
 * default values. However these values can be changed through designer or block editor
 * <ul>
 * <li>MinValue</li>
 * <li>MaxValue</li>
 * <li>ThumbPosition</li>
 * </ul></p>
 *
 * @author M. Hossein Amerkashi
 */
@DesignerComponent(version = YaVersion.SLIDER_COMPONENT_VERSION,
    description = "A Slider is like a ProgressBar that adds a draggable thumb. You can touch " +
        "the thumb and drag left or right to set the slider thumb position. " +
        "As Slider thumb is dragged, it will trigger PositionChanged event, " +
        "reporting the position of the Slider thumb. The reported position of the " +
        "Slider thumb can be used to dynamically update another component " +
        "attribute, such as a TextBox font size or a Ball component radius.",
    category = ComponentCategory.BASIC)
@SimpleObject
public class Slider extends AndroidViewComponent implements SeekBar.OnSeekBarChangeListener {
  private final static String LOG_TAG = "Slider";
  private final SeekBar view;

  float sliderMinValue;
  float sliderMaxValue;

  float sliderThumbPosition;

  /**
   * Creates a new Slider component.
   *
   * @param container container that the component will be placed in
   */
  public Slider(ComponentContainer container) {
    super(container);
    view = new SeekBar(container.$context());

    // Adds the component to its designated container
    container.$add(this);

    // Default property values. Don't use MinValue() or MaxValue() or ThumbPosition()
    sliderMinValue = Component.SLIDER_MIN_VALUE;
    sliderMaxValue = Component.SLIDER_MAX_VALUE;
    sliderThumbPosition = Component.SLIDER_THUMB_VALUE;

    view.setOnSeekBarChangeListener(this);

    //NOTE: The boundaries for Seekbar are between 0-100 and there is no lower-limit that could
    // be set. We use math to calculate the lower / upper / thumb position.

    // keep the SeekBar effectively at [0-100] and calculate thumb position within that range
    view.setMax(100);

    //Based on given min, max & thumb position, determine where the thumb position would be
    // within normal SeekBar 0-100 range
    recalcThumbPosition(sliderMinValue, sliderMaxValue, sliderThumbPosition);

    Log.d(LOG_TAG, "Slider initial min, max, thumb values are: " +
        MinValue() + "/" + MaxValue() + "/" + ThumbPosition());
  }

  /**
   * Given min, max, thumb position, this method determines where the thumb position would be
   * within normal SeekBar 0-100 range
   *
   * @param min   value for slider
   * @param max   value for slider
   * @param thumb the slider thumb position
   */
  private void recalcThumbPosition(float min, float max, float thumb) {

    //Based on given min, max & thumb position, determine where it falls in range of 0-100
    float calc = ((thumb - min) / (max - min)) * 100;

    Log.d(LOG_TAG, "Trying to recalculate. min, max, thumb, recalculatedValue: "
        + min + "/" + max + "/" + thumb + "/" + calc);

    sliderThumbPosition = calc;

    //Now set the thumb position on progress bar
    view.setProgress((int) calc);

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
  @SimpleProperty(description = "Specifies the position of the slider thumb. " +
      "If this value is > MaxValue, then it will be set to same value as MaxValue. " +
      "If this value is < MinValue, then it will be set to same value as MinValue.",
      userVisible = true)
  public void ThumbPosition(float position) {
    sliderThumbPosition = position;

    Log.d(LOG_TAG, "ThumbPosition is set to: " + sliderThumbPosition);
    recalcThumbPosition(sliderMinValue, sliderMaxValue, sliderThumbPosition);
  }

  /**
   * Returns the position of slider thumb
   *
   * @return the slider thumb position
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns the position of slider thumb", userVisible = true)
  public float ThumbPosition() {
    return sliderThumbPosition;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = Component.SLIDER_MIN_VALUE + "")
  @SimpleProperty(description = "Specifies the min value of slider. If this value is > slider " +
      "ThumbPosition, then it will be set to same value as ThumbPosition.", userVisible = true)
  public void MinValue(float minValue) {
    Log.d(LOG_TAG, "Min value is set to: " + minValue);

    sliderMinValue = minValue;

    recalcThumbPosition(minValue, sliderMaxValue, sliderThumbPosition);
  }

  /**
   * Returns the value of slider min value.
   *
   * @return the value of slider min value.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns the value of slider min value.", userVisible = true)
  public float MinValue() {
    return sliderMinValue;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = Component.SLIDER_MAX_VALUE + "")
  @SimpleProperty(description = "Specifies the max value of slider. " +
      "If this value is < ThumbPosition, then it will be set to same value as ThumbPosition",
      userVisible = true)
  public void MaxValue(float maxValue) {
    Log.d(LOG_TAG, "Max value is set to: " + maxValue);

    sliderMaxValue = maxValue;

    recalcThumbPosition(sliderMinValue, maxValue, sliderThumbPosition);
  }

  /**
   * Returns the slider max value
   *
   * @return the slider max value
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns the slider max value.", userVisible = true)
  public float MaxValue() {
    return sliderMaxValue;
  }

  @Override
  public View getView() {
    return view;
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    //progress has been changed. Set the sliderThumbPosition and then trigger the event

    //Now convert this progress value (which is between 0-100), back to a value between the
    //range that user has set within min, max
    sliderThumbPosition = ((sliderMaxValue - sliderMinValue) * (float) progress / 100)
        + sliderMinValue;

    Log.d(LOG_TAG, "onProgressChanged progress value [0-100]: " + progress
        + ", reporting to user as: " + sliderThumbPosition);

    //Trigger the event, reporting this new value
    PositionChanged(sliderThumbPosition);
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
    //NOTE: overriding and removing the annotation, because we don't want to give user
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
    //NOTE: overriding and removing the annotation, because we don't want to give user
    //ability to change the slider height and don't want display this in our block editor
    container.setChildHeight(this, height);
  }

}
