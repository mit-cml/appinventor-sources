package com.google.appinventor.client.explorer.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;

public class NoProjectDialogBox extends DialogBox {

private static NoProjectDialogBoxUiBinder uiBinder =
GWT.create(NoProjectDialogBoxUiBinder.class);

interface NoProjectDialogBoxUiBinder extends UiBinder<Widget, NoProjectDialogBox> {};

public NoProjectDialogBox() {
    this.setStylePrimaryName("ode-noDialogDiv");
    this.center();
    add(uiBinder.createAndBindUi(this));
  }
}
