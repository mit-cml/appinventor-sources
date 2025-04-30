// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark components for use in the Designer and Blocks Editor.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DesignerComponent {
  /**
   * Category within designer.
   */
  ComponentCategory category() default ComponentCategory.UNINITIALIZED;

  /**
   * If non-empty, description to use in user-level documentation on a static
   * web page in place of Javadoc, which is meant for developers.  This may
   * contain unescaped HTML.
   */
  String description() default "";

  /**
   * Description to be shown on user request in the Designer.  If this field is
   * empty, the description() field should be used.  This may contain HTML.
   * Internal double-quotes will be converted to single-quotes when this field
   * is displayed in the designer.
   */
  // TODO(user): Add more robust character escaping.
  String designerHelpDescription() default "";

  /**
   * If False, don't show this component on the palette.  This was added to
   * support the Form/Screen component.
   */
  boolean showOnPalette() default true;

  /**
   * If true, component is "non-visible" in the UI; that is, it doesn't need
   * any special handling in the Designer and can be represented by a
   * {@link com.google.appinventor.client.editor.simple.components.MockNonVisibleComponent}.
   */
  boolean nonVisible() default false;

  /**
   * The file name of the icon that represents the component in the palette.
   * This should be just the last part of the path name for the file. We'll
   * look for the file in "com/google/appinventor/images/" for
   * statically loaded resources, or in "war/images/" for dynamically loaded
   * components
   *
   * @return The name of the icon file
   */
  String iconName() default "";

  /**
   * The version of the component.
   */
  // Constants for all component version numbers must be defined in
  // com.google.appinventor.components.common.YaVersion, and specified when the DesignerComponent
  // annotation is used.
  int version(); // There is no default value.

  /**
   * Custom help URL for the component (used for extensions).
   */
  String helpUrl() default "";

  /**
   * The minimum SDK version required for the component. Defaults to
   * the global App Inventor minimum SDK unless otherwise specified.
   */
  int androidMinSdk() default ComponentConstants.APP_INVENTOR_MIN_SDK;

  /**
   * A custom version name for the component version. If provided, it
   * will be shown in the component help popup in place of the
   * {@link #version()}. This can be useful for marking beta or release
   * candidate versions of extensions, for example.
   * @return The custom version name, if any.
   */
  String versionName() default "";

  /**
   * A ISO 8601 datetime string that indicates when the component was
   * built. This information will be shown in the component help popup
   * for extensions. This is automatically populated by
   * {@link com.google.appinventor.components.scripts.ExternalComponentGenerator}.
   * @return An ISO 8601 string containing the compilation time of
   * the component.
   */
  String dateBuilt() default "";

  /**
   * The file name of the LICENSE file that the component is attributed under.
   * Meant primarily for use by external components which can have a license
   * different from that of this codebase. This string can also be a URL pointing
   * to an external LICENSE file.
   *
   * @return The name of the LICENSE file
   */
  String licenseName() default "";
}
