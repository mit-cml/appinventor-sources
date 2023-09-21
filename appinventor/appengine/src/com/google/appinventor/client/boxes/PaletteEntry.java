package com.google.appinventor.client.boxes;
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.MockBall;
import com.google.appinventor.client.editor.simple.components.MockButton;
import com.google.appinventor.client.editor.simple.components.MockCanvas;
import com.google.appinventor.client.editor.simple.components.MockCheckBox;
import com.google.appinventor.client.editor.simple.components.MockCircle;
import com.google.appinventor.client.editor.simple.components.MockCloudDB;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockComponentsUtil;
import com.google.appinventor.client.editor.simple.components.MockContactPicker;
import com.google.appinventor.client.editor.simple.components.MockDatePicker;
import com.google.appinventor.client.editor.simple.components.MockEmailPicker;
import com.google.appinventor.client.editor.simple.components.MockFeatureCollection;
import com.google.appinventor.client.editor.simple.components.MockFusionTablesControl;
import com.google.appinventor.client.editor.simple.components.MockHorizontalArrangement;
import com.google.appinventor.client.editor.simple.components.MockImage;
import com.google.appinventor.client.editor.simple.components.MockImagePicker;
import com.google.appinventor.client.editor.simple.components.MockImageSprite;
import com.google.appinventor.client.editor.simple.components.MockLabel;
import com.google.appinventor.client.editor.simple.components.MockLineString;
import com.google.appinventor.client.editor.simple.components.MockListPicker;
import com.google.appinventor.client.editor.simple.components.MockListView;
import com.google.appinventor.client.editor.simple.components.MockMap;
import com.google.appinventor.client.editor.simple.components.MockMarker;
import com.google.appinventor.client.editor.simple.components.MockNonVisibleComponent;
import com.google.appinventor.client.editor.simple.components.MockPasswordTextBox;
import com.google.appinventor.client.editor.simple.components.MockPhoneNumberPicker;
import com.google.appinventor.client.editor.simple.components.MockPolygon;
import com.google.appinventor.client.editor.simple.components.MockRadioButton;
import com.google.appinventor.client.editor.simple.components.MockRectangle;
import com.google.appinventor.client.editor.simple.components.MockScrollHorizontalArrangement;
import com.google.appinventor.client.editor.simple.components.MockScrollVerticalArrangement;
import com.google.appinventor.client.editor.simple.components.MockSlider;
import com.google.appinventor.client.editor.simple.components.MockSpinner;
import com.google.appinventor.client.editor.simple.components.MockSwitch;
import com.google.appinventor.client.editor.simple.components.MockTableArrangement;
import com.google.appinventor.client.editor.simple.components.MockTextBox;
import com.google.appinventor.client.editor.simple.components.MockTimePicker;
import com.google.appinventor.client.editor.simple.components.MockVerticalArrangement;
import com.google.appinventor.client.editor.simple.components.MockVideoPlayer;
import com.google.appinventor.client.editor.simple.components.MockWebViewer;
import com.google.appinventor.client.editor.simple.palette.ComponentHelpWidget;
import com.google.appinventor.client.editor.simple.palette.ComponentRemoveWidget;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.simple.palette.SimpleComponentDescriptor;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.widgets.dnd.DragSourcePanel;
import com.google.appinventor.client.widgets.dnd.DragSourceSupport;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * This class represents a component on the component palette panel.
 *
 */
public class PaletteEntry extends DragSourcePanel {
  private static SimpleEditor editor;
  // Queried to determine the set of UI elements that accept drops of palette items
  private DropTargetProvider dropTargetProvider;

  // Cached prototype of the component that this palette item creates.
  // Properties of the prototype may be queried by accessors.
  private MockComponent componentPrototype;

  //It is here to keep the selected panel item
  private static Widget selectedPaletteItemWidget;
  private static String componentType;
  private Image image;
  private String iconName;
  private static Boolean nonVisible;

  /**
   * Selects (sets the background to green of) a palette item when it is clicked.
   *
   * @param paletteItemWidget the Widget of the panel item to be selected
   */
  private static void select(Widget paletteItemWidget) {
    if (selectedPaletteItemWidget != null) {
      selectedPaletteItemWidget.getElement().getStyle().setProperty("backgroundColor", "white");
    }
    selectedPaletteItemWidget = paletteItemWidget;
    selectedPaletteItemWidget.getElement().getStyle().setProperty("backgroundColor", "#d2e0a6");
  }

  private void addHandlers() {
    addMouseDownHandler(new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent arg0) {
        select(getWidget());
      }
    });
    addTouchStartHandler(new TouchStartHandler() {
      @Override
      public void onTouchStart(TouchStartEvent event) {
        select(getWidget());
      }
    });
  }

  /**
   * Returns a new mock component for the palette item.
   * <p>
   * The caller is assumed to take ownership of the returned component.
   *
   * @return mock component
   */
  public MockComponent createMockComponent() {
//    return new MockCanvas(editor);
    return createMockComponentFromPalette();
  }

  /**
   * Returns whether this palette item creates components with a visual representation.
   */
  public boolean isVisibleComponent() {
    return !nonVisible;
  }

  private void cacheInternalComponentPrototype() {
    if (componentPrototype == null) {
      componentPrototype = createMockComponentFromPalette();
    }
  }

  /**
   * Instantiates the corresponding mock component.
   *
   * @return  mock component
   */
  public MockComponent createMockComponentFromPalette() {
    MockComponent mockComponent = createMockComponentInstance();
    mockComponent.onCreateFromPalette();
    return mockComponent;
  }

  /**
   * Instantiates mock component by name.
   */
  public static MockComponent createMockComponentInstance() {
    if (nonVisible) {
      if(componentType.equals(MockCloudDB.TYPE)) {
        return new MockCloudDB(editor, componentType, SimpleComponentDescriptor.getImageFromPath(SimpleComponentDatabase.getInstance(Ode.getInstance().getCurrentYoungAndroidProjectId()).getIconName(componentType),
                null, Ode.getInstance().getCurrentYoungAndroidProjectId()));
      } else if(componentType.equals(MockFusionTablesControl.TYPE)) {
        return new MockFusionTablesControl(editor, componentType, SimpleComponentDescriptor.getImageFromPath(SimpleComponentDatabase.getInstance(Ode.getInstance().getCurrentYoungAndroidProjectId()).getIconName(componentType),
                null, Ode.getInstance().getCurrentYoungAndroidProjectId()));
      } else {
        String pkgName = componentType.contains(".") ? componentType.substring(0, componentType.lastIndexOf('.')) : null;
        return new MockNonVisibleComponent(editor, componentType, SimpleComponentDescriptor.getImageFromPath(SimpleComponentDatabase.getInstance(Ode.getInstance().getCurrentYoungAndroidProjectId()).getIconName(componentType),
                pkgName, Ode.getInstance().getCurrentYoungAndroidProjectId()));
      }
    } else if (componentType.equals(MockButton.TYPE)) {
      return new MockButton(editor);
    } else if (componentType.equals(MockCanvas.TYPE)) {
      return new MockCanvas(editor);
    } else if (componentType.equals(MockCheckBox.TYPE)) {
      return new MockCheckBox(editor);
    } else if (componentType.equals(MockSwitch.TYPE)) {
      return new MockSwitch(editor);
    } else if (componentType.equals(MockImage.TYPE)) {
      return new MockImage(editor);
    } else if (componentType.equals(MockLabel.TYPE)) {
      return new MockLabel(editor);
    } else if (componentType.equals(MockListView.TYPE)) {
      return new MockListView(editor);
    } else if (componentType.equals(MockSlider.TYPE)) {
      return new MockSlider(editor);
    } else if (componentType.equals(MockPasswordTextBox.TYPE)) {
      return new MockPasswordTextBox(editor);
    } else if (componentType.equals(MockRadioButton.TYPE)) {
      return new MockRadioButton(editor);
    } else if (componentType.equals(MockTextBox.TYPE)) {
      return new MockTextBox(editor);
    } else if (componentType.equals(MockContactPicker.TYPE)) {
      return new MockContactPicker(editor);
    } else if (componentType.equals(MockPhoneNumberPicker.TYPE)) {
      return new MockPhoneNumberPicker(editor);
    } else if (componentType.equals(MockEmailPicker.TYPE)) {
      return new MockEmailPicker(editor);
    } else if (componentType.equals(MockListPicker.TYPE)) {
      return new MockListPicker(editor);
    } else if (componentType.equals(MockDatePicker.TYPE)) {
      return new MockDatePicker(editor);
    } else if (componentType.equals(MockTimePicker.TYPE)) {
      return new MockTimePicker(editor);
    } else if (componentType.equals(MockHorizontalArrangement.TYPE)) {
      return new MockHorizontalArrangement(editor);
    } else if (componentType.equals(MockScrollHorizontalArrangement.TYPE)) {
      return new MockScrollHorizontalArrangement(editor);
    } else if (componentType.equals(MockVerticalArrangement.TYPE)) {
      return new MockVerticalArrangement(editor);
    } else if (componentType.equals(MockScrollVerticalArrangement.TYPE)) {
      return new MockScrollVerticalArrangement(editor);
    } else if (componentType.equals(MockTableArrangement.TYPE)) {
      return new MockTableArrangement(editor);
    } else if (componentType.equals(MockImageSprite.TYPE)) {
      return new MockImageSprite(editor);
    } else if (componentType.equals(MockBall.TYPE)) {
      return new MockBall(editor);
    } else if (componentType.equals(MockImagePicker.TYPE)) {
      return new MockImagePicker(editor);
    } else if (componentType.equals(MockVideoPlayer.TYPE)) {
      return new MockVideoPlayer(editor);
    } else if (componentType.equals(MockWebViewer.TYPE)) {
      return new MockWebViewer(editor);
    } else if (componentType.equals(MockSpinner.TYPE)) {
      return new MockSpinner(editor);
    } else if (componentType.equals(MockMap.TYPE)) {
      return new MockMap(editor);
    } else if (componentType.equals(MockMarker.TYPE)) {
      return new MockMarker(editor);
    } else if (componentType.equals(MockCircle.TYPE)) {
      return new MockCircle(editor);
    } else if (componentType.equals(MockLineString.TYPE)) {
      return new MockLineString(editor);
    } else if (componentType.equals(MockPolygon.TYPE)) {
      return new MockPolygon(editor);
    } else if (componentType.equals(MockRectangle.TYPE)) {
      return new MockRectangle(editor);
    } else if (componentType.equals(MockFeatureCollection.TYPE)) {
      return new MockFeatureCollection(editor);
    } else {
      // TODO(user): add 3rd party mock component proxy here
      throw new UnsupportedOperationException("unknown component: " + componentType);
    }
  }


  // DragSource implementation

  @Override
  public void onDragStart() {
    // no action
  }

  @Override
  public Widget createDragWidget(int x, int y) {
    MockComponent component = createMockComponent();
    // Some components override getPreferredWidth/Height because getOffsetWidth/Height (which is
    // what MockComponentsUtil.getPreferredSizeOfDetachedWidget uses) returns very inaccurate
    // values. These components can give us the width/height even when the component is not
    // attached.
    int width = component.getPreferredWidth();
    int height = component.getPreferredHeight();
    if (width <= 0 && height <= 0) {
      // Other components don't override getPreferredWidth/Height, which means that we'll get 0 (or
      // less) because the component is not attached. So, we use getPreferredSizeOfDetachedWidget.
      int[] size = MockComponentsUtil.getPreferredSizeOfDetachedWidget(component);
      width = size[0];
      height = size[1];
    }
    component.setPixelSize(width, height);
    DragSourceSupport.configureDragWidgetToAppearWithCursorAt(component, width / 2, height / 2);
    return component;
  }

  @Override
  public Widget getDragWidget() {
    return dragSourceSupport.getDragWidget();
  }

  @Override
  public DropTarget[] getDropTargets() {
    return dropTargetProvider.getDropTargets();
  }

  @Override
  public void onDragEnd() {
    // no action
  }

  // Utility methods

  public String getName() {
    return componentType;
  }
}
