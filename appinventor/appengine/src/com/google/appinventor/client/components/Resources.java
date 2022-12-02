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
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/light.css",
    "com/google/appinventor/client/components/button.css"
  })
  ButtonStyle buttonStyleLight();

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/dark.css",
    "com/google/appinventor/client/components/button.css"
  })
  ButtonStyle buttonStyleDark();

  public interface ButtonStyle extends CssResource {
    String base();

    String left();
    String center();
    String right();
    String none();

    String primary();
    String danger();
    String inline();
    String action();

    String raised();
  }

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/light.css",
    "com/google/appinventor/client/components/dropdown.css"
  })
  DropdownStyle dropdownStyleLight();

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/dark.css",
    "com/google/appinventor/client/components/dropdown.css"
  })
  DropdownStyle dropdownStyleDark();

  public interface DropdownStyle extends CssResource {
    String buttonIcon();

    String dropdown();

    String dropdownItem();
    String dropdownItemWithDivider();
  }

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/light.css",
    "com/google/appinventor/client/components/titleBar.css"
  })
  TitleBarStyle titleBarStyleLight();

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/dark.css",
    "com/google/appinventor/client/components/titleBar.css"
  })
  TitleBarStyle titleBarStyleDark();

  public interface TitleBarStyle extends CssResource {
    String container();
    String logo();
  }

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/light.css",
    "com/google/appinventor/client/components/dialog.css"
  })
  DialogStyle dialogStyleLight();

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/dark.css",
    "com/google/appinventor/client/components/dialog.css"
  })
  DialogStyle dialogStyleDark();

  public interface DialogStyle extends CssResource {
    String dialog();
  }

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/light.css",
    "com/google/appinventor/client/components/folderTreeItem.css"
  })
  FolderTreeItemStyle folderTreeItemStyleLight();

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/dark.css",
    "com/google/appinventor/client/components/folderTreeItem.css"
  })
  FolderTreeItemStyle folderTreeItemStyleDark();

  public interface FolderTreeItemStyle extends CssResource {
    String item();
  }
}
