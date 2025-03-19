// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.editor.simple.components.MockComponent;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Descriptor for components on the component palette panel.
 * This class is immutable.
 *
 */
public final class SimpleComponentDescriptor {

  // Component display name
  private final String name;

  // Help information to display for component
  private final String helpString;

  // Whether External Component
  private final boolean external;

  // Goto documentation category URL piece
  private final String categoryDocUrlString;

  // Link to external documentation
  private final String helpUrl;

  // Whether to show the component on the palette
  private final boolean showOnPalette;

  // Whether the component has a visual representation in the app's UI
  private final boolean nonVisible;

  // Factory to create new components represented by this descriptor
  private final ComponentFactory componentFactory;

  // Image to be shown in the palette panel
  private final Image image;

  // The version of the extension (meaning is defined by the extension author).
  private final int version;

  private final String versionName;

  private final String dateBuilt;

  private final String license;

  /**
   * Creates a new component descriptor.
   *
   * @param name  component display name
   */
  public SimpleComponentDescriptor(String name,
                                   int version,
                                   String versionName,
                                   String dateBuilt,
                                   String license,
                                   String helpString,
                                   String helpUrl,
                                   String categoryDocUrlString,
                                   boolean showOnPalette,
                                   boolean nonVisible,
                                   boolean external,
                                   ComponentFactory componentFactory) {
    this.name = name;
    this.version = version;
    this.versionName = versionName;
    this.dateBuilt = dateBuilt;
    this.license = license;
    this.helpString = helpString;
    this.helpUrl = helpUrl;
    this.categoryDocUrlString = categoryDocUrlString;
    this.showOnPalette = showOnPalette;
    this.nonVisible = nonVisible;
    this.external = external;
    this.image = componentFactory.getImage(name);
    this.componentFactory = componentFactory;
  }

  /**
   * Returns the display name of the component.
   *
   * @return component display name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the help string for the component.  For more detail, see
   * javadoc for
   * {@link com.google.appinventor.client.editor.simple.ComponentDatabase#getHelpString(String)}.
   *
   * @return helpful message about the component
   */
  public String getHelpString() {
    return helpString;
  }

  /**
   * Returns the help URL for the component.  For more detail, see javadoc for
   * {@link com.google.appinventor.client.editor.simple.ComponentDatabase#getHelpUrl(String)}.
   *
   * @return URL to external documentation provided for an extension
   */
  public String getHelpUrl() {
    return helpUrl;
  }

  /**
   * Returns the origin of the component
   * @return true if component is external
   */
  public boolean getExternal() {
    return external;
  }
  /**
   * Returns the categoryDocUrl string for the component.  For more detail, see
   * javadoc for
   * {@link com.google.appinventor.client.editor.simple.ComponentDatabase#getCategoryDocUrlString(String)}.
   *
   * @return helpful message about the component
   */
  public String getCategoryDocUrlString() {
    return categoryDocUrlString;
  }

  /**
   * Returns whether this component should be shown on the palette.  For more
   * detail, see javadoc for
   * {@link com.google.appinventor.client.editor.simple.ComponentDatabase#getHelpString(String)}.
   *
   * @return whether the component should be shown on the palette
   */
  public boolean getShowOnPalette() {
    return showOnPalette;
  }

  /**
   * Returns whether this component is visible in the app's UI.  For more
   * detail, see javadoc for
   * {@link com.google.appinventor.client.editor.simple.ComponentDatabase#getHelpString(String)}.
   *
   * @return whether the component is non-visible
   */
  public boolean getNonVisible() {
    return nonVisible;
  }

  /**
   * Returns an image for display on the component palette.
   *
   * @return  image for component
   */
  public Image getImage() {
    return image;
  }

  /**
   * Returns the version of the component, if any.
   *
   * @return  component version string
   */
  public int getVersion() {
    return version;
  }

  /**
   * Returns the custom version name of the component, if any.
   *
   * @return  component version name
   */
  public String getVersionName() {
    return versionName;
  }

  /**
   * Returns the date the component was built, if any.
   *
   * @return  ISO 8601 formated date the component was built
   */
  public String getDateBuilt() {
    return dateBuilt;
  }

  /**
   * Returns the path to the license file used by the component.
   *
   * @return path to license file of component
   */
  public String getLicense() {
    return license;
  }

  /**
   * Returns a draggable image for the component. Used when dragging a
   * component from the palette onto the root.
   *
   * @return  draggable widget for component
   */
  public Widget getDragWidget() {
    return componentFactory.createMockComponent(name, null);
  }

  /**
   * Instantiates the corresponding mock component.
   *
   * @return  mock component
   */
  public MockComponent createMockComponentFromPalette() {
    MockComponent mockComponent = componentFactory.createMockComponent(name, null);
    mockComponent.onCreateFromPalette();
    return mockComponent;
  }
}
