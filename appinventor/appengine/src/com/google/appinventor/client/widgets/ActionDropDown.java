package com.google.appinventor.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 *  An Action Drop Down Button is a composite of a dropdown button and a textbutton.
 *  The Action button is a default action when clicked, while selecting the drop-down
 *  will allow the user to choose additional actions besides the dropdown.
 *
 *  This is not used yet.
 */
public class ActionDropDown extends Composite {

  public ActionDropDown(TextButton defaultAction, DropDownButton options){
    HorizontalPanel actiondropdown = new HorizontalPanel();
    defaultAction.setStyleName("ActionDropDown-Default");
    options.setStyleName("ActionDropDown-Options");
    options.setText("  \u25BE  ");
    actiondropdown.add(defaultAction);
    actiondropdown.add(options);
    initWidget(actiondropdown);

  }
}
