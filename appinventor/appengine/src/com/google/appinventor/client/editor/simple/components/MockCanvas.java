// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Mock Canvas component.
 *
 */
public final class MockCanvas extends MockContainer {

  /**
   * Component type name.
   */
  public static final String TYPE = "Canvas";

  // UI components
  private final AbsolutePanel canvasWidget;

  /**
   * Creates a new MockCanvas component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockCanvas(SimpleEditor editor) {
    super(editor, TYPE, images.canvas(), new MockCanvasLayout());

    rootPanel.setHeight("100%");

    canvasWidget = new AbsolutePanel();
    canvasWidget.setStylePrimaryName("ode-SimpleMockContainer");
    canvasWidget.add(rootPanel);

    initComponent(canvasWidget);
  }

  @Override
  protected boolean acceptableSource(DragSource source) {
    MockComponent component = null;
    if (source instanceof MockComponent) {
      component = (MockComponent) source;
    } else if (source instanceof SimplePaletteItem) {
      component = (MockComponent) source.getDragWidget();
    }
    if (component instanceof MockSprite) {
      return true;
    }
    return false;
  }

  /*
   * Sets the canvas's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetBackgroundColor(canvasWidget, text);
  }

  /**
   * Sets the canvas's BackgroundImage property to a new value.
   */
  private void setBackgroundImageProperty(String text) {
    String url = convertImagePropertyValueToUrl(text);

    // We tell the layout (which is a MockCanvasLayout) that there is (or is not) a background
    // image so it can adjust the "layout width/height". The "layout width/height" is used when the
    // preferred width/height of a MockContainer is requested. See MockContainer.getPreferredWidth
    // and getPreferredHeight, as well as MockLayout.getPreferredWidth and getPreferredHeight.
    if (url == null) {
      // text was not recognized as an asset.
      ((MockCanvasLayout) layout).setBackgroundImageUrl("");
      url = "images/canvas.png";
      // We set the background image of the canvasWidget so it displays the image. We do it inside
      // the if because we need to override the background-size property only for this case
      MockComponentsUtil.setWidgetBackgroundImage(this, canvasWidget, url);
      DOM.setStyleAttribute(canvasWidget.getElement(), "backgroundSize", "");
    } else {
      ((MockCanvasLayout) layout).setBackgroundImageUrl(url);
      // We set the background image of the canvasWidget so it displays the image.
      MockComponentsUtil.setWidgetBackgroundImage(this, canvasWidget, url);
    }
  }

  private static class MockSpriteWithCoordinates {
    final MockComponent mockComponent;
    final int left;
    final int top;

    MockSpriteWithCoordinates(MockComponent mockComponent, int left, int top) {
      this.mockComponent = mockComponent;
      this.left = left;
      this.top = top;
    }
  }

  /**
   * <p>Sorts the children of {@link #rootPanel} such that sprites are
   * earlier than ones that may be drawn over them.  Specifically, sprites
   * are sorted first on Z property, then on their order in the designer's
   * component tree.  The latter is to make the behavior the same as
   * before Z layers were added.</p>
   */
  private void sortSprites() {
    // Create a temporary set for the existing sprites.
    SortedSet<MockSpriteWithCoordinates> sprites =
        new TreeSet<MockSpriteWithCoordinates>(
            new Comparator<MockSpriteWithCoordinates>() {
              @Override
              public int compare(MockSpriteWithCoordinates s1,
                                 MockSpriteWithCoordinates s2) {
                double z1 = getZProperty(s1.mockComponent);
                double z2 = getZProperty(s2.mockComponent);
                if (z1 != z2) {
                  return (int) Math.signum(z1 - z2);
                } else {
                  // Both sprites have the same Z property value,
                  // so put first whichever one is earlier in the
                  // component tree.
                  return Integer.signum(children.indexOf(s1.mockComponent) -
                                        children.indexOf(s2.mockComponent));
                }
              }
            });

    // Remove all sprites from the container's list, transferring them to our
    // temporary set.
    while (rootPanel.getWidgetCount() > 0) {
      MockComponent sprite = (MockComponent) rootPanel.getWidget(0);
      sprites.add(new MockSpriteWithCoordinates(
          sprite,
          rootPanel.getWidgetLeft(sprite),
          rootPanel.getWidgetTop(sprite)));
      rootPanel.remove(sprite);
    }

    // Add them into the rootPanel in proper order.
    for (MockSpriteWithCoordinates sprite : sprites) {
      rootPanel.add(sprite.mockComponent, sprite.left, sprite.top);
    }
  }

  /**
   * <p>Reorders the children in {@link #rootPanel} so they are sorted by
   * Z layer value without changing their order in the component tree.
   * We assume that the list is already in the correct order except for
   * the changed sprite.</p>
   *
   * @param changedSprite the sprite whose Z layer value has changed; it
   *                      should already be a child of {@link #rootPanel}
   */
  void reorderComponents(MockSprite changedSprite) {
    // Since it has already been added to rootPanel, we do not need to pass it,
    // just to re-sort the list of rootPanel's children.
    sortSprites();

    // Redraw.
    refreshForm();
  }

  private static double getZProperty(MockComponent sprite) {
    try {
      return Double.parseDouble(
          sprite.getPropertyValue(MockSprite.PROPERTY_NAME_Z));
    } catch (NumberFormatException e) {
      return MockSprite.DEFAULT_Z_LAYER;
    }
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDIMAGE)) {
      setBackgroundImageProperty(newValue);
      refreshForm();
    }
  }
}
