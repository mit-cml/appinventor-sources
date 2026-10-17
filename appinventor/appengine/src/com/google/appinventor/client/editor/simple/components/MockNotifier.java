package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;

/**
 * Designer support for the Notifier component.
 */
public class MockNotifier extends MockNonVisibleComponent {
  public static final String TYPE = "Notifier";

  public MockNotifier(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
  }

  @Override
  public Widget getPropertiesPanelExtension() {
    Button consoleButton = new Button("View Console Log");
    consoleButton.addStyleName("gwt-Button");
    consoleButton.getElement().setAttribute(
        "aria-label", "Show or hide the console log");
    consoleButton.getElement().getStyle().setProperty(
        "margin", "6px 4px 4px 4px");
    consoleButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Ode.getInstance().setConsoleVisible(
            !Ode.getInstance().isConsoleVisible());
      }
    });
    return consoleButton;
  }
}
