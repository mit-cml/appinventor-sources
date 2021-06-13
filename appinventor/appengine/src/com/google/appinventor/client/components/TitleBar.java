package com.google.appinventor.client.components;

import com.google.appinventor.client.Ode;

import com.google.gwt.user.client.ui.Composite;

public class TitleBar extends Composite {
  public TitleBar() {
    initWidget(Ode.getInstance().getTopPanel());
  }
}
