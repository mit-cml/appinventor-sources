// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

/**
 * Categories for Simple properties. For properties marked with {@link DesignerProperty}, the
 * category will also be used to group related properties together.
 */
public enum PropertyCategory {
  // TODO(user): i18n category names
  /**
   * Category for properties that change component behavior, either in terms of the app developer
   * or the app end user. For example, Button's Enabled property, even though it changes the
   * appearance of the button, changes how the user interacts with the button, and therefore is a
   * Behavior property.
   */
  BEHAVIOR("Behavior"),

  /**
   * Category for properties that change component appearance. For example, the Notifier component's
   * BackgroundColor property is an Appearance property since it changes how the view will appear
   * to the user. While Appearance properties are more applicable to visible components, in some
   * cases non-visible components (like the Notifier) may take Appearance categories
   */
  APPEARANCE("Appearance"),

  /**
   * Category for properties that are deprecated and may be removed in a future release.
   */
  DEPRECATED("Deprecated"),

  /**
   * The unspecified category. The ComponentProcessor will now throw an error if a property does
   * not have another category specified.
   */
  UNSET("Unspecified"),

  /**
   * Category for properties that affect the application. Typically, application properties are
   * Screen1 properties. An example would be the AppName property, which controls the name of the
   * app displayed in the launcher and Settings app.
   */
  APPLICATION("Application"),

  /**
   * Category for advanced properties that won't need to be adjusted by most users. The Advanced
   * category is collapsed by default in the properties panel. An example property would be the
   * Token property of CloudDB, where App Inventor autopopulates the token and that should be
   * sufficient for most users.
   */
  ADVANCED("Advanced"),

  GENERAL("General"),

  THEMING("Theming"),

  PUBLISHING("Publishing");

  private final String name;

  PropertyCategory(String categoryName) {
    name = categoryName;
  }

  public String getName() {
    return name;
  }
}
