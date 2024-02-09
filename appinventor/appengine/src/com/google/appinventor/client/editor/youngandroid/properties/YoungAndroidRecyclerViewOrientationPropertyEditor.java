package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
/*

*/
public class YoungAndroidRecyclerViewOrientationPropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] orientation = new Choice[] {
    new Choice(MESSAGES.verticalOrientation(), "0"),
    new Choice(MESSAGES.horisontalOrientation(), "1")
//    new Choice(MESSAGES.gridOrientation(), "2")
  };

  public YoungAndroidRecyclerViewOrientationPropertyEditor() {
    super(orientation);
  }
}
