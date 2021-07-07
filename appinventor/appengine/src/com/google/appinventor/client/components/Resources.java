package com.google.appinventor.client.components;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Resources used by all components.
 */
public interface Resources extends ClientBundle {

  public static final Resources INSTANCE =  GWT.create(Resources.class);

  @Source({
    "com/google/appinventor/client/resources/theme.css",
    "com/google/appinventor/client/components/button.css"
  })
  ButtonStyle buttonStyle();

  public interface ButtonStyle extends CssResource {
    String base();

    String left();
    String center();
    String right();
    String none();

    String primary();
    String danger();
    String action();

    String raised();
  }

  @Source({
    "com/google/appinventor/client/resources/theme.css",
    "com/google/appinventor/client/components/dropdown.css"
  })
  DropdownStyle dropdownStyle();

  public interface DropdownStyle extends CssResource {
    String buttonIcon();

    String dropdown();
    
    String dropdownItem();
    String dropdownItemWithDivider();
  }
}
