// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.SimpleEditor;

import com.google.appinventor.client.editor.simple.components.MockBall;
import com.google.appinventor.client.editor.simple.components.MockButton;
import com.google.appinventor.client.editor.simple.components.MockCanvas;
import com.google.appinventor.client.editor.simple.components.MockChatBot;
import com.google.appinventor.client.editor.simple.components.MockChart;
import com.google.appinventor.client.editor.simple.components.MockChartData2D;
import com.google.appinventor.client.editor.simple.components.MockCheckBox;
import com.google.appinventor.client.editor.simple.components.MockCircle;
import com.google.appinventor.client.editor.simple.components.MockCircularProgress;
import com.google.appinventor.client.editor.simple.components.MockCloudDB;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockContactPicker;
import com.google.appinventor.client.editor.simple.components.MockDataFile;
import com.google.appinventor.client.editor.simple.components.MockDatePicker;
import com.google.appinventor.client.editor.simple.components.MockEmailPicker;
import com.google.appinventor.client.editor.simple.components.MockFeatureCollection;
import com.google.appinventor.client.editor.simple.components.MockFilePicker;
import com.google.appinventor.client.editor.simple.components.MockFirebaseDB;
import com.google.appinventor.client.editor.simple.components.MockFusionTablesControl;
import com.google.appinventor.client.editor.simple.components.MockTrendline;
import com.google.appinventor.client.editor.simple.components.MockTwitter;
import com.google.appinventor.client.editor.simple.components.MockLinearProgress;
import com.google.appinventor.client.editor.simple.components.MockSpreadsheet;
import com.google.appinventor.client.editor.simple.components.MockHorizontalArrangement;
import com.google.appinventor.client.editor.simple.components.MockImage;
import com.google.appinventor.client.editor.simple.components.MockImageBot;
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
import com.google.appinventor.client.editor.simple.components.MockTranslator;
import com.google.appinventor.client.editor.simple.components.MockVerticalArrangement;
import com.google.appinventor.client.editor.simple.components.MockVideoPlayer;
import com.google.appinventor.client.editor.simple.components.MockWebViewer;

import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.shared.storage.StorageUtil;

import com.google.common.collect.Maps;

import com.google.gwt.resources.client.ImageResource;
import com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;

import com.google.gwt.user.client.ui.Image;

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

  // The version of the extension (meaning is defined by the extension author).
  private final int version;

  private final String versionName;

  private final String dateBuilt;

  private final String licenseUrl;

  private final Image image;

  /**
   * Creates a new component descriptor.
   *
   * @param name  component display name
   */
  public SimpleComponentDescriptor(String name,
                                   int version,
                                   String versionName,
                                   String dateBuilt,
                                   String helpString,
                                   String helpUrl,
                                   String categoryDocUrlString,
                                   String licenseUrl,
                                   Image image,
                                   boolean showOnPalette,
                                   boolean nonVisible,
                                   boolean external) {
    this.name = name;
    this.version = version;
    this.versionName = versionName;
    this.dateBuilt = dateBuilt;
    this.helpString = helpString;
    this.helpUrl = helpUrl;
    this.categoryDocUrlString = categoryDocUrlString;
    this.licenseUrl = licenseUrl;
    this.image = image;
    this.showOnPalette = showOnPalette;
    this.nonVisible = nonVisible;
    this.external = external;
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
   * {@link ComponentDatabaseInterface#getHelpString(String)}.
   *
   * @return helpful message about the component
   */
  public String getHelpString() {
    return helpString;
  }

  /**
   * Returns the help URL for the component.  For more detail, see javadoc for
   * {@link ComponentDatabaseInterface#getHelpUrl(String)}.
   *
   * @return URL to external documentation provided for an extension
   */
  public String getHelpUrl() {
    return helpUrl;
  }

  /**
   * Returns the origin of the component.
   * @return true if component is external
   */
  public boolean getExternal() {
    return external;
  }

  /**
   * Returns the categoryDocUrl string for the component.
   *
   * <p>For more detail, see javadoc for
   * {@link ComponentDatabaseInterface#getCategoryDocUrlString(String)}.
   *
   * @return helpful message about the component
   */
  public String getCategoryDocUrlString() {
    return categoryDocUrlString;
  }

  /**
   * Returns whether this component should be shown on the palette.  For more
   * detail, see javadoc for
   * {@link ComponentDatabaseInterface#getHelpString(String)}.
   *
   * @return whether the component should be shown on the palette
   */
  public boolean getShowOnPalette() {
    return showOnPalette;
  }

  /**
   * Returns whether this component is visible in the app's UI.  For more
   * detail, see javadoc for
   * {@link com.google.appinventor.shared.simple.ComponentDatabaseInterface#getHelpString(String)}.
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
    return licenseUrl;
  }

  /**
   * Instantiates the corresponding mock component.
   *
   * @return  mock component
   */
  public MockComponent createMockComponentFromPalette(SimpleEditor editor) {
    MockComponent mockComponent = createMockComponent(name,
        editor.getComponentDatabase().getComponentType(name), editor);
    mockComponent.onCreateFromPalette();
    return mockComponent;
  }

  /**
   * Instantiates mock component by name.
   */
  public static MockComponent createMockComponent(String name, String type, SimpleEditor editor) {
    if (SimpleComponentDatabase.getInstance(editor.getProjectId()).getNonVisible(name)) {
      if (name.equals(MockFirebaseDB.TYPE)) {
        return new MockFirebaseDB(editor, name,
            YoungAndroidPalettePanel.getImageFromPath(
                editor.getComponentDatabase().getIconName(name), null));
      } else if (name.equals(MockCloudDB.TYPE)) {
        return new MockCloudDB(editor, name,
            YoungAndroidPalettePanel.getImageFromPath(
                editor.getComponentDatabase().getIconName(name), null));
      } else if (name.equals(MockFusionTablesControl.TYPE)) {
        return new MockFusionTablesControl(editor, name,
            YoungAndroidPalettePanel.getImageFromPath(
                editor.getComponentDatabase().getIconName(name), null));
      } else if(name.equals(MockTranslator.TYPE)) {
        return new MockTranslator(editor, name,
            YoungAndroidPalettePanel.getImageFromPath(
                editor.getComponentDatabase().getIconName(name), null));
      } else if(name.equals(MockChatBot.TYPE)) {
        return new MockTranslator(editor, name,
            YoungAndroidPalettePanel.getImageFromPath(
                editor.getComponentDatabase().getIconName(name), null));
      }else if(name.equals(MockImageBot.TYPE)) {
        return new MockTranslator(editor, name,
            YoungAndroidPalettePanel.getImageFromPath(
                editor.getComponentDatabase().getIconName(name), null));
      }else if(name.equals(MockSpreadsheet.TYPE)) {
        return new MockSpreadsheet(editor, name,
            YoungAndroidPalettePanel.getImageFromPath(
                editor.getComponentDatabase().getIconName(name), null));
      } else if (name.equals(MockDataFile.TYPE)) {
        return new MockDataFile(editor, name,
            YoungAndroidPalettePanel.getImageFromPath(
                editor.getComponentDatabase().getIconName(name), null));
      } else {
        String pkgName = type.contains(".") ? type.substring(0, type.lastIndexOf('.')) : null;
        return new MockNonVisibleComponent(editor, name,
            YoungAndroidPalettePanel.getImageFromPath(
                editor.getComponentDatabase().getIconName(name), pkgName));
      }
    } else if (name.equals(MockButton.TYPE)) {
      return new MockButton(editor);
    } else if (name.equals(MockCanvas.TYPE)) {
      return new MockCanvas(editor);
    } else if (name.equals(MockCheckBox.TYPE)) {
      return new MockCheckBox(editor);
    } else if (name.equals(MockFilePicker.TYPE)) {
      return new MockFilePicker(editor);
    } else if (name.equals(MockSwitch.TYPE)) {
      return new MockSwitch(editor);
    } else if (name.equals(MockImage.TYPE)) {
      return new MockImage(editor);
    } else if (name.equals(MockLabel.TYPE)) {
      return new MockLabel(editor);
    } else if (name.equals(MockListView.TYPE)) {
      return new MockListView(editor);
    } else if (name.equals(MockSlider.TYPE)) {
      return new MockSlider(editor);
    } else if (name.equals(MockPasswordTextBox.TYPE)) {
      return new MockPasswordTextBox(editor);
    } else if (name.equals(MockRadioButton.TYPE)) {
      return new MockRadioButton(editor);
    } else if (name.equals(MockTextBox.TYPE)) {
      return new MockTextBox(editor);
    } else if (name.equals(MockContactPicker.TYPE)) {
      return new MockContactPicker(editor);
    } else if (name.equals(MockPhoneNumberPicker.TYPE)) {
      return new MockPhoneNumberPicker(editor);
    } else if (name.equals(MockEmailPicker.TYPE)) {
      return new MockEmailPicker(editor);
    } else if (name.equals(MockListPicker.TYPE)) {
      return new MockListPicker(editor);
    } else if (name.equals(MockDatePicker.TYPE)) {
      return new MockDatePicker(editor);
    } else if (name.equals(MockTimePicker.TYPE)) {
      return new MockTimePicker(editor);
    } else if (name.equals(MockHorizontalArrangement.TYPE)) {
      return new MockHorizontalArrangement(editor);
    } else if (name.equals(MockScrollHorizontalArrangement.TYPE)) {
      return new MockScrollHorizontalArrangement(editor);
    } else if (name.equals(MockVerticalArrangement.TYPE)) {
      return new MockVerticalArrangement(editor);
    } else if (name.equals(MockScrollVerticalArrangement.TYPE)) {
      return new MockScrollVerticalArrangement(editor);
    } else if (name.equals(MockTableArrangement.TYPE)) {
      return new MockTableArrangement(editor);
    } else if (name.equals(MockImageSprite.TYPE)) {
      return new MockImageSprite(editor);
    } else if (name.equals(MockBall.TYPE)) {
      return new MockBall(editor);
    } else if (name.equals(MockImagePicker.TYPE)) {
      return new MockImagePicker(editor);
    } else if (name.equals(MockVideoPlayer.TYPE)) {
      return new MockVideoPlayer(editor);
    } else if (name.equals(MockWebViewer.TYPE)) {
      return new MockWebViewer(editor);
    } else if (name.equals(MockSpinner.TYPE)) {
      return new MockSpinner(editor);
    } else if (name.equals(MockMap.TYPE)) {
      return new MockMap(editor);
    } else if (name.equals(MockMarker.TYPE)) {
      return new MockMarker(editor);
    } else if (name.equals(MockCircle.TYPE)) {
      return new MockCircle(editor);
    } else if (name.equals(MockLineString.TYPE)) {
      return new MockLineString(editor);
    } else if (name.equals(MockPolygon.TYPE)) {
      return new MockPolygon(editor);
    } else if (name.equals(MockRectangle.TYPE)) {
      return new MockRectangle(editor);
    } else if (name.equals(MockFeatureCollection.TYPE)) {
      return new MockFeatureCollection(editor);
    } else if (name.equals(MockChart.TYPE)) {
      return new MockChart(editor);
    } else if (name.equals(MockChartData2D.TYPE)) {
      return new MockChartData2D(editor);
    } else if (name.equals(MockCircularProgress.TYPE)) {
      return new MockCircularProgress(editor);
    } else if (name.equals(MockLinearProgress.TYPE)) {
      return new MockLinearProgress(editor);
    } else if (name.equals(MockTrendline.TYPE)) {
      return new MockTrendline(editor);
    } else {
      // TODO(user): add 3rd party mock component proxy here
      throw new UnsupportedOperationException("unknown component: " + name);
    }
  }
}
