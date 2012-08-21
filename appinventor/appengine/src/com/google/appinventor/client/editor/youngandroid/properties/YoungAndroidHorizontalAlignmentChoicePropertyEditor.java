package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

// cludge for now.  mimic how this is done for button shape


/**
 * Property editor for button shape.
 *
 * @author hal@mit.edu (Hal Abelson)
 */
public class YoungAndroidHorizontalAlignmentChoicePropertyEditor extends ChoicePropertyEditor {

  // We need to get these constants as strings because we do a switch on these values
  // in MOckHVLayoutBase.  And you can't switch on strings in Java before Java 7.
  public static final String LEFT = ComponentConstants.GRAVITY_LEFT + "";
  public static final String RIGHT = ComponentConstants.GRAVITY_RIGHT + "";
  public static final String CENTER = ComponentConstants.GRAVITY_CENTER_HORIZONTAL + "";

  private static final Choice[] hAlignments = new Choice[] {
    new Choice(MESSAGES.horizontalAlignmentChoiceLeft(), LEFT),
    new Choice(MESSAGES.horizontalAlignmentChoiceCenter(), CENTER),
    new Choice(MESSAGES.horizontalAlignmentChoiceRight(), RIGHT)
  };

  public YoungAndroidHorizontalAlignmentChoicePropertyEditor() {
    super(hAlignments);
  }
}
