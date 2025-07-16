// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.simple.components.MockAbsoluteArrangement;
import com.google.appinventor.client.editor.simple.components.MockBall;
import com.google.appinventor.client.editor.simple.components.MockButton;
import com.google.appinventor.client.editor.simple.components.MockCanvas;
import com.google.appinventor.client.editor.simple.components.MockChart;
import com.google.appinventor.client.editor.simple.components.MockChartData2D;
import com.google.appinventor.client.editor.simple.components.MockChatBot;
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
import com.google.appinventor.client.editor.simple.components.MockHorizontalArrangement;
import com.google.appinventor.client.editor.simple.components.MockImage;
import com.google.appinventor.client.editor.simple.components.MockImageBot;
import com.google.appinventor.client.editor.simple.components.MockImagePicker;
import com.google.appinventor.client.editor.simple.components.MockImageSprite;
import com.google.appinventor.client.editor.simple.components.MockLabel;
import com.google.appinventor.client.editor.simple.components.MockLineString;
import com.google.appinventor.client.editor.simple.components.MockLinearProgress;
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
import com.google.appinventor.client.editor.simple.components.MockSpreadsheet;
import com.google.appinventor.client.editor.simple.components.MockSwitch;
import com.google.appinventor.client.editor.simple.components.MockTableArrangement;
import com.google.appinventor.client.editor.simple.components.MockTextBox;
import com.google.appinventor.client.editor.simple.components.MockTimePicker;
import com.google.appinventor.client.editor.simple.components.MockTranslator;
import com.google.appinventor.client.editor.simple.components.MockTrendline;
import com.google.appinventor.client.editor.simple.components.MockTwitter;
import com.google.appinventor.client.editor.simple.components.MockVerticalArrangement;
import com.google.appinventor.client.editor.simple.components.MockVideoPlayer;
import com.google.appinventor.client.editor.simple.components.MockWebViewer;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import java.util.Map;

/**
 * Base implementation of ComponentFactory that can be subclassed for specific App Inventor editors.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class BaseComponentFactory implements ComponentFactory {
  /* We keep a static map of image names to images in the image bundle so
     * that we can avoid making individual calls to the server for static image
     * that are already in the bundle. This is purely an efficiency optimization
     * for mock non-visible components.
     */
  protected final DesignerEditor<?, ?, ?, ?, ?> editor;
  private final Map<String, ImageResource> bundledImages;
  private final ComponentDatabaseInterface componentDatabase;

  /**
   * Creates a new BaseComponentFactory.
   *
   * @param editor the editor instance that will manage the components
   * @param bundledImages the map of image names to images
   */
  public BaseComponentFactory(DesignerEditor<?, ?, ?, ?, ?> editor,
                              Map<String, ImageResource> bundledImages) {
    this.editor = editor;
    this.bundledImages = bundledImages;
    componentDatabase = editor.getComponentDatabase();
  }

  @Override
  public MockComponent createMockComponent(String name, String type) {
    if (componentDatabase.getNonVisible(name)) {
      if (name.equals(MockFirebaseDB.TYPE)) {
        return new MockFirebaseDB(editor, name,
          getImageFromPath(componentDatabase.getIconName(name), null));
      } else if (name.equals(MockCloudDB.TYPE)) {
        return new MockCloudDB(editor, name,
          getImageFromPath(componentDatabase.getIconName(name), null));
      } else if (name.equals(MockFusionTablesControl.TYPE)) {
        return new MockFusionTablesControl(editor, name,
          getImageFromPath(componentDatabase.getIconName(name), null));
      } else if (name.equals(MockTwitter.TYPE)) {
        return new MockTwitter(editor, name,
          getImageFromPath(componentDatabase.getIconName(name), null));
      } else if (name.equals(MockTranslator.TYPE)) {
        return new MockTranslator(editor, name,
          getImageFromPath(componentDatabase.getIconName(name), null));
      } else if (name.equals(MockChatBot.TYPE)) {
        return new MockChatBot(editor, name,
          getImageFromPath(componentDatabase.getIconName(name), null));
      } else if (name.equals(MockImageBot.TYPE)) {
        return new MockImageBot(editor, name,
          getImageFromPath(componentDatabase.getIconName(name), null));
      } else if (name.equals(MockSpreadsheet.TYPE)) {
        return new MockSpreadsheet(editor, name,
          getImageFromPath(componentDatabase.getIconName(name), null));
      } else if (name.equals(MockDataFile.TYPE)) {
        return new MockDataFile(editor, name,
          getImageFromPath(componentDatabase.getIconName(name), null));
      } else {
        if (type == null) {
          type = componentDatabase.getComponentType(name);
        }
        String pkgName = type.contains(".") ? type.substring(0, type.lastIndexOf('.')) : null;
        return new MockNonVisibleComponent(editor, name,
          getImageFromPath(componentDatabase.getIconName(name), pkgName));
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
    } else if (name.equals(MockAbsoluteArrangement.TYPE)) {
      return new MockAbsoluteArrangement(editor);
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

  @Override
  public Image getImage(String name, String type) {
    return getImageFromPath(componentDatabase.getIconName(name), type);
  }

  @Override
  public Image getImage(String name) {
    if (componentDatabase.getComponentExternal(name)) {
      String type = componentDatabase.getComponentType(name);
      return getImage(name, type.substring(0, type.lastIndexOf('.')));
    }
    return getImage(name, null);
  }

  /**
   * Returns the path to the license file used by the component.
   *
   * @return path to license file of component
   */
  public String getLicense(String name, String type) {
    return getLicenseUrlFromPath(componentDatabase.getLicenseName(name),
        type.substring(0, type.lastIndexOf('.')));
  }

  private Image getImageFromPath(String iconPath, String packageName) {
    if (iconPath.startsWith("aiwebres/") && packageName != null) {
      // icon for extension
      Image image = new Image(StorageUtil.getFileUrl(editor.getProjectId(),
          "assets/external_comps/" + packageName + "/" + iconPath));
      image.setWidth("16px");
      image.setHeight("16px");
      return image;
    }
    if (bundledImages.containsKey(iconPath)) {
      return new Image(bundledImages.get(iconPath));
    } else {
      return new Image(iconPath);
    }
  }

  private String getLicenseUrlFromPath(String licensePath, String packageName) {
    if (licensePath.startsWith("aiwebres/") && packageName != null) {
      // License file is inside aiwebres
      return StorageUtil.getFileUrl(editor.getProjectId(),
          "assets/external_comps/" + packageName + "/" + licensePath) + "&inline";
    } else if (licensePath.startsWith("http:") || licensePath.startsWith("https:")) {
      // The license is an external URL
      return licensePath;
    } else {
      // No license file specified
      return "";
    }
  }
}
