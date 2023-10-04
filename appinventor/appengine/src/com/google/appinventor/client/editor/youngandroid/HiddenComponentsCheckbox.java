package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;

import java.util.logging.Logger;

public class HiddenComponentsCheckbox extends CheckBox {
  private static final Logger LOG = Logger.getLogger(HiddenComponentsCheckbox.class.getName());

  private static HiddenComponentsCheckbox INSTANCE;
  private MockForm form;

  // This is an attempt to create a Singleton class that does not have to be referenced
  // by Ode and thus can be placed anywhere in a UIBinder layout.
  private HiddenComponentsCheckbox() {
    if (INSTANCE != null) {
      ErrorReporter.reportError("Attempted to create second instance of HiddenComponentsCheckbox singleton");
    } else {
      addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          toggleHiddenComponents(event);
        }
      });
      INSTANCE = this;
    }
  }

  public static HiddenComponentsCheckbox getCheckbox() {
    if (INSTANCE == null) {
      INSTANCE = new HiddenComponentsCheckbox();
    }
    return INSTANCE;
  }

  public void show(MockForm form) {
    this.form = form;
    setValue(Ode.getCurrentProjectEditor().getScreenCheckboxState(form.getTitle()));
  }

  // TODO: This should not require navigating through Ode
  void toggleHiddenComponents(ClickEvent e) {
    LOG.info("Received checkbox click");
    if (form != null) {
      LOG.info("Updating hidden components");
      Ode.getCurrentProjectEditor().setScreenCheckboxState(form.getTitle(), getValue());
      form.doRefresh();
    }
  }
}
