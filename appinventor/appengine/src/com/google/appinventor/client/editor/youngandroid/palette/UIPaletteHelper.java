package com.google.appinventor.client.editor.youngandroid.palette;

import java.util.ArrayList;
import java.util.List;

/**
 * The palette helper for the User Interface component category.
 *
 * @author ajcolter@mit.edu (Aubrey Colter)
 */
public class UIPaletteHelper extends OrderedPaletteHelper {

  private static final List<String> uiComponentNames = new ArrayList<String>();
  static {
    //Most common items
    uiComponentNames.add("Button");
    uiComponentNames.add("Label");
    uiComponentNames.add("Image");
    //Boxes
    uiComponentNames.add("TextBox");
    uiComponentNames.add("PasswordTextBox");
    uiComponentNames.add("CheckBox");
    //Lists and Pickers
    uiComponentNames.add("ListView");
    uiComponentNames.add("ListPicker");
    uiComponentNames.add("DatePicker");
    uiComponentNames.add("TimePicker");
    uiComponentNames.add("Slider");
    uiComponentNames.add("Spinner");
    //Notifier
    uiComponentNames.add("Notifier");
    //Webview
    uiComponentNames.add("WebViewer");
  }

  UIPaletteHelper() {
    super(uiComponentNames);
  }
}
